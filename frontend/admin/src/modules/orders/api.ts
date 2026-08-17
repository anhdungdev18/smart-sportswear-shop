import { apiRequestEnvelope } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminOrderPage, AdminOrderResponse, PageMeta } from "@/modules/orders/types";

export type AdminOrderListParams = {
  page?: number;
  limit?: number;
  keyword?: string;
  customerId?: string;
  status?: string;
};

export async function listAdminOrders(query: AdminOrderListParams = {}): Promise<AdminOrderPage> {
  const response = await apiRequestEnvelope<AdminOrderResponse[]>(adminEndpoints.orders, {
    query,
    cache: "no-store"
  });
  const page = query.page ?? 1;
  const limit = query.limit ?? 20;
  return { items: response.data, meta: { page, limit, total: response.data.length, totalPages: 1, ...(response.meta as Partial<PageMeta> | undefined) } };
}
