from fastapi import APIRouter, Query

from app.memory.session_store import list_runs

router = APIRouter(prefix="/runs")


@router.get("")
async def runs(limit: int = Query(default=50, ge=1, le=100)) -> dict:
    return {"items": list_runs(limit)}
