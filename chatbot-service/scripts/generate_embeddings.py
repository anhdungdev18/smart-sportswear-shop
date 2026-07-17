"""
One-time script: generate OpenAI embeddings for all active products
and upsert them into the product_embeddings table.

Usage:
    cd chatbot-service
    python scripts/generate_embeddings.py

Requirements:
    - .env with DB_READ_URL, DB_WRITE_URL and a real OPENAI_API_KEY set
    - pgvector extension enabled in Supabase (CREATE EXTENSION IF NOT EXISTS vector)
    - V13 migration applied (product_embeddings table with vector(1536) column)
    - openai lib installed (already in requirements.txt)
"""
from __future__ import annotations

import asyncio
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

from dotenv import load_dotenv
load_dotenv()

import asyncpg
from openai import AsyncOpenAI

READ_DB_URL     = os.environ["DB_READ_URL"].replace("postgresql+asyncpg://", "postgresql://")
WRITE_DB_URL    = os.environ["DB_WRITE_URL"].replace("postgresql+asyncpg://", "postgresql://")
OPENAI_API_KEY  = os.environ.get("OPENAI_API_KEY", "")
EMBEDDING_MODEL = os.environ.get("EMBEDDING_MODEL", "text-embedding-3-small")
EMBEDDING_DIMS  = int(os.environ.get("EMBEDDING_DIMS", "1536"))
BATCH_SIZE      = 64   # OpenAI accepts up to 2048 inputs/request; 64 keeps payloads small


_FETCH_SQL = """
    SELECT
        p.id::text            AS product_id,
        p.name,
        p.short_description,
        p.sport_type,
        p.gender,
        p.product_type,
        c.name                AS category_name,
        b.name                AS brand_name
    FROM products p
    JOIN categories c ON c.id = p.category_id
    JOIN brands     b ON b.id = p.brand_id
    WHERE p.status = 'ACTIVE'
    ORDER BY p.created_at
"""

_UPSERT_SQL = """
    INSERT INTO product_embeddings (product_id, embedding, document_text, updated_at)
    VALUES ($1, $2, $3, now())
    ON CONFLICT (product_id) DO UPDATE
        SET embedding     = EXCLUDED.embedding,
            document_text = EXCLUDED.document_text,
            updated_at    = now()
"""


def _make_document(row: dict) -> str:
    parts = [
        row["name"] or "",
        row["category_name"] or "",
        row["sport_type"] or "",
        row["gender"] or "",
        row["brand_name"] or "",
        row["short_description"] or "",
        row["product_type"] or "",
    ]
    return " ".join(p for p in parts if p).strip()


async def main() -> None:
    if not OPENAI_API_KEY or OPENAI_API_KEY.startswith("sk-..."):
        print("ERROR: OPENAI_API_KEY chưa được đặt (hoặc còn là placeholder) trong .env")
        sys.exit(1)

    client = AsyncOpenAI(api_key=OPENAI_API_KEY)

    print("Connecting to DB...")
    read_pool = await asyncpg.create_pool(READ_DB_URL, min_size=1, max_size=3)
    write_pool = await asyncpg.create_pool(WRITE_DB_URL, min_size=1, max_size=3)

    async with read_pool.acquire() as conn:
        rows = await conn.fetch(_FETCH_SQL)
    products = [dict(r) for r in rows]
    print(f"Found {len(products)} active products")

    if not products:
        print("Nothing to embed.")
        await read_pool.close()
        await write_pool.close()
        return

    upserted = 0
    total_batches = (len(products) + BATCH_SIZE - 1) // BATCH_SIZE

    for i in range(0, len(products), BATCH_SIZE):
        batch = products[i : i + BATCH_SIZE]
        docs  = [_make_document(p) for p in batch]

        print(f"  Embedding batch {i // BATCH_SIZE + 1}/{total_batches} ({len(batch)} docs)...")
        resp = await client.embeddings.create(
            model=EMBEDDING_MODEL,
            input=docs,
            dimensions=EMBEDDING_DIMS,
        )
        embeddings = [item.embedding for item in resp.data]
        if any(len(vector) != EMBEDDING_DIMS for vector in embeddings):
            raise RuntimeError(f"Embedding dimension mismatch: expected {EMBEDDING_DIMS}")

        async with write_pool.acquire() as conn:
            async with conn.transaction():
                for product, vec, doc in zip(batch, embeddings, docs):
                    emb_str = f"[{','.join(str(v) for v in vec)}]"
                    await conn.execute(_UPSERT_SQL, product["product_id"], emb_str, doc)

        upserted += len(batch)
        print(f"  → {upserted}/{len(products)} upserted")

    await read_pool.close()
    await write_pool.close()
    print(f"\nDone. {upserted} product embeddings stored ({EMBEDDING_MODEL}, {EMBEDDING_DIMS}-dim).")


if __name__ == "__main__":
    asyncio.run(main())
