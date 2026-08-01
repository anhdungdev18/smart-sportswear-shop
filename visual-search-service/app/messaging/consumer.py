import json
import logging
from collections.abc import Awaitable, Callable

import aio_pika
from aio_pika import DeliveryMode, Message
from pydantic import ValidationError

from app.config import Settings

from .events import CatalogEvent
from .retry import PermanentEventError, RetryableEventError, classify_processing_error

logger = logging.getLogger(__name__)
EventHandler = Callable[[CatalogEvent], Awaitable[None]]
ExhaustedHandler = Callable[[CatalogEvent, Exception], Awaitable[None]]


class CatalogEventConsumer:
    def __init__(self, settings: Settings, handler: EventHandler, exhausted_handler: ExhaustedHandler | None = None):
        self.settings = settings
        self.handler = handler
        self.exhausted_handler = exhausted_handler
        self.connection: aio_pika.abc.AbstractRobustConnection | None = None
        self.channel: aio_pika.abc.AbstractRobustChannel | None = None

    async def run(self) -> None:
        self.connection = await aio_pika.connect_robust(self.settings.rabbitmq_url)
        self.channel = await self.connection.channel()
        await self.channel.set_qos(prefetch_count=self.settings.rabbitmq_prefetch_count)
        queue = await self.channel.declare_queue(self.settings.rabbitmq_consumer_queue, passive=True)
        await queue.consume(self.process_message, no_ack=False)

    async def close(self) -> None:
        if self.connection is not None:
            await self.connection.close()

    async def process_message(self, message: aio_pika.abc.AbstractIncomingMessage) -> None:
        event: CatalogEvent | None = None
        try:
            event = CatalogEvent.model_validate_json(message.body)
            await self.handler(event)
        except (ValidationError, json.JSONDecodeError) as error:
            await self._route(message, PermanentEventError("Invalid catalog event contract"), event)
            return
        except Exception as error:
            await self._route(message, classify_processing_error(error), event)
            return
        await message.ack()

    async def _route(
        self, message: aio_pika.abc.AbstractIncomingMessage, error: Exception, event: CatalogEvent | None
    ) -> None:
        headers = dict(message.headers or {})
        retry_count = int(headers.get("x-visual-retry-count", 0))
        retry_queues = self.settings.retry_queues
        if isinstance(error, RetryableEventError) and retry_count < len(retry_queues):
            target = retry_queues[retry_count]
            headers["x-visual-retry-count"] = retry_count + 1
        else:
            target = self.settings.rabbitmq_dlq
            headers["x-visual-error-type"] = type(error).__name__
            if event is not None and self.exhausted_handler is not None:
                try:
                    await self.exhausted_handler(event, error)
                except Exception:
                    logger.exception("Could not persist exhausted catalog event state")
        outgoing = Message(
            body=message.body,
            headers=headers,
            content_type=message.content_type or "application/json",
            delivery_mode=DeliveryMode.PERSISTENT,
            correlation_id=message.correlation_id,
        )
        if self.channel is None:
            raise RuntimeError("RabbitMQ consumer channel is not initialized")
        await self.channel.default_exchange.publish(outgoing, routing_key=target, mandatory=True)
        logger.warning("Catalog event routed to %s after %s", target, type(error).__name__)
        await message.ack()
