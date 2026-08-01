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


async def run(dry_run: bool, limit: int | None) -> None:
    service = IndexingJobService(VisualSearchRepository(Settings()))
    report = await service.inspect(include_failed=False)
    summary = {"dryRun": dry_run, "candidateCount": report.total, "reasons": report.reasons}
    if not dry_run:
        job = await service.enqueue("BACKFILL", report, limit=limit)
        summary["jobId"] = str(job.id)
        summary["enqueuedCount"] = job.total_count
    print(json.dumps(summary, indent=2, sort_keys=True))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Inspect or enqueue ACTIVE catalog images through the outbox.")
    parser.add_argument("--enqueue", action="store_true", help="Create a BACKFILL job and outbox events.")
    parser.add_argument("--limit", type=int, default=None, help="Optional maximum number of images to enqueue.")
    args = parser.parse_args()
    asyncio.run(run(dry_run=not args.enqueue, limit=args.limit))
