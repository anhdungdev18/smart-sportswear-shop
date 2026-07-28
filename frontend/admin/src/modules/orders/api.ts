import { apiRequestEnvelope } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminOrderResponse } from "@/modules/orders/types";

export type AdminOrderListParams = {
  page?: number;
  limit?: number;
  keyword?: string;
  status?: string;
};

export type OrderPageMeta = {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
};

export async function listAdminOrders(query: AdminOrderListParams = {}) {
  const response = await apiRequestEnvelope<AdminOrderResponse[]>(adminEndpoints.orders, {
    query,
    next: { revalidate: 30 }
  });

  return {
    orders: response.data,
    meta: (response.meta ?? { page: query.page ?? 1, limit: query.limit ?? 20, total: response.data.length, totalPages: 1 }) as OrderPageMeta
  };
}
