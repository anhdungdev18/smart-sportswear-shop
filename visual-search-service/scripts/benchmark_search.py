import argparse
import asyncio
from dataclasses import asdict
import json
from pathlib import Path
import sys
import time
from typing import Any

import httpx
import psycopg

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.config import Settings
from app.services.benchmark import BenchmarkObservation, evaluate_benchmark


ALLOWED_CONTENT_TYPES = {".jpg": "image/jpeg", ".jpeg": "image/jpeg", ".png": "image/png", ".webp": "image/webp"}
STRATUM_ALIASES = {
    "top": "tops", "tops": "tops", "shirt": "tops", "ao": "tops",
    "bottom": "bottoms", "bottoms": "bottoms", "pants": "bottoms", "quan": "bottoms",
    "shoe": "shoes", "shoes": "shoes", "footwear": "shoes", "giay": "shoes",
    "accessory": "accessories", "accessories": "accessories", "phu-kien": "accessories",
}


def normalize_stratum(value: str) -> str:
    normalized = value.strip().lower().replace("_", "-")
    if normalized.startswith("ao-"):
        return "tops"
    if normalized.startswith("quan-"):
        return "bottoms"
    if normalized.startswith("giay-"):
        return "shoes"
    if normalized.startswith("phu-kien"):
        return "accessories"
    return STRATUM_ALIASES.get(normalized, normalized)


def load_manifest(path: Path) -> tuple[Path, list[dict[str, Any]]]:
    root = path.resolve().parent
    payload = json.loads(path.read_text(encoding="utf-8"))
    samples = payload.get("samples")
    if not isinstance(samples, list):
        raise ValueError("manifest.samples must be an array")
    return root, samples


def resolve_image(root: Path, value: str) -> Path:
    candidate = (root / value).resolve()
    if root != candidate and root not in candidate.parents:
        raise ValueError("image path must stay inside the manifest directory")
    if not candidate.is_file() or candidate.suffix.lower() not in ALLOWED_CONTENT_TYPES:
        raise ValueError(f"unsupported or missing benchmark image: {value}")
    return candidate


async def product_strata(database_url: str, product_ids: set[str]) -> dict[str, str]:
    if not product_ids:
        return {}
    async with await psycopg.AsyncConnection.connect(database_url, connect_timeout=10) as connection:
        async with connection.cursor() as cursor:
            await cursor.execute(
                "select p.id::text, c.slug from products p join categories c on c.id = p.category_id where p.id = any(%s::uuid[])",
                (list(product_ids),),
            )
            return {row[0]: normalize_stratum(str(row[1])) for row in await cursor.fetchall()}


async def run(manifest_path: Path, service_url: str, output: Path | None, interval_seconds: float) -> int:
    settings = Settings()
    root, samples = load_manifest(manifest_path)
    raw_results: list[dict[str, Any]] = []
    returned_ids: set[str] = set()
    async with httpx.AsyncClient(timeout=60) as client:
        for index, sample in enumerate(samples, start=1):
            if index > 1 and interval_seconds > 0:
                await asyncio.sleep(interval_seconds)
            image_path = resolve_image(root, str(sample["image"]))
            expected_ids = frozenset(str(item) for item in sample["expectedProductIds"])
            expected_stratum = normalize_stratum(str(sample["stratum"]))
            started = time.perf_counter()
            response = await client.post(
                    f"{service_url.rstrip('/')}/internal/v1/search?limit=20",
                    headers={"X-Internal-Service-Token": settings.internal_service_token},
                    files={"image": (image_path.name, image_path.read_bytes(), ALLOWED_CONTENT_TYPES[image_path.suffix.lower()])},
                )
            latency_ms = (time.perf_counter() - started) * 1000
            response.raise_for_status()
            ids = tuple(item["product_id"] for item in response.json()["candidates"])
            returned_ids.update(ids[:1])
            raw_results.append({
                "sampleId": str(sample.get("id", index)), "expected": expected_ids,
                "expectedStratum": expected_stratum, "returned": ids, "latencyMs": latency_ms,
            })
            print(f"completed {index}/{len(samples)} latencyMs={latency_ms:.2f}", file=sys.stderr, flush=True)

    strata_by_product = await product_strata(settings.database_url, returned_ids)
    observations = [
        BenchmarkObservation(
            expected_product_ids=item["expected"], expected_stratum=item["expectedStratum"],
            returned_product_ids=item["returned"],
            returned_stratum=strata_by_product.get(item["returned"][0]) if item["returned"] else None,
            latency_ms=item["latencyMs"],
        ) for item in raw_results
    ]
    metrics = evaluate_benchmark(observations)
    report = {
        "manifest": str(manifest_path), "serviceUrl": service_url,
        "criteria": {
            "minimumSamples": 100,
            "requiredStrata": ["tops", "bottoms", "shoes"],
            "notApplicableStrata": {"accessories": "No ACTIVE accessory product with an image exists in the audited catalog"},
            "recallAt5": 0.80, "categoryAccuracy": 0.90, "p95Ms": 3000,
        },
        "metrics": asdict(metrics),
        "samples": [
            {"id": item["sampleId"], "latencyMs": round(item["latencyMs"], 2), "returnedProductIds": list(item["returned"][:5])}
            for item in raw_results
        ],
    }
    rendered = json.dumps(report, ensure_ascii=False, indent=2)
    print(rendered)
    if output:
        output.write_text(rendered + "\n", encoding="utf-8")
    return 0 if metrics.passed else 2


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Run the external-image visual-search acceptance benchmark.")
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--service-url", default="http://127.0.0.1:8090")
    parser.add_argument("--output", type=Path)
    parser.add_argument(
        "--interval-seconds", type=float, default=21,
        help="Pace requests outside the measured latency window to respect provider RPM limits.",
    )
    args = parser.parse_args()
    raise SystemExit(asyncio.run(run(args.manifest, args.service_url, args.output, args.interval_seconds)))
