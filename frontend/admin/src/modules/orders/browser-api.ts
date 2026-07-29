import { browserApiRequest, browserApiRequestEnvelope } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminOrderPage, AdminOrderResponse, PageMeta } from "@/modules/orders/types";

export async function listAdminOrdersPage(page = 1, limit = 20, keyword?: string, status?: string): Promise<AdminOrderPage> {
  const response = await browserApiRequestEnvelope<AdminOrderResponse[]>(adminEndpoints.orders, {
    query: { page, limit, keyword, status }, cache: "no-store"
  });
  return { items: response.data, meta: { page, limit, total: 0, totalPages: 0, ...(response.meta as Partial<PageMeta> | undefined) } };
}

export async function fetchOrderDetail(id: string) {
  return browserApiRequest<AdminOrderResponse>(adminEndpoints.orderDetail(id), {
    method: "GET"
  });
}

export async function updateOrderStatus(id: string, input: { status: string; note?: string }) {
  return browserApiRequest<AdminOrderResponse>(adminEndpoints.orderStatus(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}
