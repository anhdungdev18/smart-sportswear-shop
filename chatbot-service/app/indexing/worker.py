from __future__ import annotations

import asyncio
import logging

import asyncpg

from app.config.settings import settings
from app.indexing.consumer import ProductIndexingConsumer
from app.indexing.events import CatalogEvent
from app.indexing.indexer import index_product

logger = logging.getLogger(__name__)


class ProductIndexingWorker:
    def __init__(self, pool: asyncpg.Pool):
        self.pool = pool

    async def handle(self, event: CatalogEvent) -> None:
        async with self.pool.acquire() as conn:
            inserted = await conn.fetchval(
                """
                insert into product_search_processed_events(event_id,event_type,event_version)
                values($1,$2,$3) on conflict(event_id) do nothing returning true
                """,
                event.eventId, event.eventType, event.eventVersion,
            )
        if not inserted:
            return
        try:
            await index_product(
                self.pool, event.productId, settings.EMBEDDING_MODEL, settings.EMBEDDING_DIMS
            )
        except Exception:
            async with self.pool.acquire() as conn:
                await conn.execute(
                    "delete from product_search_processed_events where event_id=$1", event.eventId
                )
            raise

    async def reconcile_once(self) -> int:
        async with self.pool.acquire() as conn:
            ids = await conn.fetch(
                """
                select p.id
                from products p
                left join product_embeddings pe on pe.product_id=p.id
                where p.status='ACTIVE'
                  and (pe.product_id is null or pe.status <> 'READY'
                       or pe.embedding_model <> $1 or pe.embedding_dimensions <> $2)
                order by p.updated_at
                limit $3
                """,
                settings.EMBEDDING_MODEL,
                settings.EMBEDDING_DIMS,
                settings.PRODUCT_SEARCH_RECONCILIATION_BATCH_SIZE,
            )
        for row in ids:
            await index_product(self.pool, row["id"], settings.EMBEDDING_MODEL, settings.EMBEDDING_DIMS)
        return len(ids)


async def run_worker() -> None:
    if not settings.PRODUCT_SEARCH_INDEXING_ENABLED:
        raise RuntimeError("PRODUCT_SEARCH_INDEXING_ENABLED must be true")
    if not settings.DB_WRITE_URL:
        raise RuntimeError("DB_WRITE_URL is required by the indexing worker")
    dsn = settings.DB_WRITE_URL.replace("postgresql+asyncpg://", "postgresql://", 1)
    pool = await asyncpg.create_pool(dsn, min_size=1, max_size=3)
    worker = ProductIndexingWorker(pool)
    consumer = ProductIndexingConsumer(settings, worker.handle)
    await consumer.run()
    try:
        while True:
            await asyncio.sleep(settings.PRODUCT_SEARCH_RECONCILIATION_INTERVAL_SECONDS)
            try:
                await worker.reconcile_once()
            except Exception:
                logger.exception("Product search reconciliation failed")
    finally:
        await consumer.close()
        await pool.close()
