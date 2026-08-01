import asyncio
import logging

from app.config import get_settings
from app.messaging.consumer import CatalogEventConsumer
from app.persistence import VisualSearchRepository
from app.providers.factory import build_provider
from app.services.catalog_indexer import CatalogIndexer
from app.services.image_pipeline import ImagePipeline
from app.services.indexing_jobs import ReconciliationScheduler


async def main() -> None:
    settings = get_settings()
    if not settings.visual_search_enabled:
        raise RuntimeError("VISUAL_SEARCH_ENABLED must be true to run the indexing worker")
    repository = VisualSearchRepository(settings)
    indexer = CatalogIndexer(
        repository,
        ImagePipeline(settings),
        build_provider(settings),
    )
    consumer = CatalogEventConsumer(settings, indexer.handle, indexer.mark_exhausted)
    await consumer.run()
    reconciliation_task = None
    if settings.reconciliation_enabled:
        scheduler = ReconciliationScheduler(
            repository,
            interval_seconds=settings.reconciliation_interval_seconds,
            initial_delay_seconds=settings.reconciliation_initial_delay_seconds,
            processing_timeout_minutes=settings.reconciliation_processing_timeout_minutes,
            batch_size=settings.reconciliation_batch_size,
        )
        reconciliation_task = asyncio.create_task(scheduler.run(), name="visual-reconciliation")
    try:
        await asyncio.Future()
    finally:
        if reconciliation_task is not None:
            reconciliation_task.cancel()
            await asyncio.gather(reconciliation_task, return_exceptions=True)
        await consumer.close()


import sys

if __name__ == "__main__":
    if sys.platform == "win32":
        asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())
