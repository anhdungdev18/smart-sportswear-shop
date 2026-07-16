import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { OrderResponse } from "@/modules/account/types";
import type { CheckoutPreviewResponse } from "@/modules/checkout/types";

export async function previewCheckout(addressId?: string) {
  const result = await apiFetch<CheckoutPreviewResponse>(endpoints.checkout.preview, {
    method: "POST",
    body: JSON.stringify({ addressId: addressId || null }),
  });
  return result.data;
}

export async function createOrder(payload: {
  addressId: string;
  paymentMethod: "COD" | "VNPAY";
  note?: string;
}) {
  const result = await apiFetch<OrderResponse>(endpoints.orders.root, {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export type { CheckoutItemPreview, CheckoutPreviewResponse } from "@/modules/checkout/types";
