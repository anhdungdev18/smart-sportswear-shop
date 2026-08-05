from __future__ import annotations

import logging

import aio_pika
from aio_pika import DeliveryMode, Message
from pydantic import ValidationError

from app.config.settings import Settings
from app.indexing.events import CatalogEvent

logger = logging.getLogger(__name__)


class ProductIndexingConsumer:
    def __init__(self, settings: Settings, handler):
        self.settings = settings
        self.handler = handler
        self.connection = None
        self.channel = None

    async def run(self) -> None:
        self.connection = await aio_pika.connect_robust(self.settings.RABBITMQ_URL)
        self.channel = await self.connection.channel()
        await self.channel.set_qos(prefetch_count=self.settings.PRODUCT_SEARCH_INDEXING_PREFETCH)
        queue = await self.channel.declare_queue(self.settings.PRODUCT_SEARCH_INDEXING_QUEUE, passive=True)
        await queue.consume(self.process_message)

    async def close(self) -> None:
        if self.connection:
            await self.connection.close()

    async def process_message(self, message) -> None:
        event = None
        try:
            event = CatalogEvent.model_validate_json(message.body)
            await self.handler(event)
            await message.ack()
        except ValidationError:
            await self._route(message, permanent=True, error_type="InvalidEvent")
        except Exception as exc:
            logger.warning("Product indexing event failed: %s", type(exc).__name__)
            await self._route(message, permanent=False, error_type=type(exc).__name__)

    async def _route(self, message, permanent: bool, error_type: str) -> None:
        headers = dict(message.headers or {})
        count = int(headers.get("x-product-search-retry-count", 0))
        retries = tuple(
            item.strip() for item in self.settings.PRODUCT_SEARCH_INDEXING_RETRY_QUEUES.split(",") if item.strip()
        )
        if not permanent and count < len(retries):
            target = retries[count]
            headers["x-product-search-retry-count"] = count + 1
        else:
            target = self.settings.PRODUCT_SEARCH_INDEXING_DLQ
            headers["x-product-search-error-type"] = error_type
        outgoing = Message(
            body=message.body,
            headers=headers,
            content_type=message.content_type or "application/json",
            delivery_mode=DeliveryMode.PERSISTENT,
            correlation_id=message.correlation_id,
        )
        if self.channel is None:
            raise RuntimeError("RabbitMQ channel is not initialized")
        await self.channel.default_exchange.publish(outgoing, routing_key=target, mandatory=True)
        await message.ack()
