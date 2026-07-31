"""
Admin API – internal endpoints for coverage, usage, retry and job history.
All endpoints require the X-Internal-Service-Token header; never expose to
public internet in production.
"""
import hmac
from datetime import date
from uuid import UUID

from fastapi import APIRouter, Depends, Header, HTTPException, Query, status
from pydantic import BaseModel

from app.config import Settings, get_settings
from app.persistence.repository import (
    CoverageStats,
    RecentJob,
    RepositoryUnavailableError,
    UsageDayStat,
    VisualSearchRepository,
)
from app.services.indexing_jobs import IndexingJobService

router = APIRouter(prefix="/internal/v1/admin", tags=["admin"])


def get_repository(settings: Settings = Depends(get_settings)) -> VisualSearchRepository:
    return VisualSearchRepository(settings)


# ---------------------------------------------------------------------------
# Auth dependency
# ---------------------------------------------------------------------------


def require_internal_token(
    x_internal_service_token: str = Header(default=""),
    settings: Settings = Depends(get_settings),
) -> None:
    if not settings.internal_service_token or not hmac.compare_digest(
        x_internal_service_token, settings.internal_service_token
    ):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid internal service token")


# ---------------------------------------------------------------------------
# Response models
# ---------------------------------------------------------------------------


class CoverageResponse(BaseModel):
    total_active_images: int
    ready: int
    pending: int
    processing: int
    failed: int
    missing: int
    coverage_pct: float


class UsageDayStatResponse(BaseModel):
    day: date
    operation: str
    requests: int
    image_pixels: int
    text_tokens: int
    estimated_cost_usd: float
    success_count: int
    failure_count: int


class UsageResponse(BaseModel):
    days: int
    rows: list[UsageDayStatResponse]


class RecentJobResponse(BaseModel):
    id: str
    job_type: str
    status: str
    total_count: int
    completed_count: int
    failed_count: int
    pending_count: int
    created_at: str
    completed_at: str | None


class JobsResponse(BaseModel):
    jobs: list[RecentJobResponse]


class RetryFailedResponse(BaseModel):
    job_id: str
    enqueued_count: int


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------


@router.get(
    "/coverage",
    response_model=CoverageResponse,
    dependencies=[Depends(require_internal_token)],
    summary="Embedding coverage for the active model",
)
async def get_coverage(
    settings: Settings = Depends(get_settings),
    repo: VisualSearchRepository = Depends(get_repository),
) -> CoverageResponse:
    if not settings.visual_search_enabled:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Visual search is disabled")
    try:
        stats: CoverageStats = await repo.coverage_stats()
    except RepositoryUnavailableError as exc:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database unavailable") from exc
    return CoverageResponse(
        total_active_images=stats.total_active_images,
        ready=stats.ready,
        pending=stats.pending,
        processing=stats.processing,
        failed=stats.failed,
        missing=stats.missing,
        coverage_pct=stats.coverage_pct,
    )


@router.get(
    "/usage",
    response_model=UsageResponse,
    dependencies=[Depends(require_internal_token)],
    summary="AI API usage and estimated cost",
)
async def get_usage(
    days: int = Query(default=30, ge=1, le=365),
    settings: Settings = Depends(get_settings),
    repo: VisualSearchRepository = Depends(get_repository),
) -> UsageResponse:
    if not settings.visual_search_enabled:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Visual search is disabled")
    try:
        rows: list[UsageDayStat] = await repo.usage_stats(days)
    except RepositoryUnavailableError as exc:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database unavailable") from exc
    return UsageResponse(
        days=days,
        rows=[
            UsageDayStatResponse(
                day=row.day,
                operation=row.operation,
                requests=row.requests,
                image_pixels=row.image_pixels,
                text_tokens=row.text_tokens,
                estimated_cost_usd=row.estimated_cost_usd,
                success_count=row.success_count,
                failure_count=row.failure_count,
            )
            for row in rows
        ],
    )


@router.get(
    "/jobs",
    response_model=JobsResponse,
    dependencies=[Depends(require_internal_token)],
    summary="Recent indexing/backfill/reconciliation jobs",
)
async def get_jobs(
    limit: int = Query(default=10, ge=1, le=50),
    settings: Settings = Depends(get_settings),
    repo: VisualSearchRepository = Depends(get_repository),
) -> JobsResponse:
    if not settings.visual_search_enabled:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Visual search is disabled")
    try:
        jobs: list[RecentJob] = await repo.recent_jobs(limit)
    except RepositoryUnavailableError as exc:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database unavailable") from exc
    return JobsResponse(
        jobs=[
            RecentJobResponse(
                id=str(job.id),
                job_type=job.job_type,
                status=job.status,
                total_count=job.total_count,
                completed_count=job.completed_count,
                failed_count=job.failed_count,
                pending_count=job.pending_count,
                created_at=job.created_at.isoformat(),
                completed_at=job.completed_at.isoformat() if job.completed_at else None,
            )
            for job in jobs
        ]
    )


@router.post(
    "/retry-failed",
    response_model=RetryFailedResponse,
    dependencies=[Depends(require_internal_token)],
    summary="Enqueue a RECONCILIATION job for all FAILED embeddings",
)
async def retry_failed(
    settings: Settings = Depends(get_settings),
    repo: VisualSearchRepository = Depends(get_repository),
) -> RetryFailedResponse:
    if not settings.visual_search_enabled:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Visual search is disabled")
    try:
        service = IndexingJobService(repo)
        report = await service.inspect(include_failed=True)
        failed_only_report = report.__class__(
            total=sum(1 for c in report.candidates if c.reason == "FAILED"),
            reasons={"FAILED": sum(1 for c in report.candidates if c.reason == "FAILED")},
            candidates=tuple(c for c in report.candidates if c.reason == "FAILED"),
        )
        if failed_only_report.total == 0:
            return RetryFailedResponse(job_id="", enqueued_count=0)
        job = await service.enqueue("RECONCILIATION", failed_only_report)
    except RepositoryUnavailableError as exc:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database unavailable") from exc
    except RuntimeError as exc:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(exc)) from exc
    return RetryFailedResponse(job_id=str(job.id), enqueued_count=job.total_count)
