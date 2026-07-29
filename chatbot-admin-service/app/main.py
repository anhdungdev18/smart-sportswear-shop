from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import router
from app.config.settings import settings
from app.observability.trace_logger import get_logger, setup_logging


@asynccontextmanager
async def lifespan(app: FastAPI):
    setup_logging()
    get_logger(__name__).info("chatbot-admin-service starting env=%s", settings.ADMIN_COPILOT_ENV)
    yield
    get_logger(__name__).info("chatbot-admin-service shutting down")


app = FastAPI(
    title="chatbot-admin-service",
    description="Admin Copilot read-only service for inventory intelligence",
    version="0.5.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[origin.strip() for origin in settings.CORS_ALLOWED_ORIGINS.split(",") if origin.strip()],
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

app.include_router(router)
