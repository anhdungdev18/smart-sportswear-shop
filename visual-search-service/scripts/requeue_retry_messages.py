import argparse
import asyncio
import sys
from pathlib import Path

import aio_pika
from aio_pika import DeliveryMode, Message

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.config import Settings


async def run(source: str, maximum: int, execute: bool) -> None:
    settings = Settings()
    allowed_sources = (*settings.retry_queues, settings.rabbitmq_dlq)
    if source not in allowed_sources:
        raise SystemExit(f"Source must be one of: {', '.join(allowed_sources)}")
    connection = await aio_pika.connect_robust(settings.rabbitmq_url)
    try:
        channel = await connection.channel(publisher_confirms=True)
        queue = await channel.declare_queue(source, passive=True)
        available = queue.declaration_result.message_count
        print({"source": source, "available": available, "execute": execute, "maximum": maximum})
        if not execute:
            return
        moved = 0
        while moved < maximum:
            incoming = await queue.get(fail=False, no_ack=False)
            if incoming is None:
                break
            headers = dict(incoming.headers or {})
            headers.pop("x-visual-retry-count", None)
            outgoing = Message(
                body=incoming.body,
                headers=headers,
                content_type=incoming.content_type or "application/json",
                delivery_mode=DeliveryMode.PERSISTENT,
                correlation_id=incoming.correlation_id,
            )
            await channel.default_exchange.publish(
                outgoing, routing_key=settings.rabbitmq_consumer_queue, mandatory=True
            )
            await incoming.ack()
            moved += 1
        print({"moved": moved, "destination": settings.rabbitmq_consumer_queue})
    finally:
        await connection.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Move bounded messages from a retry queue back to main.")
    parser.add_argument("source")
    parser.add_argument("--max", type=int, default=25)
    parser.add_argument("--execute", action="store_true")
    args = parser.parse_args()
    asyncio.run(run(args.source, args.max, args.execute))
