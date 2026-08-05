from types import SimpleNamespace
from uuid import uuid4

import pytest

from app.indexing.consumer import ProductIndexingConsumer
from app.indexing.events import CatalogEvent


def test_catalog_event_rejects_visual_only_event():
    with pytest.raises(ValueError):
        CatalogEvent.model_validate(
            {
                "eventId": str(uuid4()),
                "eventType": "PRODUCT_IMAGE_CREATED",
                "eventVersion": 1,
                "productId": str(uuid4()),
                "occurredAt": "2026-08-05T00:00:00Z",
                "traceId": "trace",
            }
        )


class FakeExchange:
    def __init__(self):
        self.published = []

    async def publish(self, message, routing_key, mandatory):
        self.published.append((message, routing_key, mandatory))


class FakeMessage:
    def __init__(self, headers=None):
        self.body = b"{}"
        self.headers = headers or {}
        self.content_type = "application/json"
        self.correlation_id = "correlation"
        self.acked = False

    async def ack(self):
        self.acked = True


@pytest.mark.asyncio
async def test_retry_then_dlq_routing():
    settings = SimpleNamespace(
        PRODUCT_SEARCH_INDEXING_RETRY_QUEUES="retry.once",
        PRODUCT_SEARCH_INDEXING_DLQ="indexing.dlq",
    )
    consumer = ProductIndexingConsumer(settings, None)
    exchange = FakeExchange()
    consumer.channel = SimpleNamespace(default_exchange=exchange)

    first = FakeMessage()
    await consumer._route(first, permanent=False, error_type="TimeoutError")
    assert exchange.published[0][1] == "retry.once"
    assert first.acked

    exhausted = FakeMessage({"x-product-search-retry-count": 1})
    await consumer._route(exhausted, permanent=False, error_type="TimeoutError")
    assert exchange.published[1][1] == "indexing.dlq"


@pytest.mark.asyncio
async def test_permanent_error_goes_directly_to_dlq():
    settings = SimpleNamespace(
        PRODUCT_SEARCH_INDEXING_RETRY_QUEUES="retry.once",
        PRODUCT_SEARCH_INDEXING_DLQ="indexing.dlq",
    )
    consumer = ProductIndexingConsumer(settings, None)
    exchange = FakeExchange()
    consumer.channel = SimpleNamespace(default_exchange=exchange)
    message = FakeMessage()

    await consumer._route(message, permanent=True, error_type="InvalidEvent")
    assert exchange.published[0][1] == "indexing.dlq"
