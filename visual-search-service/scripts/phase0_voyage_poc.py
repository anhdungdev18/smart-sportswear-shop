"""Run the reproducible Phase-0 catalog audit and Voyage retrieval POC.

The report contains URLs, aggregate metrics and timings only. Image bytes,
embeddings, credentials and database connection details are never persisted.
"""

from __future__ import annotations

import argparse
import base64
import io
import json
import math
import os
import socket
import time
from collections import Counter
from datetime import UTC, datetime
from pathlib import Path
from typing import Any
from urllib.parse import quote_plus, urlparse

import httpx
import psycopg
from PIL import Image, ImageOps, UnidentifiedImageError

ROOT = Path(__file__).resolve().parents[2]
SERVICE_ROOT = Path(__file__).resolve().parents[1]
ALLOWED_HOSTS = frozenset({"res.cloudinary.com", "cdn.shopify.com"})
MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024
MAX_PIXELS = 12_000_000
TARGET_SIZE = (1024, 1024)


def load_env(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    if not path.exists():
        return values
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip().strip('"').strip("'")
    return values


def database_dsn(backend_env: dict[str, str]) -> str:
    explicit = backend_env.get("DATABASE_URL") or os.getenv("DATABASE_URL")
    if explicit:
        return explicit
    required = ("DB_HOST", "DB_PORT", "DB_NAME", "DB_USERNAME", "DB_PASSWORD")
    missing = [key for key in required if not backend_env.get(key)]
    if missing:
        raise RuntimeError(f"Missing database configuration keys: {', '.join(missing)}")
    params = backend_env.get("DB_PARAMS", "sslmode=require").lstrip("?")
    return (
        f"postgresql://{quote_plus(backend_env['DB_USERNAME'])}:{quote_plus(backend_env['DB_PASSWORD'])}"
        f"@{backend_env['DB_HOST']}:{backend_env['DB_PORT']}/{backend_env['DB_NAME']}?{params}"
    )


def catalog_audit(connection: psycopg.Connection[Any]) -> dict[str, Any]:
    with connection.cursor() as cursor:
        cursor.execute(
            """
            select
                count(*) as total_products,
                count(*) filter (where status = 'ACTIVE') as active_products,
                count(*) filter (where status = 'INACTIVE') as inactive_products
            from products
            """
        )
        total_products, active_products, inactive_products = cursor.fetchone()
        cursor.execute(
            """
            select
                count(*) as total_images,
                count(*) filter (where p.status = 'ACTIVE') as active_images,
                count(*) filter (where p.status = 'INACTIVE') as inactive_images,
                count(*) filter (where p.status = 'ACTIVE' and pi.image_url like 'https://cdn.shopify.com/%') as active_shopify,
                count(*) filter (where p.status = 'ACTIVE' and pi.image_url like 'https://res.cloudinary.com/%') as active_cloudinary,
                count(*) filter (where pi.public_id is null or btrim(pi.public_id) = '') as missing_public_id,
                count(*) filter (where pi.image_url !~ '^https://') as relative_or_non_https
            from product_images pi join products p on p.id = pi.product_id
            """
        )
        row = cursor.fetchone()
        cursor.execute(
            """
            select
                count(*) filter (where image_count > 0) as products_with_images,
                count(*) filter (where image_count = 0) as products_without_images
            from (
                select p.id, count(pi.id) as image_count
                from products p left join product_images pi on pi.product_id = p.id
                where p.status = 'ACTIVE'
                group by p.id
            ) counts
            """
        )
        with_images, without_images = cursor.fetchone()
    return {
        "totalProducts": total_products,
        "activeProducts": active_products,
        "inactiveProducts": inactive_products,
        "totalImages": row[0],
        "activeImages": row[1],
        "inactiveImages": row[2],
        "activeShopifyImages": row[3],
        "activeCloudinaryImages": row[4],
        "missingPublicId": row[5],
        "relativeOrNonHttpsImages": row[6],
        "activeProductsWithImages": with_images,
        "activeProductsWithoutImages": without_images,
    }


def select_samples(connection: psycopg.Connection[Any], per_source: int) -> list[dict[str, Any]]:
    with connection.cursor() as cursor:
        cursor.execute(
            """
            with candidates as (
                select p.id, p.name, p.gender, c.slug as category_slug,
                       case
                           when pi.image_url like 'https://cdn.shopify.com/%%' then 'shopify'
                           when pi.image_url like 'https://res.cloudinary.com/%%' then 'cloudinary'
                       end as source,
                       array_agg(pi.image_url order by pi.is_primary desc, pi.sort_order, pi.id) as urls
                from products p
                join categories c on c.id = p.category_id
                join product_images pi on pi.product_id = p.id
                where p.status = 'ACTIVE'
                  and (pi.image_url like 'https://cdn.shopify.com/%%'
                       or pi.image_url like 'https://res.cloudinary.com/%%')
                group by p.id, p.name, p.gender, c.slug, source
                having count(*) >= 2
            ), ranked as (
                select *, row_number() over (partition by source order by category_slug, id) as source_rank
                from candidates
            )
            select id, name, gender, category_slug, source, urls
            from ranked where source_rank <= %s
            order by source, source_rank
            """,
            (per_source * 5,),
        )
        return [
            {
                "productId": str(row[0]),
                "name": row[1],
                "gender": row[2],
                "category": row[3],
                "source": row[4],
                "documentUrl": row[5][0],
                "queryUrl": row[5][1],
            }
            for row in cursor.fetchall()
        ]


def assert_public_allowed_host(url: str) -> None:
    parsed = urlparse(url)
    if parsed.scheme != "https" or parsed.hostname not in ALLOWED_HOSTS or parsed.username or parsed.password:
        raise ValueError("URL is not an allowed HTTPS catalog source")
    addresses = socket.getaddrinfo(parsed.hostname, 443, type=socket.SOCK_STREAM)
    import ipaddress

    for address in addresses:
        ip = ipaddress.ip_address(address[4][0])
        if not ip.is_global:
            raise ValueError("Catalog hostname resolved to a non-public address")


def download_and_normalize(client: httpx.Client, url: str) -> tuple[bytes, int, int]:
    assert_public_allowed_host(url)
    with client.stream("GET", url) as response:
        response.raise_for_status()
        content_type = response.headers.get("content-type", "").split(";", 1)[0].lower()
        if content_type not in {"image/jpeg", "image/png", "image/webp"}:
            raise ValueError(f"Unsupported image content type: {content_type or 'missing'}")
        content_length = response.headers.get("content-length")
        if content_length and int(content_length) > MAX_DOWNLOAD_BYTES:
            raise ValueError("Image exceeds the byte limit")
        chunks: list[bytes] = []
        size = 0
        for chunk in response.iter_bytes():
            size += len(chunk)
            if size > MAX_DOWNLOAD_BYTES:
                raise ValueError("Image exceeds the byte limit")
            chunks.append(chunk)
    try:
        with Image.open(io.BytesIO(b"".join(chunks))) as source:
            source.load()
            width, height = source.size
            if width * height > MAX_PIXELS:
                raise ValueError("Image exceeds the decoded pixel limit")
            image = ImageOps.exif_transpose(source).convert("RGB")
            image.thumbnail(TARGET_SIZE, Image.Resampling.LANCZOS)
            output = io.BytesIO()
            image.save(output, format="JPEG", quality=85, optimize=True)
            return output.getvalue(), image.width, image.height
    except (UnidentifiedImageError, Image.DecompressionBombError) as error:
        raise ValueError("Image could not be decoded safely") from error


def voyage_embed(client: httpx.Client, api_key: str, images: list[bytes], input_type: str) -> tuple[list[list[float]], float]:
    inputs = [
        {
            "content": [
                {
                    "type": "image_base64",
                    "image_base64": "data:image/jpeg;base64," + base64.b64encode(image).decode("ascii"),
                }
            ]
        }
        for image in images
    ]
    started = time.perf_counter()
    response: httpx.Response | None = None
    for attempt in range(4):
        response = client.post(
            "https://api.voyageai.com/v1/multimodalembeddings",
            headers={"Authorization": f"Bearer {api_key}"},
            json={"inputs": inputs, "model": "voyage-multimodal-3.5", "input_type": input_type},
        )
        if response.status_code != 429:
            break
        if attempt < 3:
            retry_after = response.headers.get("retry-after")
            delay = min(float(retry_after), 30.0) if retry_after else (2 ** attempt) * 5.0
            time.sleep(delay)
    assert response is not None
    if response.is_error:
        try:
            error_payload = response.json()
            message = str(error_payload.get("detail") or error_payload.get("message") or "Voyage request failed")
        except (ValueError, AttributeError):
            message = "Voyage request failed"
        raise RuntimeError(f"Voyage API returned HTTP {response.status_code}: {message[:300]}")
    elapsed_ms = (time.perf_counter() - started) * 1000
    payload = response.json()
    embeddings = payload.get("embeddings")
    if embeddings is None and isinstance(payload.get("data"), list):
        embeddings = [item.get("embedding") for item in payload["data"]]
    if not isinstance(embeddings, list) or len(embeddings) != len(images):
        raise RuntimeError("Voyage returned an unexpected embedding count")
    if any(len(vector) != 1024 for vector in embeddings):
        raise RuntimeError("Voyage returned an unexpected embedding dimension")
    return embeddings, elapsed_ms


def cosine(left: list[float], right: list[float]) -> float:
    denominator = math.sqrt(sum(x * x for x in left)) * math.sqrt(sum(x * x for x in right))
    return sum(x * y for x, y in zip(left, right, strict=True)) / denominator


def evaluate(samples: list[dict[str, Any]], documents: list[list[float]], queries: list[list[float]]) -> dict[str, Any]:
    ranks: list[int] = []
    results: list[dict[str, Any]] = []
    for index, query in enumerate(queries):
        ranking = sorted(range(len(documents)), key=lambda candidate: cosine(query, documents[candidate]), reverse=True)
        rank = ranking.index(index) + 1
        ranks.append(rank)
        results.append(
            {
                "productId": samples[index]["productId"],
                "source": samples[index]["source"],
                "category": samples[index]["category"],
                "expectedRank": rank,
                "topProductId": samples[ranking[0]]["productId"],
                "topSimilarity": round(cosine(query, documents[ranking[0]]), 6),
            }
        )
    count = len(ranks)
    return {
        "queryCount": count,
        "recallAt1": sum(rank <= 1 for rank in ranks) / count,
        "recallAt5": sum(rank <= 5 for rank in ranks) / count,
        "categoryAccuracy": sum(
            result["category"] == samples[next(i for i, item in enumerate(samples) if item["productId"] == result["topProductId"])]["category"]
            for result in results
        ) / count,
        "meanReciprocalRank": sum(1 / rank for rank in ranks) / count,
        "results": results,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--per-source", type=int, default=5)
    parser.add_argument("--report", type=Path, default=ROOT / "evidence" / "visual-search" / "phase0-poc-report.json")
    args = parser.parse_args()

    service_env = load_env(SERVICE_ROOT / ".env")
    backend_env = load_env(ROOT / "backend" / ".env")
    api_key = service_env.get("VOYAGE_API_KEY") or os.getenv("VOYAGE_API_KEY", "")
    if not api_key:
        raise RuntimeError("VOYAGE_API_KEY is not configured")

    with psycopg.connect(database_dsn(backend_env), connect_timeout=10) as connection:
        audit = catalog_audit(connection)
        samples = select_samples(connection, args.per_source)
    if len(samples) < 4 or len({sample["source"] for sample in samples}) < 2:
        raise RuntimeError("POC requires at least four products and both image sources")

    downloads: list[dict[str, Any]] = []
    download_failures: list[dict[str, str]] = []
    valid_samples: list[dict[str, Any]] = []
    document_images: list[bytes] = []
    query_images: list[bytes] = []
    accepted_by_source: Counter[str] = Counter()
    with httpx.Client(timeout=httpx.Timeout(20), follow_redirects=False) as client:
        for sample in samples:
            if accepted_by_source[sample["source"]] >= args.per_source:
                continue
            try:
                document, doc_width, doc_height = download_and_normalize(client, sample["documentUrl"])
                query, query_width, query_height = download_and_normalize(client, sample["queryUrl"])
            except (httpx.HTTPError, OSError, ValueError) as error:
                download_failures.append(
                    {"productId": sample["productId"], "source": sample["source"], "reason": str(error)}
                )
                continue
            valid_samples.append(sample)
            accepted_by_source[sample["source"]] += 1
            document_images.append(document)
            query_images.append(query)
            downloads.append(
                {
                    "productId": sample["productId"],
                    "source": sample["source"],
                    "documentNormalizedSize": [doc_width, doc_height],
                    "queryNormalizedSize": [query_width, query_height],
                    "documentBytes": len(document),
                    "queryBytes": len(query),
                }
            )
        if any(accepted_by_source[source] < args.per_source for source in ("cloudinary", "shopify")):
            raise RuntimeError(f"Not enough valid samples after pipeline validation: {dict(accepted_by_source)}")
        documents, document_latency = voyage_embed(client, api_key, document_images, "document")
        queries, query_latency = voyage_embed(client, api_key, query_images, "query")

    metrics = evaluate(valid_samples, documents, queries)
    report = {
        "generatedAt": datetime.now(UTC).isoformat(),
        "model": "voyage-multimodal-3.5",
        "dimensions": 1024,
        "groundTruth": {
            "kind": "alternate-catalog-view-of-same-product",
            "purpose": "Phase-0 feasibility only; not the final out-of-catalog acceptance benchmark",
            "sampleCount": len(valid_samples),
            "sourceCounts": dict(Counter(sample["source"] for sample in valid_samples)),
            "categoryCounts": dict(Counter(sample["category"] for sample in valid_samples)),
        },
        "catalogAudit": audit,
        "pipeline": {
            "successfulPairs": len(downloads),
            "failedCandidates": len(download_failures),
            "failures": download_failures,
            "items": downloads,
        },
        "voyage": {
            "documentBatchLatencyMs": round(document_latency, 2),
            "queryBatchLatencyMs": round(query_latency, 2),
        },
        "metrics": metrics,
        "preliminaryGate": {
            "recallAt5Target": 0.8,
            "passed": metrics["recallAt5"] >= 0.8,
        },
    }
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"report": str(args.report), "sampleCount": len(valid_samples), "metrics": {k: v for k, v in metrics.items() if k != "results"}, "passed": report["preliminaryGate"]["passed"]}))


if __name__ == "__main__":
    main()
