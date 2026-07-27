from __future__ import annotations

from typing import Any

from app.graph.evidence import EvidencePack
from app.services.llm_client import LlmClient


def _page_items(result: Any) -> list[dict[str, Any]]:
    if isinstance(result, dict) and isinstance(result.get("content"), list):
        return result["content"]
    if isinstance(result, list):
        return result
    return []


def collect_numbers(result: Any) -> list[str]:
    numbers: list[str] = []
    if isinstance(result, dict):
        if isinstance(result.get("content"), list):
            numbers.append(f"rows={len(result['content'])}")
        for key, value in result.items():
            if isinstance(value, (int, float)) and not isinstance(value, bool):
                numbers.append(f"{key}={value}")
            elif isinstance(value, dict):
                numbers.extend(collect_numbers(value)[:8])
            elif isinstance(value, list):
                numbers.extend(collect_numbers(value)[:8])
        if "totalElements" in result:
            numbers.append(f"totalElements={result['totalElements']}")
    if isinstance(result, list):
        numbers.append(f"rows={len(result)}")
        for item in result[:5]:
            numbers.extend(collect_numbers(item)[:8])
    return numbers[:12]


def _format_number(value: Any) -> str:
    if isinstance(value, (int, float)) and not isinstance(value, bool):
        return f"{value:,.0f}".replace(",", ".")
    return str(value)


def _format_vnd(value: Any) -> str:
    return f"{_format_number(value)}₫"


def _format_status(value: Any) -> str:
    labels = {
        "PENDING_CONFIRMATION": "chờ xác nhận",
        "CONFIRMED": "đã xác nhận",
        "PACKING": "đang đóng gói",
        "SHIPPING": "đang giao",
        "DELIVERED": "đã giao",
        "CANCELLED": "đã hủy",
        "PENDING": "đang chờ",
        "STOCKOUT": "nguy cơ hết hàng",
        "OVERSTOCK": "nguy cơ dư hàng",
        "BALANCED": "ổn định",
        "UNKNOWN": "chưa phân loại",
    }
    return labels.get(str(value), str(value).lower().replace("_", " "))


def _format_order_overview(result: Any) -> str:
    if not isinstance(result, dict):
        return "Tôi chưa đọc được tổng quan đơn hàng từ phản hồi hệ thống."

    total_orders = result.get("totalOrders")
    if total_orders is None:
        return "Tôi chưa thấy tổng số đơn hàng trong phản hồi hệ thống."

    return f"Hiện tại hệ thống có {_format_number(total_orders)} đơn hàng."


def _is_why_question(message: str | None) -> bool:
    if not message:
        return False
    text = message.lower()
    return any(term in text for term in ["tại sao", "tai sao", "vì sao", "vi sao", "khác nhau", "khac nhau", "chenh", "chênh"])


def _format_sales_overview(result: Any, message: str | None = None) -> str:
    if not isinstance(result, dict):
        return "Tôi chưa đọc được tổng quan doanh thu từ phản hồi hệ thống."

    gross_revenue = result.get("grossRevenue")
    realized_revenue = result.get("realizedRevenue")
    if gross_revenue is None and realized_revenue is None:
        return "Tôi chưa thấy số liệu doanh thu trong phản hồi hệ thống."

    if gross_revenue is not None and realized_revenue is not None and _is_why_question(message):
        difference = abs(float(realized_revenue) - float(gross_revenue))
        if float(realized_revenue) > float(gross_revenue):
            direction = "doanh thu thực nhận đang cao hơn doanh thu ghi nhận"
        elif float(realized_revenue) < float(gross_revenue):
            direction = "doanh thu ghi nhận đang cao hơn doanh thu thực nhận"
        else:
            direction = "hai số này hiện đang bằng nhau"
        return (
            f"Hai số này khác nhau vì đang đo hai lát cắt khác nhau của đơn hàng. "
            f"Doanh thu ghi nhận chỉ cộng các đơn có trạng thái thanh toán PAID, còn doanh thu thực nhận cộng các đơn đã DELIVERED. "
            f"Với dữ liệu hiện tại, {direction}, chênh khoảng {_format_vnd(difference)}. "
            f"Trường hợp này thường xảy ra khi có nhiều đơn COD đã giao nhưng chưa được đánh dấu PAID, hoặc trạng thái thanh toán và trạng thái giao hàng chưa đồng bộ."
        )

    if gross_revenue is not None and realized_revenue is not None:
        return (
            f"Doanh thu ghi nhận hiện tại là {_format_vnd(gross_revenue)}. "
            f"Doanh thu thực nhận là {_format_vnd(realized_revenue)}."
        )
    if gross_revenue is not None:
        return f"Doanh thu ghi nhận hiện tại là {_format_vnd(gross_revenue)}."
    return f"Doanh thu thực nhận hiện tại là {_format_vnd(realized_revenue)}."


def _format_revenue_breakdown(result: Any, message: str | None = None) -> str:
    if not isinstance(result, dict):
        return "Toi chua doc duoc du lieu phan ra doanh thu tu he thong."

    gross_revenue = result.get("grossRevenue")
    realized_revenue = result.get("realizedRevenue")
    difference = result.get("difference")
    if difference is None and isinstance(gross_revenue, (int, float)) and isinstance(realized_revenue, (int, float)):
        difference = float(realized_revenue) - float(gross_revenue)

    if gross_revenue is None and realized_revenue is None:
        return "Toi chua thay du du lieu doanh thu de giai thich chenh lech."

    parts = [
        "Hai chi so doanh thu dang do hai lat cat khac nhau: doanh thu ghi nhan dua tren don da thanh toan, con doanh thu thuc nhan dua tren don da giao.",
    ]
    if gross_revenue is not None:
        parts.append(f"Doanh thu ghi nhan la {_format_vnd(gross_revenue)}.")
    if realized_revenue is not None:
        parts.append(f"Doanh thu thuc nhan la {_format_vnd(realized_revenue)}.")
    if difference is not None:
        direction = "thuc nhan cao hon ghi nhan" if float(difference) > 0 else "ghi nhan cao hon thuc nhan" if float(difference) < 0 else "hai so dang bang nhau"
        parts.append(f"Chenh lech hien tai la {_format_vnd(abs(float(difference)))}; {direction}.")

    cod = result.get("codDeliveredUnpaid")
    paid_not_delivered = result.get("paidNotDelivered")
    evidence_details: list[str] = []
    if isinstance(cod, dict) and cod.get("amount") is not None:
        evidence_details.append(f"COD da giao nhung chua danh dau da thanh toan: {_format_vnd(cod.get('amount'))}.")
    if isinstance(paid_not_delivered, dict) and paid_not_delivered.get("amount") is not None:
        evidence_details.append(f"Don da thanh toan nhung chua giao: {_format_vnd(paid_not_delivered.get('amount'))}.")
    if evidence_details:
        parts.extend(evidence_details)
    elif result.get("breakdownAvailable") is False:
        parts.append("Hien chua du breakdown theo trang thai thanh toan va giao hang, nen day la giai thich theo quy tac nghiep vu chu chua ket luan nguyen nhan chac chan.")
    else:
        parts.append("De ket luan nguyen nhan chinh, can doi chieu breakdown theo trang thai thanh toan va trang thai giao hang.")
    return " ".join(parts)


def _format_data_quality(result: Any) -> str:
    if not isinstance(result, dict):
        return "Tôi chưa đọc được báo cáo chất lượng dữ liệu."

    total = result.get("totalVariants")
    insufficient = result.get("insufficientVariants")
    missing_supplier = result.get("variantsMissingSupplier")
    if total is None:
        return "Tôi đã kiểm tra chất lượng dữ liệu, nhưng phản hồi chưa có tổng số biến thể."

    reply = f"Hệ thống đang có {_format_number(total)} biến thể trong báo cáo chất lượng dữ liệu."
    details: list[str] = []
    if insufficient is not None:
        details.append(f"{_format_number(insufficient)} biến thể thiếu dữ liệu")
    if missing_supplier is not None:
        details.append(f"{_format_number(missing_supplier)} biến thể thiếu nhà cung cấp")
    if details:
        reply += " Điểm cần chú ý: " + ", ".join(details) + "."
    return reply


def _format_inventory_risks(items: list[dict[str, Any]]) -> tuple[str, list[str]]:
    split: dict[str, int] = {}
    for item in items:
        risk = str(item.get("risk", "UNKNOWN"))
        split[risk] = split.get(risk, 0) + 1

    numbers = [f"{risk}={count}" for risk, count in sorted(split.items())]
    if not items:
        return "Tôi chưa thấy SKU nào trong danh sách rủi ro tồn kho hiện tại.", numbers

    parts = [f"{_format_status(risk)}: {_format_number(count)}" for risk, count in sorted(split.items())]
    return f"Hiện có {_format_number(len(items))} SKU trong danh sách rủi ro tồn kho. Phân bổ: {', '.join(parts)}.", numbers


def _format_replenishment_suggestions(items: list[dict[str, Any]], question_type: str = "UNKNOWN") -> str:
    if not items:
        return "Hiện chưa có đề xuất nhập hàng nào trong trang kết quả này."
    if question_type == "EXPLANATION":
        return (
            f"Hiện có {_format_number(len(items))} đề xuất nhập hàng trong trang kết quả này. "
            "Chưa đủ dữ liệu chi tiết để kết luận chắc chắn vì danh sách hiện tại chưa kèm công thức, vận tốc bán, tồn khả dụng và chất lượng dự báo cho từng SKU."
        )
    return f"Hiện có {_format_number(len(items))} đề xuất nhập hàng trong trang kết quả này."


def _format_product_inventory(items: list[dict[str, Any]]) -> str:
    if not items:
        return "Tôi chưa tìm thấy biến thể kho nào phù hợp."
    if len(items) == 1:
        item = items[0]
        return (
            f"{item.get('productName')} SKU {item.get('sku')} còn "
            f"{_format_number(item.get('availableQuantity'))} sản phẩm khả dụng "
            f"(tồn {_format_number(item.get('stockQuantity'))}, đã giữ {_format_number(item.get('reservedQuantity'))})."
        )
    candidates = ", ".join(f"{item.get('sku')} {item.get('size')}/{item.get('color')}" for item in items[:5])
    return f"Tôi tìm thấy {_format_number(len(items))} biến thể phù hợp. Một vài mã đầu tiên: {candidates}."


def _format_best_sellers(result: Any) -> str:
    if not isinstance(result, dict):
        return "Tôi chưa đọc được danh sách sản phẩm bán chạy."
    rows = result.get("items", [])
    if not isinstance(rows, list) or not rows:
        return "Trong khoảng thời gian này chưa có sản phẩm bán chạy nào trong dữ liệu."
    first = rows[0]
    name = first.get("productName") or first.get("sku") or "sản phẩm dẫn đầu"
    return (
        f"Từ {result.get('fromDate')} đến {result.get('toDate')}, có {_format_number(len(rows))} sản phẩm trong nhóm bán chạy. "
        f"Dẫn đầu là {name}."
    )


def _format_freshness(result: Any) -> str:
    if not isinstance(result, dict):
        return "Tôi chưa đọc được trạng thái cập nhật dữ liệu AI."
    stale = result.get("stale")
    source = result.get("dataSource") or "nguồn hiện tại"
    if stale is True:
        return f"Dữ liệu AI của {source} đang bị cũ, nên nên làm mới trước khi ra quyết định."
    if stale is False:
        return f"Dữ liệu AI của {source} đang còn mới."
    return f"Tôi đã kiểm tra trạng thái cập nhật dữ liệu AI cho {source}."


def generate_grounded_answer(intent: str, tool_name: str, result: Any, message: str | None = None, question_type: str = "UNKNOWN") -> tuple[str, list[str], list[str]]:
    warnings: list[str] = []
    numbers = collect_numbers(result)
    items = _page_items(result)

    if tool_name == "get_inventory_risks":
        reply, risk_numbers = _format_inventory_risks(items)
        numbers = risk_numbers + numbers
    elif tool_name == "get_replenishment_suggestions":
        reply = _format_replenishment_suggestions(items, question_type)
    elif tool_name == "search_product_inventory":
        reply = _format_product_inventory(items)
    elif tool_name == "get_best_selling_products":
        reply = _format_best_sellers(result)
    elif tool_name == "get_ai_data_freshness":
        reply = _format_freshness(result)
    elif tool_name == "get_urgent_replenishment_candidates":
        reply = f"Tôi tìm thấy {_format_number(len(items))} SKU cần ưu tiên xem xét nhập hàng."
    elif tool_name in {"sync_ai_snapshot", "run_demand_classification", "run_forecast_evaluation", "run_forecast_generation"}:
        reply = "Tôi đã gửi yêu cầu chạy lại tác vụ AI được phép. Bạn có thể kiểm tra trạng thái sau vài giây."
    elif tool_name == "get_ai_job_status":
        status = result.get("status") if isinstance(result, dict) else None
        reply = f"Tác vụ AI hiện đang ở trạng thái {_format_status(status)}."
    elif tool_name == "get_order_overview":
        reply = _format_order_overview(result)
    elif tool_name == "get_sales_overview":
        reply = _format_sales_overview(result, message)
    elif tool_name == "get_revenue_breakdown":
        reply = _format_revenue_breakdown(result, message)
    elif tool_name in {"get_data_quality_summary", "get_forecast_quality"}:
        reply = _format_data_quality(result)
    elif tool_name == "simulate_inventory_policy":
        reply = "Tôi đã chạy mô phỏng tồn kho ở chế độ chỉ đọc. Không có thay đổi nào được ghi vào hệ thống."
        warnings.append("Mô phỏng chỉ dùng để tham khảo, không phải lệnh nhập hàng.")
    elif intent == "UNKNOWN":
        reply = "Tôi chưa rõ bạn muốn xem nghiệp vụ nào. Bạn có thể hỏi cụ thể hơn về đơn hàng, doanh thu, tồn kho hoặc dự báo."
    else:
        reply = "Tôi đã lấy được dữ liệu liên quan, nhưng chưa có mẫu trả lời phù hợp cho loại câu hỏi này."

    return reply, warnings, numbers


async def compose_grounded_answer(
    evidence_pack: EvidencePack,
    primary_call: dict[str, Any],
    llm: LlmClient | None = None,
) -> tuple[str, list[str], list[str]]:
    fallback_reply, fallback_warnings, fallback_numbers = generate_grounded_answer(
        evidence_pack.intent,
        primary_call["tool"],
        primary_call["result"],
        evidence_pack.message,
        evidence_pack.question_type,
    )

    client = llm or LlmClient()
    if not client.enabled():
        return fallback_reply, fallback_warnings, fallback_numbers

    payload = {
        "userQuestion": evidence_pack.message,
        "intent": evidence_pack.intent,
        "questionType": evidence_pack.question_type,
        "toolResults": [
            {
                "result": call.get("result"),
                "reason": call.get("reason"),
                "source": call.get("source"),
            }
            for call in evidence_pack.tool_calls
        ],
        "businessRules": evidence_pack.business_rules,
        "memory": evidence_pack.memory,
        "deterministicFallback": fallback_reply,
    }
    system_prompt = (
        "You are an admin operations analyst for a sportswear ecommerce system. "
        "Answer in natural Vietnamese using only provided evidence and business rules. "
        "Never expose internal tool names, endpoints, traces, JSON field names, run IDs, or API details. "
        "If evidence is insufficient, say what is missing and avoid a definitive conclusion. "
        "For money, format VND. Keep concise unless the user asks for detail. "
        "Return only JSON with fields reply and warnings."
    )
    try:
        parsed = await client.complete_json(system_prompt, payload)
        reply = str(parsed.get("reply", "")).strip()
        warnings = parsed.get("warnings", [])
        if not reply:
            return fallback_reply, fallback_warnings, fallback_numbers
        if not isinstance(warnings, list):
            warnings = []
        return reply, [str(item) for item in warnings], fallback_numbers
    except Exception:
        return fallback_reply, fallback_warnings, fallback_numbers
