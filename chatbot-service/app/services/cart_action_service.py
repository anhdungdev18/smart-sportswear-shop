from __future__ import annotations

from app.clients import cart_api_client
from app.clients.backend_api_client import BackendError
from app.schemas.action import AddToCartResult
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_ERROR_MESSAGES: dict[str, str] = {
    "auth_required": "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
    "forbidden":     "Bạn không có quyền thực hiện thao tác này.",
    "not_found":     "Sản phẩm không tồn tại hoặc đã hết hàng.",
    "conflict":      "Không thể thêm sản phẩm này vào giỏ.",
    "invalid":       "Thông tin sản phẩm không hợp lệ.",
    "timeout":       "Hệ thống không phản hồi. Vui lòng thử lại sau.",
    "unavailable":   "Không thể kết nối đến hệ thống. Vui lòng thử lại sau.",
    "backend_error": "Đã xảy ra lỗi từ hệ thống. Vui lòng thử lại.",
}


async def add_to_cart(
    variant_id: str | None,
    quantity: int,
    access_token: str | None,
) -> AddToCartResult:
    """
    Add a product variant to the user's cart via backend API.

    Tradeoff (Phase 5): `variant_id` must be provided as a UUID string.
    The chatbot cannot resolve variant UUID from free-text query alone —
    the frontend/client layer is responsible for passing structured args
    (variantId) when the user selects a product variant from search results.
    If variant_id is absent, we return a clear error asking for more info.
    """
    if not access_token:
        return AddToCartResult(
            success=False,
            error="Bạn cần đăng nhập để thêm sản phẩm vào giỏ.",
            errorCode="auth_required",
        )

    if not variant_id:
        return AddToCartResult(
            success=False,
            error=(
                "Vui lòng chọn sản phẩm, size và màu cụ thể trước khi thêm vào giỏ hàng. "
                "Bạn muốn tôi tìm sản phẩm nào?"
            ),
            errorCode="variant_required",
        )

    logger.info(f"cart_action_service | add variant_id={variant_id} qty={quantity}")
    try:
        cart = await cart_api_client.add_item(access_token, variant_id, quantity)
    except BackendError as e:
        logger.warning(f"cart_action_service | add error={e.code} msg={e}")
        return AddToCartResult(
            success=False,
            error=_ERROR_MESSAGES.get(e.code, str(e)),
            errorCode=e.code,
        )

    items = cart.get("items") or []
    subtotal = cart.get("subtotal")

    return AddToCartResult(
        success=True,
        cartId=str(cart.get("id", "")),
        itemCount=len(items),
        subtotal=float(subtotal) if subtotal is not None else None,
    )
