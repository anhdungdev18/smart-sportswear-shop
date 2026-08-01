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

export type RevenueGranularity = "DAY" | "MONTH" | "YEAR";

export type RevenueReportResponse = {
  granularity: RevenueGranularity;
  dateFrom: string;
  dateTo: string;
  points: Array<{ label: string; date: string; revenue: number; orders: number }>;
};

export type ProductReportResponse = {
  dateFrom: string | null;
  dateTo: string | null;
  bestSelling: Array<{
    productId: string;
    productName: string;
    totalQuantitySold: number;
    totalRevenue: number;
  }>;
  meta: PageMeta;
};

export type CustomerReportResponse = {
  dateFrom: string | null;
  dateTo: string | null;
  customers: Array<{
    customerId: string;
    customerName: string;
    email: string;
    totalOrders: number;
    totalRevenue: number;
  }>;
  meta: PageMeta;
};

export type ReportFilters = { dateFrom?: string; dateTo?: string };
export type PageMeta = { page: number; limit: number; total: number; totalPages: number };

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

export async function getRevenueReport(granularity: RevenueGranularity = "MONTH", filters: ReportFilters = {}) {
  return apiRequest<RevenueReportResponse>(adminEndpoints.revenueReport, {
    query: { granularity, ...filters }, next: { revalidate: 30 }
  });
}

export async function getProductReport(filters: ReportFilters = {}) {
  return apiRequest<ProductReportResponse>(adminEndpoints.topProducts, {
    query: { page: 1, limit: 10, ...filters }, next: { revalidate: 30 }
  });
}

export async function getCustomerReport(filters: ReportFilters = {}) {
  return apiRequest<CustomerReportResponse>(adminEndpoints.customerReport, {
    query: { page: 1, limit: 10, ...filters }, next: { revalidate: 30 }
  });
}
