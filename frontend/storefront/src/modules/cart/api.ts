import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { CartResponse } from "@/modules/cart/types";

export async function getCart() {
  const result = await apiFetch<CartResponse>(endpoints.cart);
  return result.data;
}

export async function addCartItem(variantId: string, quantity: number) {
  const result = await apiFetch<CartResponse>(`${endpoints.cart}/items`, {
    method: "POST",
    body: JSON.stringify({ variantId, quantity }),
  });
  return result.data;
}

export async function updateCartItem(id: string, quantity: number) {
  const result = await apiFetch<CartResponse>(`${endpoints.cart}/items/${id}`, {
    method: "PATCH",
    body: JSON.stringify({ quantity }),
  });
  return result.data;
}

export async function removeCartItem(id: string) {
  const result = await apiFetch<CartResponse>(`${endpoints.cart}/items/${id}`, {
    method: "DELETE",
  });
  return result.data;
}

export type { CartItem, CartResponse } from "@/modules/cart/types";
