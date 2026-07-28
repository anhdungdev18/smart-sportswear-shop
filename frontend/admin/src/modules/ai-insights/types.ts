export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export type DataSource = "DEMO" | "REAL" | "IMPORTED" | string;
export type QualityLevel = "HIGH" | "MEDIUM" | "LOW" | "INSUFFICIENT" | string;
export type ForecastConfidence = "HIGH" | "MEDIUM" | "LOW" | "INSUFFICIENT" | string;
export type InventoryRiskType = "STOCKOUT" | "OVERSTOCK" | "BALANCED" | "INSUFFICIENT_DATA";
export type InventoryRiskLevel = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "NONE";
export type ReplenishmentStatus = "PENDING" | "ACCEPTED" | "ADJUSTED" | "DISMISSED" | "RECEIVED";

export interface DataQualitySourceSummaryResponse {
  dataSource: DataSource;
  totalVariants: number;
  highQualityVariants: number;
  mediumQualityVariants: number;
  lowQualityVariants: number;
  insufficientVariants: number;
  variantsMissingSupplier: number;
  variantsWithMissingSalesDays: number;
  variantsWithInventoryGaps: number;
}

export interface DataQualitySummaryResponse extends Omit<DataQualitySourceSummaryResponse, "dataSource"> {
  bySource: DataQualitySourceSummaryResponse[];
}

export type InventoryAgeingStatus = "NEW_NO_SALES" | "WATCH" | "SLOW_MOVING" | "DORMANT" | "DEAD_STOCK";

export interface InventoryAgeingItemResponse {
  variantId: string; productId: string; sku: string; productName: string; size: string; color: string;
  availableQuantity: number; unitPrice: number; estimatedInventoryValue: number;
  lastImportDate: string; lastSaleDate: string | null; inventoryAgeDays: number; daysWithoutSale: number;
  unitsSold30Days: number; unitsSold90Days: number; unitsSold180Days: number;
  status: InventoryAgeingStatus; urgencyScore: number; supplierConfigured: boolean; recommendedActions: string[];
}

export interface InventoryAgeingSummaryResponse {
  dataSource: DataSource; generatedAt: string; totalVariants: number; variantsWithStock: number;
  newNoSalesVariants: number; watchVariants: number; slowMovingVariants: number;
  dormantVariants: number; deadStockVariants: number; variantsMissingSupplier: number;
  estimatedAtRiskValue: number; items: InventoryAgeingItemResponse[];
}

export interface DemandClassificationResponse {
  variantId: string;
  classification: "NO_DEMAND" | "INSUFFICIENT_DATA" | "NEW_ITEM" | "INTERMITTENT" | "ERRATIC" | "SMOOTH" | "GROWING" | "DECLINING";
}

export interface OverstockMetrics {
  daysOfSupply: number | null;
  deadStockDays: number | null;
  inventoryTurnover: number | null;
  excessQuantity: number;
  excessValue: number | null;
}

export interface InventoryDecisionFormula {
  averageDailyDemand: number;
  leadTimeDays: number;
  targetCoverDays: number;
  residualStdDev: number;
  serviceLevel: number;
  zScore: number;
  incomingQuantity: number;
  expectedDemandDuringLeadTime: number;
  safetyStock: number;
  reorderPoint: number;
  targetStock: number;
  rawSuggestedQuantity: number;
  minimumOrderQuantity: number;
  packSize: number;
  suggestedQuantity: number;
}

export interface InventoryRiskResponse {
  variantId: string;
  productId: string;
  sku: string;
  productName: string;
  size: string;
  color: string;
  risk: InventoryRiskType;
  severity: InventoryRiskLevel;
  availableQuantity: number;
  incomingQuantity: number;
  expectedDemandDuringLeadTime: number;
  safetyStock: number;
  reorderPoint: number;
  suggestedQuantity: number;
  estimatedStockoutDays: number | null;
  estimatedStockoutDateOffsetDays: number | null;
  stockoutProbability: number | null;
  overstock: OverstockMetrics | null;
  confidence: ForecastConfidence;
  selectedModel: string;
  demandPattern: string;
  formula: InventoryDecisionFormula;
  reasons: string[];
  warnings: string[];
  generatedAt: string;
}

export interface ReplenishmentSuggestionResponse {
  id: string;
  variantId: string;
  productId: string;
  sku: string;
  productName: string;
  size: string;
  color: string;
  availableQuantity: number;
  averageDailyDemand: number;
  forecastHorizonDays: number;
  forecastQuantity: number;
  estimatedStockoutDays: number | null;
  reorderPoint: number;
  safetyStock: number;
  suggestedQuantity: number;
  priority: InventoryRiskLevel;
  algorithm: string;
  confidence: ForecastConfidence;
  mae: number;
  wape: number | null;
  status: ReplenishmentStatus;
  createdAt: string;
}

export interface ReplenishmentExplanationResponse {
  recommendationId: string;
  variantId: string;
  decision: InventoryRiskResponse;
  persistedExplanation: Record<string, unknown>;
}

export interface InventorySimulationRequest {
  variantId: string;
  availableQuantity?: number;
  incomingQuantity?: number;
  leadTimeDays?: number;
  serviceLevel?: number;
  targetCoverDays?: number;
  minimumOrderQuantity?: number;
  packSize?: number;
  forecastHorizonDays?: number;
}

export interface InventorySimulationResponse {
  variantId: string;
  current: InventoryRiskResponse;
  simulated: InventoryRiskResponse;
  suggestedQuantityDelta: number;
  reorderPointDelta: number;
  stockoutDaysDelta: number | null;
  warnings: string[];
}
