import { apiRequest, shouldUseMockApi, type ApiQuery } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import { adminProducts, productStats, type AdminProduct } from "@/modules/product-management/products";
import { NO_IMAGE } from "@/modules/ui/placeholder";

export type AdminProductListQuery = ApiQuery & {
  q?: string;
  status?: AdminProduct["status"];
  brand?: string;
  category?: string;
  sort?: "newest" | "best-selling" | "low-stock";
  page?: number;
  limit?: number;
};

type AdminProductListItemResponse = {
  id: string;
  name: string;
  slug: string;
  status: "ACTIVE" | "DRAFT" | "INACTIVE";
  isFeatured: boolean;
  brand: { id: string; name: string } | null;
  category: { id: string; name: string } | null;
  thumbnail: string | null;
  minPrice: number | null;
  maxPrice: number | null;
};

type InventoryItemResponse = {
  variantId: string;
  productId: string;
  productName: string;
  sku: string;
  size: string | null;
  color: string | null;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
};

type ProductReportResponse = {
  bestSelling: Array<{
    productId: string;
    productName: string;
    totalQuantitySold: number;
  }>;
};

function formatVnd(value: number | null | undefined) {
  if (value == null) {
    return "Liên hệ";
  }

  return `${Math.round(value).toLocaleString("vi-VN")} ₫`;
}

function mapAdminStatus(status: AdminProductListItemResponse["status"], stock: number): AdminProduct["status"] {
  if (status !== "ACTIVE") {
    return "draft";
  }

  if (stock <= 0) {
    return "out";
  }

  if (stock <= 10) {
    return "low";
  }

  return "active";
}

// Backend caps page size at 100 (ProductService.MAX_LIMIT). To show the whole
// catalog in the admin, we page through every batch instead of stopping at the
// first 100 — otherwise products beyond the newest page are invisible.
const ADMIN_PAGE_SIZE = 100;

async function fetchAllAdminProducts() {
  const all: AdminProductListItemResponse[] = [];
  for (let page = 1; page <= 100; page += 1) {
    const batch = await apiRequest<AdminProductListItemResponse[]>(adminEndpoints.products, {
      query: { page, limit: ADMIN_PAGE_SIZE },
      next: { revalidate: 30 }
    });
    all.push(...batch);
    if (batch.length < ADMIN_PAGE_SIZE) break;
  }
  return all;
}

async function loadAdminProductDataset(query: AdminProductListQuery = {}) {
  // No explicit page/limit means "give me the full catalog" (stats + client-side
  // pagination both rely on the complete set); a specific page/limit fetches one batch.
  const productsResponse =
    query.page == null && query.limit == null
      ? await fetchAllAdminProducts()
      : await apiRequest<AdminProductListItemResponse[]>(adminEndpoints.products, {
          query: { page: query.page, limit: query.limit ?? ADMIN_PAGE_SIZE },
          next: { revalidate: 30 }
        });

  const [inventoryResponse, productReport] = await Promise.all([
    apiRequest<InventoryItemResponse[]>(adminEndpoints.inventory, {
      query: { limit: 500 },
      next: { revalidate: 30 }
    }).catch(() => [] as InventoryItemResponse[]),
    apiRequest<ProductReportResponse>(adminEndpoints.topProducts, { next: { revalidate: 30 } })
      .catch(() => ({ bestSelling: [] } as ProductReportResponse))
  ]);

  const stockByProduct = new Map<string, { stock: number; sku: string }>();
  for (const item of inventoryResponse) {
    const current = stockByProduct.get(item.productId) ?? { stock: 0, sku: item.sku };
    current.stock += item.availableQuantity;
    if (!current.sku) {
      current.sku = item.sku;
    }
    stockByProduct.set(item.productId, current);
  }

  const soldByProduct = new Map(productReport.bestSelling.map((item) => [item.productId, item.totalQuantitySold]));

  return productsResponse.map((item) => {
    const stockEntry = stockByProduct.get(item.id);
    const stock = stockEntry?.stock ?? 0;
    const minPrice = item.minPrice ?? item.maxPrice ?? 0;
    const maxPrice = item.maxPrice && item.maxPrice > minPrice ? item.maxPrice : null;

    return {
      id: item.id,
      sku: stockEntry?.sku ?? item.slug,
      name: item.name,
      category: item.category?.name ?? "Chưa phân loại",
      brand: item.brand?.name ?? "Chưa có brand",
      price: maxPrice ? `${formatVnd(minPrice)} - ${formatVnd(maxPrice)}` : formatVnd(minPrice),
      stock,
      sold: soldByProduct.get(item.id) ?? 0,
      status: mapAdminStatus(item.status, stock),
      isFeatured: item.isFeatured,
      image: item.thumbnail ?? NO_IMAGE
    } satisfies AdminProduct;
  });
}

export async function listAdminProducts(query: AdminProductListQuery = {}) {
  if (shouldUseMockApi()) {
    return adminProducts;
  }

  return loadAdminProductDataset(query);
}

export function buildAdminProductStats(items: AdminProduct[]) {
  const active = items.filter((item) => item.status === "active").length;
  const low = items.filter((item) => item.status === "low").length;
  const out = items.filter((item) => item.status === "out").length;

  return [
    { label: "Tổng sản phẩm", value: String(items.length), tone: "neutral" },
    { label: "Đang bán", value: String(active), tone: "success" },
    { label: "Sắp hết hàng", value: String(low), tone: "warning" },
    { label: "Hết hàng", value: String(out), tone: "danger" }
  ] as const;
}

export async function getAdminProductStats() {
  if (shouldUseMockApi()) {
    return productStats;
  }

  const items = await loadAdminProductDataset({ limit: 100 });
  return buildAdminProductStats(items);
}
