from dataclasses import dataclass
from typing import Any
from uuid import UUID, uuid4

import psycopg
from psycopg.errors import OperationalError

from app.config import Settings
from app.providers.base import EmbeddingResult


class RepositoryUnavailableError(RuntimeError):
    pass


@dataclass(frozen=True, slots=True)
class ModelVersion:
    id: UUID
    provider: str
    model: str
    dimensions: int


@dataclass(frozen=True, slots=True)
class CatalogImage:
    id: UUID
    product_id: UUID
    image_url: str
    public_id: str | None
    product_status: str


@dataclass(frozen=True, slots=True)
class ExistingEmbedding:
    status: str
    image_hash: str | None


class VisualSearchRepository:
    def __init__(self, settings: Settings):
        self.database_url = settings.database_url

    async def _connect(self) -> psycopg.AsyncConnection[Any]:
        try:
            return await psycopg.AsyncConnection.connect(self.database_url, connect_timeout=10)
        except OperationalError as exc:
            raise RepositoryUnavailableError("Visual search database is unavailable") from exc

    async def is_processed(self, event_id: UUID) -> bool:
        async with await self._connect() as connection:
            async with connection.cursor() as cursor:
                await cursor.execute(
                    "select exists(select 1 from visual_search.processed_events where event_id = %s)",
                    (event_id,),
                )
                return bool((await cursor.fetchone())[0])

    async def active_model(self) -> ModelVersion | None:
        async with await self._connect() as connection:
            async with connection.cursor() as cursor:
                await cursor.execute(
                    "select id, provider, model, dimensions from visual_search.model_versions where status = 'ACTIVE'"
                )
                row = await cursor.fetchone()
                return ModelVersion(*row) if row else None

    async def get_image(self, image_id: UUID) -> CatalogImage | None:
        async with await self._connect() as connection:
            async with connection.cursor() as cursor:
                await cursor.execute(
                    """
                    select pi.id, pi.product_id, pi.image_url, pi.public_id, p.status
                    from product_images pi join products p on p.id = pi.product_id
                    where pi.id = %s
                    """,
                    (image_id,),
                )
                row = await cursor.fetchone()
                return CatalogImage(*row) if row else None

    async def product_image_ids(self, product_id: UUID) -> list[UUID]:
        async with await self._connect() as connection:
            async with connection.cursor() as cursor:
                await cursor.execute("select id from product_images where product_id = %s order by created_at", (product_id,))
                return [row[0] for row in await cursor.fetchall()]

    async def existing_embedding(self, image_id: UUID, model_id: UUID) -> ExistingEmbedding | None:
        async with await self._connect() as connection:
            async with connection.cursor() as cursor:
                await cursor.execute(
                    "select status, image_hash from visual_search.image_embeddings where image_id = %s and model_version_id = %s",
                    (image_id, model_id),
                )
                row = await cursor.fetchone()
                return ExistingEmbedding(*row) if row else None

    async def mark_processing(self, image: CatalogImage, model_id: UUID) -> bool:
        async with await self._connect() as connection:
            async with connection.transaction():
                async with connection.cursor() as cursor:
                    await cursor.execute(
                        """
                        insert into visual_search.image_embeddings
                            (image_id, product_id, model_version_id, status, attempts, last_attempt_at)
                        values (%s, %s, %s, 'PROCESSING', 1, now())
                        on conflict (image_id, model_version_id) do update
                        set status = 'PROCESSING', attempts = visual_search.image_embeddings.attempts + 1,
                            last_attempt_at = now(), last_error = null, updated_at = now()
                        where visual_search.image_embeddings.status <> 'PROCESSING'
                           or visual_search.image_embeddings.last_attempt_at < now() - interval '15 minutes'
                        returning image_id
                        """,
                        (image.id, image.product_id, model_id),
                    )
                    return await cursor.fetchone() is not None

    async def mark_ready_and_processed(
        self,
        event_id: UUID | None,
        event_type: str | None,
        event_version: int | None,
        image: CatalogImage,
        model: ModelVersion,
        image_hash: str,
        result: EmbeddingResult,
        latency_ms: int,
    ) -> None:
        vector = "[" + ",".join(str(value) for value in result.vector) + "]"
        async with await self._connect() as connection:
            async with connection.transaction():
                async with connection.cursor() as cursor:
                    await cursor.execute(
                        """
                        update visual_search.image_embeddings
                        set embedding = %s::vector, image_hash = %s, status = 'READY', last_error = null,
                            ready_at = now(), updated_at = now()
                        where image_id = %s and model_version_id = %s
                        """,
                        (vector, image_hash, image.id, model.id),
                    )
                    await cursor.execute(
                        """
                        insert into visual_search.usage_events
                            (id, provider, model, operation, image_pixels, text_tokens, latency_ms, success)
                        values (%s, %s, %s, 'DOCUMENT_EMBEDDING', %s, %s, %s, true)
                        """,
                        (uuid4(), model.provider, model.model, result.usage.image_pixels, result.usage.text_tokens, latency_ms),
                    )
                    if event_id is not None and event_type is not None and event_version is not None:
                        await self._insert_processed(cursor, event_id, event_type, event_version)

    async def mark_processed(self, event_id: UUID, event_type: str, event_version: int) -> None:
        async with await self._connect() as connection:
            async with connection.transaction():
                async with connection.cursor() as cursor:
                    await self._insert_processed(cursor, event_id, event_type, event_version)

    @staticmethod
    async def _insert_processed(cursor: Any, event_id: UUID, event_type: str, event_version: int) -> None:
        await cursor.execute(
            """
            insert into visual_search.processed_events(event_id, event_type, event_version)
            values (%s, %s, %s) on conflict (event_id) do nothing
            """,
            (event_id, event_type, event_version),
        )

    async def mark_failed(self, image_id: UUID | None, model_id: UUID | None, error: str) -> None:
        if image_id is None or model_id is None:
            return
        async with await self._connect() as connection:
            async with connection.transaction():
                async with connection.cursor() as cursor:
                    await cursor.execute(
                        """
                        insert into visual_search.image_embeddings
                            (image_id, product_id, model_version_id, status, attempts, last_error, last_attempt_at)
                        select pi.id, pi.product_id, %s, 'FAILED', 1, %s, now()
                        from product_images pi where pi.id = %s
                        on conflict (image_id, model_version_id) do update
                        set status = 'FAILED', last_error = excluded.last_error, updated_at = now()
                        """,
                        (model_id, error[:2000], image_id),
                    )
