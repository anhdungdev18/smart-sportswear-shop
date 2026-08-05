"""Idempotent ACTIVE product search embedding backfill (dry-run by default)."""
from __future__ import annotations

import argparse
import asyncio
import os
import sys
from pathlib import Path

import asyncpg
from dotenv import load_dotenv
from openai import AsyncOpenAI

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.services.product_search_document import canonical_document, content_hash


async def main(args: argparse.Namespace) -> int:
    load_dotenv()
    model = os.getenv("EMBEDDING_MODEL", "text-embedding-3-small")
    dimensions = int(os.getenv("EMBEDDING_DIMS", "1536"))
    dsn_key = "DB_WRITE_URL" if args.apply else "DB_READ_URL"
    dsn = os.environ[dsn_key].replace("postgresql+asyncpg://", "postgresql://", 1)
    conn = await asyncpg.connect(dsn)
    try:
        products = await conn.fetch(
            """
            select p.id, p.name, p.short_description, p.description, p.gender,
                   p.sport_type, p.product_type, p.attributes, c.name category_name,
                   b.name brand_name,
                   coalesce(array_agg(distinct pv.sku) filter (where pv.status='ACTIVE'), '{}') skus,
                   coalesce(array_agg(distinct pv.color) filter (where pv.status='ACTIVE'), '{}') colors,
                   coalesce(array_agg(distinct pv.size) filter (where pv.status='ACTIVE'), '{}') sizes
            from products p
            join categories c on c.id=p.category_id
            join brands b on b.id=p.brand_id
            left join product_variants pv on pv.product_id=p.id
            where p.status='ACTIVE'
            group by p.id,c.name,b.name order by p.id
            """
        )
        current = {
            row["product_id"]: row
            for row in await conn.fetch(
                "select product_id,content_hash,embedding_model,embedding_dimensions from product_embeddings"
            )
        }
        documents = [(row, canonical_document(row)) for row in products]
        pending = []
        fresh = 0
        for row, document in documents:
            digest = content_hash(document)
            saved = current.get(row["id"])
            if (
                saved and saved["content_hash"] == digest
                and saved["embedding_model"] == model
                and saved["embedding_dimensions"] == dimensions
            ):
                fresh += 1
            else:
                pending.append((row["id"], document, digest))
        inactive_stale = await conn.fetchval(
            """
            select count(*) from product_embeddings pe join products p on p.id=pe.product_id
            where p.status <> 'ACTIVE'
            """
        )
        missing = sum(1 for product_id, _, _ in pending if product_id not in current)
        print(f"mode={'APPLY' if args.apply else 'COVERAGE' if args.coverage else 'DRY_RUN'}")
        print(f"active_products={len(products)} already_fresh={fresh} missing={missing}")
        print(f"needs_update={len(pending)} inactive_stale_rows={inactive_stale}")
        if not args.apply:
            coverage = fresh / len(products) * 100 if products else 100
            print(f"fresh_coverage_percent={coverage:.2f}")
            return 0

        client = AsyncOpenAI(api_key=os.environ["OPENAI_API_KEY"])
        updated = failed = 0
        for offset in range(0, len(pending), args.batch_size):
            batch = pending[offset : offset + args.batch_size]
            try:
                response = await client.embeddings.create(
                    model=model,
                    input=[document for _, document, _ in batch],
                    dimensions=dimensions,
                )
                rows = []
                for (product_id, document, digest), item in zip(batch, response.data):
                    if len(item.embedding) != dimensions:
                        raise ValueError("embedding dimension mismatch")
                    rows.append((product_id, str(item.embedding), document, model, dimensions, digest))
                await conn.executemany(
                    """
                    insert into product_embeddings(
                        product_id,embedding,document_text,embedding_model,
                        embedding_dimensions,content_hash,status,last_error,updated_at
                    ) values($1,$2::vector,$3,$4,$5,$6,'READY',null,now())
                    on conflict(product_id) do update set
                        embedding=excluded.embedding,document_text=excluded.document_text,
                        embedding_model=excluded.embedding_model,
                        embedding_dimensions=excluded.embedding_dimensions,
                        content_hash=excluded.content_hash,status='READY',
                        last_error=null,updated_at=now()
                    """,
                    rows,
                )
                updated += len(rows)
            except Exception as exc:
                failed += len(batch)
                print(f"batch_failed offset={offset} error_type={type(exc).__name__}")
        print(f"updated={updated} failed={failed}")
        return 0 if failed == 0 else 1
    finally:
        await conn.close()


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--coverage", action="store_true")
    parser.add_argument("--batch-size", type=int, default=50)
    raise SystemExit(asyncio.run(main(parser.parse_args())))
