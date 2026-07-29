import { browserAiApiRequest } from "@/modules/ai-api/browser-client";
import type {
  DataQualitySummaryResponse,
  InventoryRiskResponse,
  InventoryRiskType,
  InventorySimulationRequest,
  InventorySimulationResponse,
  ReplenishmentExplanationResponse,
} from "./types";

export async function getDataQualitySummary() {
  return browserAiApiRequest<DataQualitySummaryResponse>("/api/v1/admin/ai/data-quality/summary");
}

export async function listInventoryRisks(risk?: InventoryRiskType | "ALL") {
  return browserAiApiRequest<InventoryRiskResponse[]>("/api/v1/admin/ai/inventory-risks", {
    query: risk && risk !== "ALL" ? { risk } : undefined,
  });
}

export async function getInventoryRisk(variantId: string) {
  return browserAiApiRequest<InventoryRiskResponse>(`/api/v1/admin/ai/inventory-risks/${variantId}`);
}

export async function getReplenishmentExplanation(recommendationId: string) {
  return browserAiApiRequest<ReplenishmentExplanationResponse>(
    `/api/v1/admin/ai/replenishment/explanations/${recommendationId}`,
  );
}

export async function simulateInventoryPolicy(request: InventorySimulationRequest) {
  return browserAiApiRequest<InventorySimulationResponse>("/api/v1/admin/ai/inventory/simulate", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
