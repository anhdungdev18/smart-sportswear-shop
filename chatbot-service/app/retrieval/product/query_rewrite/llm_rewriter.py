"""
LLM-based query rewriter — fallback khi synonym_rewriter và keyword search đều thất bại.

Cơ chế:
  1. Đọc catalog.md một lần khi module load (cached)
  2. Khi được gọi, gửi (catalog, query) cho LLM
  3. LLM chỉ thay thế từ chỉ loại sản phẩm không có trong catalog
     bằng từ gần nghĩa nhất thực sự có trong catalog
  4. Không đổi màu, size, giá, giới tính

Chỉ gọi khi keyword search + synonym rewrite đều trả về 0 kết quả.
KHÔNG gọi cho mọi query — tránh tốn chi phí/thời gian không cần thiết.
"""
from __future__ import annotations

from pathlib import Path

from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_CATALOG_MD_PATH = Path(__file__).parent / "catalog.md"

_REWRITE_PROMPT = """\
Bạn là trợ lý chuẩn hóa từ khóa tìm kiếm cho cửa hàng thể thao.

Dưới đây là danh mục sản phẩm THỰC SỰ có trong cửa hàng:
{catalog}

Câu hỏi của khách: "{query}"

Nhiệm vụ: viết lại câu hỏi trên, CHỈ thay các từ chỉ loại sản phẩm KHÔNG CÓ trong danh mục \
bằng từ gần nghĩa nhất thực sự có trong danh mục.
- Giữ nguyên: màu sắc, size, giá tiền, giới tính, thương hiệu, số lượng
- Nếu không tìm được từ nào gần nghĩa trong danh mục, giữ NGUYÊN câu hỏi gốc
- Chỉ trả về câu đã viết lại, không giải thích, không dấu ngoặc kép"""

_AMBIGUOUS_NEED_PROMPT = """\
Bạn là bộ chuẩn hóa truy vấn tìm kiếm cho cửa hàng thể thao.

Catalog thực tế của cửa hàng:
{catalog}

Khách hỏi: "{query}"

Câu hỏi đang mô tả hoàn cảnh hoặc nhu cầu thay vì thuộc tính sản phẩm rõ ràng.
Hãy viết lại thành MỘT truy vấn tìm kiếm ngắn, dùng loại sản phẩm và tính năng phù hợp có trong catalog.

Quy tắc bắt buộc:
- Suy luận hợp lý từ hoàn cảnh, ví dụ "áo đi Đà Lạt" có thể thành "áo khoác giữ ấm".
- Giữ nguyên mọi màu sắc, size, giá, giới tính, thương hiệu và số lượng khách đã nói.
- Không tự thêm thương hiệu, màu, size, giới tính hoặc mức giá mới.
- Không liệt kê nhiều phương án bằng dấu phẩy hoặc từ "hoặc"; chọn truy vấn phù hợp nhất.
- Chỉ trả về truy vấn đã viết lại, không giải thích, không đặt trong dấu ngoặc kép.
"""


def _load_catalog() -> str:
    """Load catalog: ưu tiên catalog_context.txt (DB-driven), fallback về catalog.md tĩnh."""
    try:
        from app.services.catalog_context import get_catalog_context
        db_catalog = get_catalog_context()
        if db_catalog:
            return db_catalog
    except Exception:
        pass
    # Fallback: hand-crafted catalog.md (synonym mappings phong phú hơn)
    try:
        return _CATALOG_MD_PATH.read_text(encoding="utf-8").strip()
    except Exception as exc:
        logger.warning(f"llm_rewriter | catalog_load_error={exc!r}")
        return ""


async def rewrite(query: str) -> str:
    """
    Rewrite query using LLM + catalog context.

    Returns the rewritten query string, or the original query on any failure.
    """
    from app.services import llm_client

    if not llm_client.is_available():
        logger.info("llm_rewriter | skip reason=llm_unavailable")
        return query

    catalog = _load_catalog()
    if not catalog:
        logger.info("llm_rewriter | skip reason=catalog_empty")
        return query

    prompt = _REWRITE_PROMPT.format(catalog=catalog, query=query)

    try:
        response = await llm_client.chat_complete(
            [{"role": "user", "content": prompt}], temperature=0, max_tokens=80
        )
        rewritten = (response or "").strip().strip('"')
        if not rewritten:
            return query

        if rewritten.strip().lower() == query.strip().lower():
            logger.info(f"llm_rewriter | no_change query={query!r}")
            return query

        logger.info(f"llm_rewriter | original={query!r} rewritten={rewritten!r}")
        return rewritten

    except Exception as exc:
        logger.warning(f"llm_rewriter | error={exc!r} returning_original")
        return query


async def rewrite_ambiguous_need(query: str) -> str:
    """Rewrite an occasion-based query before retrieval, with constraint guards."""
    from app.services import llm_client
    from app.retrieval.product.query_rewrite import synonym_rewriter

    fallback = synonym_rewriter.rewrite(query)

    if not llm_client.is_available():
        return fallback

    catalog = _load_catalog()
    if not catalog:
        return fallback

    try:
        response = await llm_client.chat_complete(
            [{"role": "user", "content": _AMBIGUOUS_NEED_PROMPT.format(catalog=catalog, query=query)}],
            temperature=0,
            max_tokens=60,
        )
        rewritten = " ".join((response or "").strip().strip('"').split())
        if not rewritten or len(rewritten) > 180:
            return fallback
        if not _preserves_explicit_constraints(query, rewritten):
            logger.warning("llm_rewriter | ambiguous_rewrite_rejected reason=constraint_changed")
            return fallback
        logger.info(f"llm_rewriter | ambiguous original={query!r} rewritten={rewritten!r}")
        return rewritten
    except Exception as exc:
        logger.warning(f"llm_rewriter | ambiguous_error={exc!r} returning_original")
        return fallback


def _preserves_explicit_constraints(original: str, rewritten: str) -> bool:
    from app.retrieval.product.parser.query_parser import parse_query

    before = parse_query(original)
    after = parse_query(rewritten)
    for field in ("gender", "brand", "color", "price_min", "price_max"):
        expected = getattr(before, field)
        if expected is not None and getattr(after, field) != expected:
            return False
    return True
