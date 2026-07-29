import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type {
  CustomerReportResponse,
  ProductReportResponse,
  ReportFilters,
  RevenueGranularity,
  RevenueReportResponse
} from "@/modules/reports/api";

export async function fetchReports(filters: ReportFilters, granularity: RevenueGranularity) {
  return Promise.all([
    browserApiRequest<RevenueReportResponse>(adminEndpoints.revenueReport, {
      query: { granularity, ...filters }
    }),
    browserApiRequest<CustomerReportResponse>(adminEndpoints.customerReport, {
      query: { page: 1, limit: 10, ...filters }
    }),
    browserApiRequest<ProductReportResponse>(adminEndpoints.topProducts, {
      query: { page: 1, limit: 10, ...filters }
    })
  ]);
}

export function fetchCustomerReport(filters: ReportFilters, page: number) {
  return browserApiRequest<CustomerReportResponse>(adminEndpoints.customerReport, {
    query: { page, limit: 10, ...filters }
  });
}

export function fetchProductReport(filters: ReportFilters, page: number) {
  return browserApiRequest<ProductReportResponse>(adminEndpoints.topProducts, {
    query: { page, limit: 10, ...filters }
  });
}
