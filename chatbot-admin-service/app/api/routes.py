from fastapi import APIRouter

from app.api.chat import router as chat_router
from app.api.config import router as config_router
from app.api.health import router as health_router
from app.api.runs import router as runs_router

router = APIRouter()
router.include_router(health_router)
router.include_router(chat_router)
router.include_router(runs_router)
router.include_router(config_router)
