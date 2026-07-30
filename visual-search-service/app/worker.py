import asyncio
import logging

from app.config import get_settings
from app.messaging.consumer import CatalogEventConsumer
from app.persistence import VisualSearchRepository
from app.providers.factory import build_provider
from app.services.catalog_indexer import CatalogIndexer
from app.services.image_pipeline import ImagePipeline


async def main() -> None:
    settings = get_settings()
    if not settings.visual_search_enabled:
        raise RuntimeError("VISUAL_SEARCH_ENABLED must be true to run the indexing worker")
    indexer = CatalogIndexer(
        VisualSearchRepository(settings),
        ImagePipeline(settings),
        build_provider(settings),
    )
    consumer = CatalogEventConsumer(settings, indexer.handle, indexer.mark_exhausted)
    await consumer.run()
    try:
        await asyncio.Future()
    finally:
        await consumer.close()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())
