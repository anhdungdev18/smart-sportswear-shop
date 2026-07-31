import argparse
import asyncio
import json
import sys
from pathlib import Path

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.config import Settings
from app.persistence.repository import VisualSearchRepository
from app.services.indexing_jobs import IndexingJobService


async def run(
    enqueue: bool,
    include_failed: bool,
    retryable_failed_only: bool,
    timeout_minutes: int,
    limit: int | None,
) -> None:
    service = IndexingJobService(VisualSearchRepository(Settings()))
    report = await service.inspect(
        include_failed=include_failed or retryable_failed_only,
        processing_timeout_minutes=timeout_minutes,
        retryable_failed_only=retryable_failed_only,
    )
    summary = {"dryRun": not enqueue, "candidateCount": report.total, "reasons": report.reasons}
    if enqueue:
        job = await service.enqueue("RECONCILIATION", report, limit=limit)
        summary["jobId"] = str(job.id)
        summary["enqueuedCount"] = job.total_count
    print(json.dumps(summary, indent=2, sort_keys=True))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Find missing/stale/failed visual embeddings.")
    parser.add_argument("--enqueue", action="store_true", help="Create a RECONCILIATION job and outbox events.")
    parser.add_argument("--include-failed", action="store_true")
    parser.add_argument(
        "--retryable-failed-only",
        action="store_true",
        help="Select only transient FAILED rows (currently Voyage 429/5xx exhaustion).",
    )
    parser.add_argument("--processing-timeout-minutes", type=int, default=15)
    parser.add_argument("--limit", type=int, default=None)
    args = parser.parse_args()
    asyncio.run(
        run(
            args.enqueue,
            args.include_failed,
            args.retryable_failed_only,
            args.processing_timeout_minutes,
            args.limit,
        )
    )
