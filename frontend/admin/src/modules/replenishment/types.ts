export interface PageResponse<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }
export type ReplenishmentPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ReplenishmentStatus = 'PENDING' | 'ACCEPTED' | 'ADJUSTED' | 'DISMISSED' | 'RECEIVED';

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
  priority: ReplenishmentPriority;
  algorithm: string;
  confidence: string;
  mae: number;
  wape: number;
  status: ReplenishmentStatus;
  createdAt: string;
}

export interface DailyChartData {
  date: string;
  actual: number | null;
  forecast: number | null;
  backtestPeriod: boolean;
}

export interface ForecastModelMetric {
  algorithm: string;
  mae: number;
  wape: number | null;
  selected: boolean;
}

export interface ReplenishmentSuggestionDetailResponse extends ReplenishmentSuggestionResponse {
  policyLeadTimeDays: number;
  policyTargetCoverDays: number;
  policyServiceLevel: number;
  explanationJson: { summary?: string; reasons?: string[]; formula?: Record<string, number>; [key: string]: unknown };
  historyData: DailyChartData[];
  futureForecastData: DailyChartData[];
  modelMetrics: ForecastModelMetric[];
  selectedModel: string;
  selectionReason: string;
}

export interface InventoryPolicyRequest {
  leadTimeDays: number;
  targetCoverDays: number;
  serviceLevel: number;
  minimumOrderQuantity: number;
  packSize: number;
  supplierName: string;
  active: boolean;
}

export interface ReplenishmentActionRequest {
  quantity?: number;
  note?: string;
}

export interface GenerateForecastRequest {
  variantIds?: string[];
}

export interface ForecastGenerationResult {
  requested: number;
  succeeded: number;
  failed: number;
  durationMillis: number;
  failedVariantIds: string[];
}

export type GenerationStatusType = 'IDLE' | 'SYNCING' | 'FORECASTING' | 'COMPLETED' | 'FAILED';

export interface ForecastGenerationStatus {
  status: GenerationStatusType;
  requested: number;
  processed: number;
  succeeded: number;
  failed: number;
  durationMillis: number;
  failedVariantIds: string[];
}