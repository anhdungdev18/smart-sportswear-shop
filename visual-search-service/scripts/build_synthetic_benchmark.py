import argparse
import asyncio
import io
import json
import random
from pathlib import Path
import sys

import psycopg
from PIL import Image, ImageEnhance, ImageFilter

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.config import Settings
from app.services.image_pipeline import ImagePipeline


TARGETS = {"tops": 34, "bottoms": 33, "shoes": 33}


def stratum(slug: str) -> str | None:
    if slug.startswith("ao-"):
        return "tops"
    if slug.startswith("quan-"):
        return "bottoms"
    if slug.startswith("giay-"):
        return "shoes"
    return None


def simulate_camera(content: bytes, seed: int) -> bytes:
    rng = random.Random(seed)
    with Image.open(io.BytesIO(content)) as source:
        image = source.convert("RGB")
    image.thumbnail((760, 760), Image.Resampling.LANCZOS)
    angle = rng.uniform(-13, 13)
    image = image.rotate(angle, resample=Image.Resampling.BICUBIC, expand=True, fillcolor=(238, 235, 228))
    image = ImageEnhance.Brightness(image).enhance(rng.uniform(0.78, 1.18))
    image = ImageEnhance.Contrast(image).enhance(rng.uniform(0.82, 1.22))
    image = ImageEnhance.Color(image).enhance(rng.uniform(0.82, 1.15))
    background_color = tuple(rng.randint(175, 245) for _ in range(3))
    canvas = Image.new("RGB", (900, 900), background_color)
    scale = rng.uniform(0.72, 0.91)
    image.thumbnail((int(900 * scale), int(900 * scale)), Image.Resampling.LANCZOS)
    x = (900 - image.width) // 2 + rng.randint(-35, 35)
    y = (900 - image.height) // 2 + rng.randint(-35, 35)
    canvas.paste(image, (x, y))
    noise = Image.effect_noise(canvas.size, rng.uniform(3, 10)).convert("L")
    noise_rgb = Image.merge("RGB", (noise, noise, noise))
    canvas = Image.blend(canvas, noise_rgb, rng.uniform(0.015, 0.04)).filter(
        ImageFilter.GaussianBlur(radius=rng.uniform(0.15, 0.65))
    )
    output = io.BytesIO()
    canvas.save(output, "JPEG", quality=rng.randint(72, 88), optimize=True)
    return output.getvalue()


async def catalog_rows(settings: Settings):
    async with await psycopg.AsyncConnection.connect(settings.database_url) as connection:
        async with connection.cursor() as cursor:
            await cursor.execute(
                """
                select p.id::text, p.name, c.slug, pi.id::text, pi.image_url, pi.public_id
                from products p join categories c on c.id = p.category_id
                join product_images pi on pi.product_id = p.id
                where p.status = 'ACTIVE'
                order by p.id, pi.created_at, pi.id
                """
            )
            return await cursor.fetchall()


async def run(output_dir: Path, seed: int) -> None:
    settings = Settings()
    rows = await catalog_rows(settings)
    grouped = {name: [] for name in TARGETS}
    for row in rows:
        group = stratum(row[2])
        if group:
            grouped[group].append(row)
    selected = []
    for group, count in TARGETS.items():
        products: dict[str, list] = {}
        for row in grouped[group]:
            products.setdefault(row[0], []).append(row)
        product_ids = sorted(products)
        index = 0
        while len([item for item in selected if item[0] == group]) < count:
            product_id = product_ids[index % len(product_ids)]
            variants = products[product_id]
            row = variants[(index // len(product_ids)) % len(variants)]
            selected.append((group, row))
            index += 1

    images_dir = output_dir / "benchmark-images"
    images_dir.mkdir(parents=True, exist_ok=True)
    pipeline = ImagePipeline(settings)
    samples = []
    for index, (group, row) in enumerate(selected, start=1):
        product_id, product_name, _, image_id, image_url, public_id = row
        normalized = await pipeline.download_and_normalize(image_url, public_id)
        transformed = simulate_camera(normalized.content, seed + index)
        filename = f"synthetic-{index:03d}.jpg"
        (images_dir / filename).write_bytes(transformed)
        samples.append({
            "id": f"synthetic-{index:03d}",
            "image": f"benchmark-images/{filename}",
            "stratum": group,
            "expectedProductIds": [product_id],
            "provenance": {
                "kind": "catalog-derived-camera-simulation",
                "sourceImageId": image_id,
                "sourceProductName": product_name,
                "seed": seed + index,
                "operations": ["rotation", "background", "brightness", "contrast", "color", "noise", "blur", "jpeg"],
            },
        })
        print(f"{index:03d}/100 {group} {product_name}")
    manifest = {
        "benchmarkType": "synthetic-robustness",
        "limitations": "Catalog-derived simulations measure transformation robustness, not real-world generalization.",
        "seed": seed,
        "samples": samples,
    }
    (output_dir / "benchmark-manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Build the approved deterministic 100-image synthetic benchmark.")
    parser.add_argument("output_dir", type=Path)
    parser.add_argument("--seed", type=int, default=20260801)
    args = parser.parse_args()
    asyncio.run(run(args.output_dir, args.seed))
