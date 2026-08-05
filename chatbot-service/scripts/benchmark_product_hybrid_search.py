"""Run the versioned 60-query product-search acceptance benchmark."""
from __future__ import annotations

import argparse
import asyncio
import json
import statistics
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from app.config.settings import settings
from app.db import pool
from app.schemas.internal_product_search import InternalSearchRequest
from app.services.product_search_service import search_internal


def percentile(values: list[float], percent: float) -> float:
    if not values:
        return 0
    ordered = sorted(values)
    return ordered[min(len(ordered) - 1, round((len(ordered) - 1) * percent))]


async def execute(case: dict) -> dict:
    started = time.perf_counter()
    if not case["query"].strip():
        return {"ids": [], "parsed": {}, "latencyMs": 0, "mode": "VALIDATION"}
    response = await search_internal(
        InternalSearchRequest(query=case["query"], page=1, limit=5, filters={})
    )
    return {
        "ids": [str(item["productId"]) for item in response["items"]],
        "items": response["items"],
        "parsed": response["parsedQuery"],
        "latencyMs": round((time.perf_counter() - started) * 1000),
        "mode": response["searchMode"],
    }


async def run(dataset_path: Path, warm_runs: int) -> dict:
    dataset = json.loads(dataset_path.read_text(encoding="utf-8"))
    await pool.init_pool(settings.DB_READ_URL)
    try:
        results = []
        for case in dataset["cases"]:
            result = None
            samples = []
            for _ in range(warm_runs):
                result = await execute(case)
                samples.append(result["latencyMs"])
            assert result is not None
            expected = set(case["expectedProductIds"])
            returned = set(result["ids"])
            filters_ok = all(
                result["parsed"].get(key) == value
                for key, value in case["requiredFilters"].items()
            )
            recall_hit = not expected or bool(expected & returned)
            zero_ok = case["type"] != "zero" or not returned
            results.append({
                "id": case["id"],
                "type": case["type"],
                "query": case["query"],
                "returnedIds": result["ids"],
                "returnedItems": result.get("items", []),
                "filtersOk": filters_ok,
                "recallHit": recall_hit,
                "zeroOk": zero_ok,
                "mode": result["mode"],
                "latencyMs": samples[-1],
                "latencySamplesMs": samples,
            })
    finally:
        await pool.close_pool()

    def rate(cases: list[dict], key: str) -> float:
        return round(100 * sum(bool(item[key]) for item in cases) / len(cases), 2) if cases else 100.0

    structured = [r for r in results if r["type"] in {"structured", "typo"} and
                  next(c for c in dataset["cases"] if c["id"] == r["id"])["requiredFilters"]]
    exact = [r for r in results if r["type"] == "exact"]
    semantic = [r for r in results if r["type"] == "semantic"]
    zero = [r for r in results if r["type"] == "zero"]
    cold_latencies = [r["latencySamplesMs"][0] for r in results if r["latencySamplesMs"][0] > 0]
    warm_latencies = [r["latencySamplesMs"][-1] for r in results if r["latencySamplesMs"][-1] > 0]
    summary = {
        "datasetVersion": dataset["version"],
        "caseCount": len(results),
        "structuredFilterPrecisionPercent": rate(structured, "filtersOk"),
        "exactRecallAt5Percent": rate(exact, "recallHit"),
        "semanticRecallAt5Percent": rate(semantic, "recallHit"),
        "zeroResultPrecisionPercent": rate(zero, "zeroOk"),
        "coldLatencyP50Ms": round(statistics.median(cold_latencies)) if cold_latencies else 0,
        "coldLatencyP95Ms": round(percentile(cold_latencies, .95)),
        "warmLatencyP50Ms": round(statistics.median(warm_latencies)) if warm_latencies else 0,
        "warmLatencyP95Ms": round(percentile(warm_latencies, .95)),
    }
    summary["passed"] = (
        summary["structuredFilterPrecisionPercent"] == 100
        and summary["exactRecallAt5Percent"] >= 95
        and summary["semanticRecallAt5Percent"] >= 80
        and summary["zeroResultPrecisionPercent"] == 100
        and summary["coldLatencyP95Ms"] <= 3000
        and summary["warmLatencyP95Ms"] <= 1500
    )
    return {"summary": summary, "results": results}


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--dataset",
        type=Path,
        default=ROOT / "benchmarks" / "product_hybrid_search_v1.json",
    )
    parser.add_argument("--warm-runs", type=int, default=2)
    parser.add_argument("--keyword-only", action="store_true")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if args.keyword_only:
        settings.PRODUCT_SEARCH_SEMANTIC_ENABLED = False
    report = asyncio.run(run(args.dataset, max(1, args.warm_runs)))
    rendered = json.dumps(report, ensure_ascii=False, indent=2)
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered + "\n", encoding="utf-8")
    print(json.dumps(report["summary"], ensure_ascii=False))
    raise SystemExit(0 if report["summary"]["passed"] else 1)


if __name__ == "__main__":
    main()
