from __future__ import annotations

from pydantic import BaseModel


class OrderStatusResult(BaseModel):
    success: bool
    orderCode: str | None = None
    orderId: str | None = None
    orderStatus: str | None = None          # EN enum value
    orderStatusLabel: str | None = None     # Vietnamese label
    paymentStatus: str | None = None
    paymentStatusLabel: str | None = None
    totalAmount: float | None = None
    error: str | None = None
    errorCode: str | None = None            # auth_required | not_found | timeout | backend_error


class AddToCartResult(BaseModel):
    success: bool
    cartId: str | None = None
    itemCount: int | None = None
    subtotal: float | None = None
    error: str | None = None
    errorCode: str | None = None


class CancelOrderResult(BaseModel):
    success: bool
    orderCode: str | None = None
    orderStatus: str | None = None
    orderStatusLabel: str | None = None
    error: str | None = None
    errorCode: str | None = None
