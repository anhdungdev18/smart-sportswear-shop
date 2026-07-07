import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type {
  AddressResponse,
  OrderResponse,
  PagedResult,
  WishlistResponse,
} from "@/modules/account/types";

export async function getWishlist() {
  const result = await apiFetch<WishlistResponse>(endpoints.wishlist.root);
  return result.data;
}

export async function addWishlistItem(productId: string) {
  const result = await apiFetch<WishlistResponse>(endpoints.wishlist.items, {
    method: "POST",
    body: JSON.stringify({ productId }),
  });
  return result.data;
}

export async function removeWishlistItem(productId: string) {
  const result = await apiFetch<WishlistResponse>(endpoints.wishlist.item(productId), {
    method: "DELETE",
  });
  return result.data;
}

export async function listAddresses() {
  const result = await apiFetch<AddressResponse[]>(endpoints.addresses.root);
  return result.data;
}

export async function createAddress(payload: {
  receiverName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  addressLine: string;
  isDefault?: boolean;
}) {
  const result = await apiFetch<AddressResponse>(endpoints.addresses.root, {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export async function updateAddress(
  id: string,
  payload: Partial<{
    receiverName: string;
    phone: string;
    province: string;
    district: string;
    ward: string;
    addressLine: string;
  }>,
) {
  const result = await apiFetch<AddressResponse>(endpoints.addresses.item(id), {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
  return result.data;
}

export async function deleteAddress(id: string) {
  await apiFetch(endpoints.addresses.item(id), {
    method: "DELETE",
  });
}

export async function setDefaultAddress(id: string) {
  const result = await apiFetch<AddressResponse>(endpoints.addresses.default(id), {
    method: "PATCH",
  });
  return result.data;
}

export async function listMyOrders(query?: { page?: number; limit?: number; status?: string }) {
  const result = await apiFetch<OrderResponse[]>(endpoints.orders.mine, {
    method: "GET",
    query,
  });
  return result as PagedResult<OrderResponse[]>;
}

export async function getOrderDetail(id: string) {
  const result = await apiFetch<OrderResponse>(endpoints.orders.detail(id));
  return result.data;
}

export async function cancelOrder(id: string, reason?: string) {
  const result = await apiFetch<OrderResponse>(`${endpoints.orders.detail(id)}/cancel`, {
    method: "POST",
    body: JSON.stringify({ reason: reason || null }),
  });
  return result.data;
}

export type {
  AddressResponse,
  OrderItem,
  OrderResponse,
  PagedResult,
  WishlistItem,
  WishlistResponse,
} from "@/modules/account/types";
