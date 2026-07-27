from __future__ import annotations

from typing import Any


def _page_items(result: Any) -> list[dict[str, Any]]:
    if isinstance(result, dict) and isinstance(result.get("content"), list):
        return result["content"]
    if isinstance(result, list):
        return result
    return []


def collect_numbers(result: Any) -> list[str]:
    numbers: list[str] = []
    if isinstance(result, dict):
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


def generate_grounded_answer(intent: str, tool_name: str, result: Any) -> tuple[str, list[str], list[str]]:
    warnings: list[str] = []
    numbers = collect_numbers(result)
    items = _page_items(result)

    if tool_name == "get_inventory_risks":
        split: dict[str, int] = {}
        for item in items:
            split[item.get("risk", "UNKNOWN")] = split.get(item.get("risk", "UNKNOWN"), 0) + 1
        reply = f"Inventory risk hiện có {len(items)} SKU trong kết quả. Phân bổ: {split}."
    elif tool_name == "get_replenishment_suggestions":
        reply = f"Đang có {len(items)} đề xuất nhập hàng trong trang kết quả read-only."
    elif tool_name == "get_forecast_quality":
        reply = "Data-quality/forecast quality đã được lấy từ AI service."
    elif tool_name == "simulate_inventory_policy":
        reply = "Mô phỏng tồn kho đã chạy ở chế độ read-only; không có thay đổi nào được ghi vào hệ thống."
        warnings.append("Simulation chỉ là what-if, không phải lệnh nhập hàng.")
    elif intent == "UNKNOWN":
        reply = "Tôi chưa xác định được nghiệp vụ chính, nên trả về data-quality summary làm điểm bắt đầu."
    else:
        reply = f"Đã lấy dữ liệu {intent.lower()} từ API read-only."

    if numbers:
        reply += " Số liệu nguồn: " + ", ".join(numbers[:6]) + "."
    return reply, warnings, numbers
