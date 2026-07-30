from fastapi import APIRouter, Depends

from app.config import Settings, get_settings

router = APIRouter(tags=["health"])


@router.get("/health/live")
async def live(settings: Settings = Depends(get_settings)) -> dict[str, str]:
    return {"status": "ok", "service": settings.service_name}


@router.get("/health/ready")
async def ready(settings: Settings = Depends(get_settings)) -> dict[str, object]:
    return {
        "status": "ready" if settings.visual_search_enabled else "disabled",
        "enabled": settings.visual_search_enabled,
        "provider": settings.visual_embedding_provider,
    }
