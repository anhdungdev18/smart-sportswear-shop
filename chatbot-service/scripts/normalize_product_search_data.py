"""Dry-run-first normalization for product_type, color_family and edition.

Examples:
  python scripts/normalize_product_search_data.py
  python scripts/normalize_product_search_data.py --apply

The script is idempotent and never changes product_variants.color, which remains
the storefront display value.
"""
from __future__ import annotations

import argparse
import asyncio
import os
import re
import sys
import unicodedata
from collections import Counter

import asyncpg
from dotenv import load_dotenv


COLOR_TERMS = {
    "BLACK": ("black", "den", "all black"),
    "WHITE": ("white", "trang"),
    "BLUE": ("blue", "navy", "cobalt", "xanh duong", "xanh bien"),
    "GREEN": ("green", "xanh la"),
    "RED": ("red", "do"),
    "PINK": ("pink", "hong"),
    "YELLOW": ("yellow", "vang", "solar"),
    "GRAY": ("gray", "grey", "xam"),
    "BROWN": ("brown", "nau"),
    "ORANGE": ("orange", "cam"),
    "PURPLE": ("purple", "violet", "tim"),
    "BEIGE": ("beige", " be "),
}
EDITION_TERMS = ("san nha", "san khach", "event pack", "shadow", "mij")


def normalize(value: str) -> str:
    decomposed = unicodedata.normalize("NFD", value.casefold())
    without_marks = "".join(ch for ch in decomposed if unicodedata.category(ch) != "Mn")
    return " ".join(without_marks.replace("đ", "d").split())


def classify_color(value: str) -> tuple[str | None, str | None]:
    normalized = f" {normalize(value)} "
    def contains_term(term: str) -> bool:
        return re.search(rf"(?<!\w){re.escape(term)}(?!\w)", normalized) is not None

    edition = next((term for term in EDITION_TERMS if contains_term(term)), None)
    family = next(
        (family for family, terms in COLOR_TERMS.items() if any(contains_term(term) for term in terms)),
        None,
    )
    return family, edition.title() if edition else None


def infer_product_type(category_name: str) -> str | None:
    category = normalize(category_name)
    if re.search(r"\b(ao|quan|trang phuc)\b", category):
        return "APPAREL"
    if re.search(r"\b(giay|dep)\b", category):
        return "FOOTWEAR"
    if re.search(r"\b(phu kien|balo|tui|mu|tat|vo)\b", category):
        return "ACCESSORY"
    if re.search(r"\b(thiet bi|dung cu|bong|vot)\b", category):
        return "EQUIPMENT"
    return None


async def run(apply: bool) -> int:
    load_dotenv()
    dsn = os.environ["DB_WRITE_URL" if apply else "DB_READ_URL"].replace(
        "postgresql+asyncpg://", "postgresql://", 1
    )
    conn = await asyncpg.connect(dsn)
    try:
        products = await conn.fetch(
            """
            select p.id, c.name category_name
            from products p join categories c on c.id = p.category_id
            where p.status = 'ACTIVE' and p.product_type is null
            order by p.id
            """
        )
        variants = await conn.fetch(
            """
            select pv.id, pv.color
            from product_variants pv join products p on p.id = pv.product_id
            where p.status = 'ACTIVE'
            order by pv.id
            """
        )
        product_updates = [(row["id"], infer_product_type(row["category_name"])) for row in products]
        variant_updates = [(row["id"], *classify_color(row["color"])) for row in variants]
        unmapped_products = [str(pid) for pid, value in product_updates if value is None]
        unmapped_colors = sorted({row["color"] for row, update in zip(variants, variant_updates) if update[1] is None})

        print(f"mode={'APPLY' if apply else 'DRY_RUN'}")
        print(f"active_missing_product_type={len(products)}")
        print(f"product_type_mapping={dict(Counter(value or 'UNMAPPED' for _, value in product_updates))}")
        print(f"active_variants={len(variants)}")
        print(f"color_family_mapping={dict(Counter(family or 'UNMAPPED' for _, family, _ in variant_updates))}")
        print(f"edition_mapping={dict(Counter(edition or 'NONE' for _, _, edition in variant_updates))}")
        print(f"unmapped_product_ids={len(unmapped_products)}")
        print(f"unmapped_color_values={unmapped_colors}")

        if apply:
            async with conn.transaction():
                await conn.executemany(
                    "update products set product_type=$2 where id=$1 and product_type is null",
                    [(pid, value) for pid, value in product_updates if value],
                )
                await conn.executemany(
                    """
                    update product_variants
                    set color_family=$2, edition=$3
                    where id=$1
                      and (color_family is distinct from $2 or edition is distinct from $3)
                    """,
                    variant_updates,
                )
            print("applied=true")
        return 0 if not unmapped_products else 2
    finally:
        await conn.close()


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    raise SystemExit(asyncio.run(run(args.apply)))
