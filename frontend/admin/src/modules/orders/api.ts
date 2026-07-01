import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminOrderResponse } from "@/modules/orders/types";

export async function listAdminOrders() {
  return apiRequest<AdminOrderResponse[]>(adminEndpoints.orders, {
    next: { revalidate: 30 }
  });
}
