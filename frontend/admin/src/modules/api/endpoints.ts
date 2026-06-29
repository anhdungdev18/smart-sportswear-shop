export const adminEndpoints = {
  dashboard: "/admin/dashboard",
  revenue: "/admin/analytics/revenue",
  topProducts: "/admin/analytics/top-products",
  stockAlerts: "/admin/inventory/stock-alerts",
  products: "/admin/products",
  productStats: "/admin/products/stats",
  productDetail: (sku: string) => `/admin/products/${sku}`
} as const;
