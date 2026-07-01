import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { ShipmentResponse } from "@/modules/shipping/types";

export async function fetchOrderShipment(id: string) {
  return browserApiRequest<ShipmentResponse>(adminEndpoints.orderShipping(id), {
    method: "GET"
  });
}

export async function updateOrderShipment(id: string, input: Record<string, unknown>) {
  return browserApiRequest<ShipmentResponse>(adminEndpoints.orderShipping(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}
