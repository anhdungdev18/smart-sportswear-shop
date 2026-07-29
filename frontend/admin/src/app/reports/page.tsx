import { ReportsWorkspace } from "@/components/reports/ReportsWorkspace";
import {
  getCustomerReport,
  getOverviewReport,
  getProductReport,
  getRevenueReport
} from "@/modules/reports/api";

export default async function ReportsPage() {
  const [overview, revenue, customers, products] = await Promise.all([
    getOverviewReport().catch(() => ({ grossRevenue: 0, realizedRevenue: 0, totalOrders: 0, pendingOrders: 0, lowStockCount: 0 })),
    getRevenueReport("MONTH").catch(() => ({ granularity: "MONTH" as const, dateFrom: "", dateTo: "", points: [] })),
    getCustomerReport().catch(() => ({ dateFrom: null, dateTo: null, customers: [], meta: { page: 1, limit: 10, total: 0, totalPages: 0 } })),
    getProductReport().catch(() => ({ dateFrom: null, dateTo: null, bestSelling: [], meta: { page: 1, limit: 10, total: 0, totalPages: 0 } }))
  ]);

  return <ReportsWorkspace overview={overview} initialRevenue={revenue} initialCustomers={customers} initialProducts={products} />;
}
