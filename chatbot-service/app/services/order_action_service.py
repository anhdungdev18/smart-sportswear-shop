from __future__ import annotations

from app.clients import order_api_client
from app.clients.backend_api_client import BackendError
from app.schemas.action import OrderStatusResult, CancelOrderResult
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_ORDER_STATUS_LABELS: dict[str, str] = {
    "PENDING_CONFIRMATION": "Chờ xác nhận",
    "CONFIRMED":            "Đã xác nhận",
    "PACKING":              "Đang đóng gói",
    "SHIPPING":             "Đang giao hàng",
    "DELIVERED":            "Đã giao hàng",
    "CANCELLED":            "Đã hủy",
}

_PAYMENT_STATUS_LABELS: dict[str, str] = {
    "UNPAID":    "Chưa thanh toán",
    "PENDING":   "Đang xử lý",
    "PAID":      "Đã thanh toán",
    "FAILED":    "Thanh toán thất bại",
    "CANCELLED": "Đã hủy",
    "REFUNDED":  "Đã hoàn tiền",
}

_ERROR_MESSAGES: dict[str, str] = {
    "auth_required": "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
    "forbidden":     "Bạn không có quyền xem đơn hàng này.",
    "not_found":     "Không tìm thấy đơn hàng.",
    "conflict":      "Không thể thực hiện do xung đột nghiệp vụ.",
    "invalid":       "Yêu cầu không hợp lệ.",
    "timeout":       "Hệ thống không phản hồi. Vui lòng thử lại sau.",
    "unavailable":   "Không thể kết nối đến hệ thống. Vui lòng thử lại sau.",
    "backend_error": "Đã xảy ra lỗi từ hệ thống. Vui lòng thử lại.",
}


async def get_order_status(order_code: str, access_token: str | None) -> OrderStatusResult:
    if not access_token:
        return OrderStatusResult(
            success=False,
            error="Bạn cần đăng nhập để xem trạng thái đơn hàng.",
            errorCode="auth_required",
        )

    logger.info(f"order_action_service | get_status code={order_code}")
    try:
        order = await order_api_client.find_by_code(access_token, order_code)
    except BackendError as e:
        logger.warning(f"order_action_service | get_status error={e.code} msg={e}")
        return OrderStatusResult(
            success=False,
            error=_ERROR_MESSAGES.get(e.code, str(e)),
            errorCode=e.code,
        )

    if order is None:
        return OrderStatusResult(
            success=False,
            orderCode=order_code,
            error=f"Không tìm thấy đơn hàng {order_code}.",
            errorCode="not_found",
        )

    raw_status = order.get("orderStatus", "")
    raw_payment = order.get("paymentStatus", "")
    total = order.get("totalAmount")

    return OrderStatusResult(
        success=True,
        orderCode=order.get("orderCode", order_code),
        orderId=str(order.get("id", "")),
        orderStatus=raw_status,
        orderStatusLabel=_ORDER_STATUS_LABELS.get(raw_status, raw_status),
        paymentStatus=raw_payment,
        paymentStatusLabel=_PAYMENT_STATUS_LABELS.get(raw_payment, raw_payment),
        totalAmount=float(total) if total is not None else None,
    )


async def cancel_order(
    order_code: str,
    access_token: str | None,
    reason: str | None = None,
) -> CancelOrderResult:
    if not access_token:
        return CancelOrderResult(
            success=False,
            error="Bạn cần đăng nhập để hủy đơn hàng.",
            errorCode="auth_required",
        )

    logger.info(f"order_action_service | cancel code={order_code}")
    try:
        # Step 1: resolve code → UUID
        order = await order_api_client.find_by_code(access_token, order_code)
        if order is None:
            return CancelOrderResult(
                success=False,
                orderCode=order_code,
                error=f"Không tìm thấy đơn hàng {order_code}.",
                errorCode="not_found",
            )

        order_id = str(order["id"])

        # Step 2: cancel by UUID
        cancelled = await order_api_client.cancel_order(access_token, order_id, reason)

    except BackendError as e:
        logger.warning(f"order_action_service | cancel error={e.code} msg={e}")
        return CancelOrderResult(
            success=False,
            orderCode=order_code,
            error=_ERROR_MESSAGES.get(e.code, str(e)),
            errorCode=e.code,
        )

    raw_status = cancelled.get("orderStatus", "")
    return CancelOrderResult(
        success=True,
        orderCode=cancelled.get("orderCode", order_code),
        orderStatus=raw_status,
        orderStatusLabel=_ORDER_STATUS_LABELS.get(raw_status, raw_status),
    )
