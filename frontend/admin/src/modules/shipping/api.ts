import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { ShipmentResponse, ShippingMethodResponse } from "@/modules/shipping/types";

export async function listShippingMethods() {
  return apiRequest<ShippingMethodResponse[]>(adminEndpoints.shippingMethods, {
    next: { revalidate: 30 }
  });
}

export async function getOrderShipment(id: string) {
  return apiRequest<ShipmentResponse>(adminEndpoints.orderShipping(id), {
    next: { revalidate: 0 }
  });
}
