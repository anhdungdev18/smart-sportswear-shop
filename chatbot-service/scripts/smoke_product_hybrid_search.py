"""Manual Supabase/OpenAI smoke test; never part of the default test suite."""
from __future__ import annotations

import asyncio
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.config.settings import settings
from app.db import pool
from app.schemas.internal_product_search import InternalSearchRequest
from app.services.product_search_service import search_internal

QUERIES = [
    "giày Nike nam màu đen sân cỏ nhân tạo dưới 2 triệu size 42",
    "áo chạy bộ nữ thoáng khí màu hồng dưới 1 triệu size M",
    "giày đá banh tốc độ cho tiền đạo",
    "áo MU sân nhà",
    "đồ chạy bộ mặc trời nóng",
    "giay co nhan tao nike den",
]


async def main() -> None:
    await pool.init_pool(settings.DB_READ_URL)
    try:
        for query in QUERIES:
            result = await search_internal(
                InternalSearchRequest(query=query, page=1, limit=5, filters={})
            )
            print(
                f"query={query!r} mode={result['searchMode']} "
                f"total={result['total']} returned={len(result['items'])} "
                f"latency_ms={result['processingTimeMs']}"
            )
    finally:
        await pool.close_pool()


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    asyncio.run(main())
