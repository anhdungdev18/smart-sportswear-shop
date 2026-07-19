import { browserAiApiRequest } from "@/modules/ai-api/browser-client";
import type { ForecastGenerationResult, GenerateForecastRequest, InventoryPolicyRequest, PageResponse, ReplenishmentActionRequest, ReplenishmentSuggestionDetailResponse, ReplenishmentSuggestionResponse } from "./types";

export async function listSuggestions() {
  return browserAiApiRequest<PageResponse<ReplenishmentSuggestionResponse>>("/api/v1/admin/replenishment/suggestions");
}

export async function getSuggestionDetail(id: string) {
  return browserAiApiRequest<ReplenishmentSuggestionDetailResponse>(`/api/v1/admin/replenishment/suggestions/${id}`);
}

export async function generateForecast(request?: GenerateForecastRequest) {
  return browserAiApiRequest<ForecastGenerationResult>("/api/v1/admin/replenishment/generate", {
    method: "POST",
    body: JSON.stringify(request || {})
  });
}

export async function updatePolicy(variantId: string, request: InventoryPolicyRequest) {
  return browserAiApiRequest<void>(`/api/v1/admin/replenishment/policies/${variantId}`, {
    method: "PUT",
    body: JSON.stringify(request)
  });
}

export async function acceptSuggestion(id: string, request?: ReplenishmentActionRequest) {
  return browserAiApiRequest<void>(`/api/v1/admin/replenishment/suggestions/${id}/accept`, {
    method: "POST",
    body: JSON.stringify(request || {})
  });
}

export async function adjustSuggestion(id: string, request: ReplenishmentActionRequest) {
  return browserAiApiRequest<void>(`/api/v1/admin/replenishment/suggestions/${id}/adjust`, {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export async function dismissSuggestion(id: string, request: ReplenishmentActionRequest) {
  return browserAiApiRequest<void>(`/api/v1/admin/replenishment/suggestions/${id}/dismiss`, {
    method: "POST",
    body: JSON.stringify(request)
  });
}
