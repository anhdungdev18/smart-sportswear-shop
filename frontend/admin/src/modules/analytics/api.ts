import { apiRequest, shouldUseMockApi } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import { revenue, stockAlerts, topProducts } from "@/modules/analytics/dashboard-data";

type OverviewReportResponse = {
  grossRevenue: number;
  realizedRevenue: number;
  totalOrders: number;
  pendingOrders: number;
  lowStockCount: number;
};

export async function getOverviewReport(): Promise<OverviewReportResponse | null> {
  try {
    return await apiRequest<OverviewReportResponse>(adminEndpoints.overview, { next: { revalidate: 30 } });
  } catch {
    return null;
  }
}

export type RevenuePoint = (typeof revenue)[number];
export type TopProductPoint = (typeof topProducts)[number];
export type StockAlert = (typeof stockAlerts)[number];

type ProductReportResponse = {
  bestSelling: Array<{
    productId: string;
    productName: string;
    totalQuantitySold: number;
    totalRevenue: number;
  }>;
};

type InventoryReportResponse = {
  lowStockThreshold: number;
  lowStockItems: Array<{
    variantId: string;
    sku: string;
    productName: string;
    stockQuantity: number;
    reservedQuantity: number;
    availableQuantity: number;
  }>;
};

export async function getDashboardData() {
  if (!shouldUseMockApi()) {
    const [productReport, inventoryReport] = await Promise.all([
      apiRequest<ProductReportResponse>(adminEndpoints.topProducts, { next: { revalidate: 30 } }),
      apiRequest<InventoryReportResponse>(adminEndpoints.inventoryReport, { next: { revalidate: 30 } })
    ]);

    return {
      revenue,
      topProducts: productReport.bestSelling.slice(0, 4).map((item) => ({
        name: item.productName,
        sales: item.totalQuantitySold
      })),
      stockAlerts: inventoryReport.lowStockItems.slice(0, 8).map((item) => ({
        sku: item.sku,
        product: item.productName,
        stock: item.availableQuantity,
        status: item.availableQuantity <= Math.max(1, Math.floor(inventoryReport.lowStockThreshold / 2)) ? "critical" : "low"
      }))
    };
  }

  return { revenue, topProducts, stockAlerts };
}

export async function listStockAlerts() {
  if (!shouldUseMockApi()) {
    const inventoryReport = await apiRequest<InventoryReportResponse>(adminEndpoints.inventoryReport, { next: { revalidate: 30 } });
    return inventoryReport.lowStockItems.slice(0, 8).map((item) => ({
      sku: item.sku,
      product: item.productName,
      stock: item.availableQuantity,
      status: item.availableQuantity <= Math.max(1, Math.floor(inventoryReport.lowStockThreshold / 2)) ? "critical" : "low"
    }));
  }

  return stockAlerts;
}
