import { apiRequest, shouldUseMockApi } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import { revenue, stockAlerts, topProducts } from "@/modules/analytics/dashboard-data";

export type RevenuePoint = (typeof revenue)[number];
export type TopProductPoint = (typeof topProducts)[number];
export type StockAlert = (typeof stockAlerts)[number];

export async function getDashboardData() {
  if (!shouldUseMockApi()) {
    return apiRequest<{
      revenue: RevenuePoint[];
      topProducts: TopProductPoint[];
      stockAlerts: StockAlert[];
    }>(adminEndpoints.dashboard, { next: { revalidate: 30 } });
  }

  return { revenue, topProducts, stockAlerts };
}

export async function listStockAlerts() {
  if (!shouldUseMockApi()) {
    return apiRequest<StockAlert[]>(adminEndpoints.stockAlerts, { next: { revalidate: 30 } });
  }

  return stockAlerts;
}
