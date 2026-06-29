import { apiRequest, shouldUseMockApi, type ApiQuery } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import { adminProducts, productStats, type AdminProduct } from "@/modules/product-management/products";

export type AdminProductListQuery = ApiQuery & {
  q?: string;
  status?: AdminProduct["status"];
  brand?: string;
  category?: string;
  sort?: "newest" | "best-selling" | "low-stock";
  page?: number;
  limit?: number;
};

export async function listAdminProducts(query: AdminProductListQuery = {}) {
  if (!shouldUseMockApi()) {
    return apiRequest<AdminProduct[]>(adminEndpoints.products, { query, next: { revalidate: 30 } });
  }

  return adminProducts;
}

export async function getAdminProductStats() {
  if (!shouldUseMockApi()) {
    return apiRequest<typeof productStats>(adminEndpoints.productStats, { next: { revalidate: 30 } });
  }

  return productStats;
}
