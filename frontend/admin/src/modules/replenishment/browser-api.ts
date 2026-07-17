import { browserApiRequest } from "@/modules/api/browser-client";
import type { GenerateForecastRequest, InventoryPolicyRequest, ReplenishmentActionRequest, ReplenishmentSuggestionDetailResponse } from "./types";

export async function getSuggestionDetail(id: string) {
  return browserApiRequest<ReplenishmentSuggestionDetailResponse>(`/api/v1/admin/replenishment/suggestions/${id}`);
}

export async function generateForecast(request?: GenerateForecastRequest) {
  return browserApiRequest<void>("/api/v1/admin/replenishment/generate", {
    method: "POST",
    body: JSON.stringify(request || {})
  });
}

export async function updatePolicy(variantId: string, request: InventoryPolicyRequest) {
  return browserApiRequest<void>(`/api/v1/admin/replenishment/policies/${variantId}`, {
    method: "PUT",
    body: JSON.stringify(request)
  });
}

export async function acceptSuggestion(id: string, request?: ReplenishmentActionRequest) {
  return browserApiRequest<void>(`/api/v1/admin/replenishment/suggestions/${id}/accept`, {
    method: "POST",
    body: JSON.stringify(request || {})
  });
}

export async function adjustSuggestion(id: string, request: ReplenishmentActionRequest) {
  return browserApiRequest<void>(`/api/v1/admin/replenishment/suggestions/${id}/adjust`, {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export async function dismissSuggestion(id: string, request: ReplenishmentActionRequest) {
  return browserApiRequest<void>(`/api/v1/admin/replenishment/suggestions/${id}/dismiss`, {
    method: "POST",
    body: JSON.stringify(request)
  });
}
