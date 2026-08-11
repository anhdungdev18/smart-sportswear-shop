import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { PromotionResponse } from "@/modules/promotions/types";

export function getPromotions() {
  return apiRequest<PromotionResponse[]>(adminEndpoints.promotions, { next: { revalidate: 30 } });
}
