from fastapi import FastAPI

from app.api.admin import router as admin_router
from app.api.health import router as health_router
from app.api.search import router as search_router
from app.config import get_settings


def create_app() -> FastAPI:
    settings = get_settings()
    application = FastAPI(title="Visual Product Search", version="0.1.0")
    application.include_router(health_router)
    application.include_router(search_router)
    application.include_router(admin_router)
    return application


app = create_app()
