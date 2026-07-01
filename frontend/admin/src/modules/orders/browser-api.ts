import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminOrderResponse } from "@/modules/orders/types";

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
