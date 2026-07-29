import { apiRequestEnvelope } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminOrderPage, AdminOrderResponse, PageMeta } from "@/modules/orders/types";

export async function listAdminOrders(page = 1, limit = 20) : Promise<AdminOrderPage> {
  const response = await apiRequestEnvelope<AdminOrderResponse[]>(adminEndpoints.orders, {
    query: { page, limit }, cache: "no-store"
  });
  return { items: response.data, meta: { page, limit, total: 0, totalPages: 0, ...(response.meta as Partial<PageMeta> | undefined) } };
}
