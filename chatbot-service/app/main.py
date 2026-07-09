from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.api.routes import router
from app.config.settings import settings
from app.observability.trace_logger import get_logger, setup_logging


@asynccontextmanager
async def lifespan(app: FastAPI):
    setup_logging(settings.LOG_LEVEL)
    logger = get_logger(__name__)
    logger.info(
        f"chatbot-service starting | env={settings.CHATBOT_ENV} "
        f"host={settings.CHATBOT_HOST} port={settings.CHATBOT_PORT}"
    )
    yield
    logger.info("chatbot-service shutting down")


app = FastAPI(
    title="chatbot-service",
    description="AI chatbot service — Phase 0 scaffold",
    version="0.1.0",
    lifespan=lifespan,
)

app.include_router(router)
