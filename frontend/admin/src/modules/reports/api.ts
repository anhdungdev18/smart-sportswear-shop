import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";

export type OverviewReportResponse = {
  grossRevenue: number;
  realizedRevenue: number;
  totalOrders: number;
  pendingOrders: number;
  lowStockCount: number;
};

export type OrderStatusCount = {
  status: string;
  count: number;
};

export type OrderReportResponse = {
  dateFrom: string | null;
  dateTo: string | null;
  totalOrders: number;
  byStatus: OrderStatusCount[];
};

export async function getOverviewReport() {
  return apiRequest<OverviewReportResponse>(adminEndpoints.overview, {
    next: { revalidate: 30 }
  });
}

export async function getOrderReport() {
  return apiRequest<OrderReportResponse>(adminEndpoints.orderReport, {
    next: { revalidate: 30 }
  });
}
