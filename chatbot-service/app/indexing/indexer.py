from __future__ import annotations

from uuid import UUID

import asyncpg

from app.services.embedder import embed_batch
from app.services.product_search_document import PRODUCT_DOCUMENT_SQL, canonical_document, content_hash


async def index_product(pool: asyncpg.Pool, product_id: UUID, model: str, dimensions: int) -> str:
    async with pool.acquire() as conn:
        row = await conn.fetchrow(PRODUCT_DOCUMENT_SQL, product_id)
        if row is None:
            return "MISSING"
        if row["status"] != "ACTIVE":
            await conn.execute(
                "update product_embeddings set status='STALE', updated_at=now() where product_id=$1",
                product_id,
            )
            return "STALE"

        document = canonical_document(row)
        digest = content_hash(document)
        current = await conn.fetchrow(
            """select content_hash,embedding_model,embedding_dimensions,status
               from product_embeddings where product_id=$1""",
            product_id,
        )
        if current and current["content_hash"] == digest and current["embedding_model"] == model \
                and current["embedding_dimensions"] == dimensions and current["status"] == "READY":
            return "SKIPPED"

        vectors = await embed_batch([document])
        if not vectors or len(vectors[0]) != dimensions:
            raise RuntimeError("Embedding provider returned an invalid vector")
        await conn.execute(
            """
            insert into product_embeddings(
                product_id,embedding,document_text,embedding_model,
                embedding_dimensions,content_hash,status,last_error,updated_at
            ) values($1,$2::vector,$3,$4,$5,$6,'READY',null,now())
            on conflict(product_id) do update set
                embedding=excluded.embedding, document_text=excluded.document_text,
                embedding_model=excluded.embedding_model,
                embedding_dimensions=excluded.embedding_dimensions,
                content_hash=excluded.content_hash, status='READY',
                last_error=null, updated_at=now()
            """,
            product_id, str(vectors[0]), document, model, dimensions, digest,
        )
        return "UPDATED"
