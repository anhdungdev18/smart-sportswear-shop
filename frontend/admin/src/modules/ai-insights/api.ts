import { aiApiRequest } from "@/modules/ai-api/client";
import type {
  DataQualitySummaryResponse,
  InventoryRiskResponse,
  PageResponse,
  ReplenishmentSuggestionResponse,
} from "./types";

export async function getDataQualitySummary() {
  return aiApiRequest<DataQualitySummaryResponse>("/api/v1/admin/ai/data-quality/summary", {
    next: { revalidate: 0 },
  });
}

export async function listInventoryRisks() {
  return aiApiRequest<InventoryRiskResponse[]>("/api/v1/admin/ai/inventory-risks", {
    next: { revalidate: 0 },
  });
}

export async function listPendingSuggestions() {
  return aiApiRequest<PageResponse<ReplenishmentSuggestionResponse>>("/api/v1/admin/replenishment/suggestions", {
    query: { status: "PENDING", limit: 100 },
    next: { revalidate: 0 },
  });
}
