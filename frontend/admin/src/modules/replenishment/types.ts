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
}

export interface ReplenishmentSuggestionDetailResponse extends ReplenishmentSuggestionResponse {
  policyLeadTimeDays: number;
  policyTargetCoverDays: number;
  policyServiceLevel: number;
  explanationJson: Record<string, any>;
  historyData: DailyChartData[];
  futureForecastData: DailyChartData[];
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
