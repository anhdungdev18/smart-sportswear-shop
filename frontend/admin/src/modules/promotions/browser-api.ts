import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { PromotionInput, PromotionResponse, PromotionStatus } from "@/modules/promotions/types";

export function listPromotions() {
  return browserApiRequest<PromotionResponse[]>(adminEndpoints.promotions, { method: "GET" });
}

export function createPromotion(input: PromotionInput) {
  return browserApiRequest<PromotionResponse>(adminEndpoints.promotions, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export function updatePromotion(id: string, input: PromotionInput) {
  return browserApiRequest<PromotionResponse>(adminEndpoints.promotion(id), {
    method: "PUT",
    body: JSON.stringify(input)
  });
}

export function updatePromotionStatus(id: string, status: PromotionStatus) {
  return browserApiRequest<PromotionResponse>(adminEndpoints.promotionStatus(id), {
    method: "PATCH",
    body: JSON.stringify({ status })
  });
}

export function deletePromotion(id: string) {
  return browserApiRequest<void>(adminEndpoints.promotion(id), { method: "DELETE" });
}
