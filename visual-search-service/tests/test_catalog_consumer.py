import asyncio
import json
from datetime import UTC, datetime
from types import SimpleNamespace
from uuid import uuid4

import pytest
from pydantic import ValidationError

from app.config import Settings
from app.messaging.consumer import CatalogEventConsumer
from app.messaging.events import CatalogEvent
from app.messaging.retry import PermanentEventError, RetryableEventError, classify_processing_error
from app.services.errors import ImageDownloadError, ImagePipelineError


def event_payload(**overrides):
    payload = {
        "eventId": str(uuid4()),
        "eventType": "PRODUCT_IMAGE_CREATED",
        "eventVersion": 1,
        "productId": str(uuid4()),
        "imageId": str(uuid4()),
        "occurredAt": datetime.now(UTC).isoformat(),
        "traceId": "test-trace",
    }
    payload.update(overrides)
    return payload


def test_event_contract_rejects_wrong_version_and_missing_image_id():
    with pytest.raises(ValidationError, match="version"):
        CatalogEvent.model_validate(event_payload(eventVersion=2))
    with pytest.raises(ValidationError, match="imageId"):
        CatalogEvent.model_validate(event_payload(imageId=None))


def test_error_classification_distinguishes_retryable_and_permanent():
    assert isinstance(classify_processing_error(ImageDownloadError("network")), RetryableEventError)
    assert isinstance(classify_processing_error(ImagePipelineError("bad image")), PermanentEventError)


class FakeIncomingMessage:
    def __init__(self, payload: dict, headers=None):
        self.body = json.dumps(payload).encode()
        self.headers = headers or {}
        self.content_type = "application/json"
        self.correlation_id = "correlation"
        self.acked = False

    async def ack(self):
        self.acked = True


class FakeExchange:
    def __init__(self):
        self.published = []

    async def publish(self, message, routing_key, mandatory):
        self.published.append((message, routing_key, mandatory))


def test_consumer_acks_successful_event():
    async def scenario():
        handled = []

        async def handler(event):
            handled.append(event)

        consumer = CatalogEventConsumer(Settings(), handler)
        message = FakeIncomingMessage(event_payload())
        await consumer.process_message(message)
        assert message.acked
        assert len(handled) == 1

    asyncio.run(scenario())


def test_consumer_routes_transient_failures_through_retry_then_dlq():
    async def scenario():
        async def handler(_event):
            raise RetryableEventError("temporary")

        settings = Settings(rabbitmq_retry_queues="retry-a,retry-b", rabbitmq_dlq="dead")
        consumer = CatalogEventConsumer(settings, handler)
        exchange = FakeExchange()
        consumer.channel = SimpleNamespace(default_exchange=exchange)

        first = FakeIncomingMessage(event_payload())
        await consumer.process_message(first)
        assert first.acked
        assert exchange.published[0][1] == "retry-a"
        assert exchange.published[0][0].headers["x-visual-retry-count"] == 1

        exhausted = FakeIncomingMessage(event_payload(), {"x-visual-retry-count": 2})
        await consumer.process_message(exhausted)
        assert exhausted.acked
        assert exchange.published[1][1] == "dead"

    asyncio.run(scenario())


def test_consumer_routes_invalid_contract_directly_to_dlq():
    async def scenario():
        consumer = CatalogEventConsumer(Settings(rabbitmq_dlq="dead"), lambda _event: None)
        exchange = FakeExchange()
        consumer.channel = SimpleNamespace(default_exchange=exchange)
        message = FakeIncomingMessage(event_payload(eventVersion=9))
        await consumer.process_message(message)
        assert message.acked
        assert exchange.published[0][1] == "dead"

    asyncio.run(scenario())
