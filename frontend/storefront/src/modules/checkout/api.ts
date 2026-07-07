import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { OrderResponse } from "@/modules/account/types";
import type {
  CheckoutPreviewResponse,
  CouponValidationResponse,
} from "@/modules/checkout/types";

export async function previewCheckout(addressId?: string, couponCode?: string) {
  const result = await apiFetch<CheckoutPreviewResponse>(endpoints.checkout.preview, {
    method: "POST",
    body: JSON.stringify({
      addressId: addressId || null,
      couponCode: couponCode || null,
    }),
  });
  return result.data;
}

export async function validateCoupon(couponCode: string) {
  const result = await apiFetch<CouponValidationResponse>(endpoints.checkout.validateCoupon, {
    method: "POST",
    body: JSON.stringify({ couponCode }),
  });
  return result.data;
}

export async function createOrder(payload: {
  addressId: string;
  paymentMethod: "COD" | "VNPAY";
  note?: string;
  couponCode?: string;
}) {
  const result = await apiFetch<OrderResponse>(endpoints.orders.root, {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export type {
  AppliedCouponSummary,
  CheckoutItemPreview,
  CheckoutPreviewResponse,
  CouponValidationResponse,
} from "@/modules/checkout/types";
