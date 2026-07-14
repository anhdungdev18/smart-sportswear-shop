from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import router
from app.config.settings import settings
from app.observability.trace_logger import get_logger, setup_logging
from app.tools.registry import setup_registry
from app.db import pool as db_pool
from app.memory.store_factory import init_store


@asynccontextmanager
async def lifespan(app: FastAPI):
    setup_logging(settings.LOG_LEVEL)
    logger = get_logger(__name__)

    # DB pool (Phase 3+) — skipped gracefully if DB_READ_URL is empty
    if settings.DB_READ_URL:
        await db_pool.init_pool(settings.DB_READ_URL)
        logger.info("chatbot-service | db_pool=ready")
    else:
        logger.warning("chatbot-service | DB_READ_URL not set — product search will return empty")

    if settings.DB_WRITE_URL:
        await db_pool.init_write_pool(settings.DB_WRITE_URL)
        logger.info("chatbot-service | db_write_pool=ready")
    else:
        logger.warning("chatbot-service | DB_WRITE_URL not set — durable chat history disabled")

    # Session store (Phase 9) — Redis if configured, else in-memory fallback
    await init_store(
        redis_url=settings.REDIS_URL,
        ttl=settings.SESSION_TTL_SECONDS,
    )

    setup_registry()

    # Catalog context — auto-generate từ DB và cache vào file
    if settings.DB_READ_URL:
        try:
            from app.services.catalog_context import refresh_catalog_context
            await refresh_catalog_context()
        except Exception as _cat_err:
            logger.warning(f"chatbot-service | catalog_context refresh failed: {_cat_err!r}")

    logger.info(
        f"chatbot-service starting | env={settings.CHATBOT_ENV} "
        f"host={settings.CHATBOT_HOST} port={settings.CHATBOT_PORT}"
    )
    yield

    from app.graph.nodes.save_result import drain_pending_saves
    await drain_pending_saves()
    await db_pool.close_pool()
    logger.info("chatbot-service shutting down")


app = FastAPI(
    title="chatbot-service",
    description="AI chatbot service — Phase 10",
    version="0.10.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ALLOWED_ORIGINS.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)
