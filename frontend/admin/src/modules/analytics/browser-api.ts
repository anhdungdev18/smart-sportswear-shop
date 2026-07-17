import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { RevenueGranularity, RevenueReport } from "@/modules/analytics/api";

/** Client-side revenue fetch used by the dashboard granularity filter. */
export async function fetchRevenueReport(granularity: RevenueGranularity): Promise<RevenueReport> {
  return browserApiRequest<RevenueReport>(adminEndpoints.revenueReport, {
    method: "GET",
    query: { granularity }
  });
}
