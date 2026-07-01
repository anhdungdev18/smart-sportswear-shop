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

async function loadAdminProductDataset(query: AdminProductListQuery = {}) {
  const [productsResponse, inventoryResponse, productReport] = await Promise.all([
    apiRequest<AdminProductListItemResponse[]>(adminEndpoints.products, {
      query: {
        page: query.page,
        limit: query.limit ?? 50
      },
      next: { revalidate: 30 }
    }),
    apiRequest<InventoryItemResponse[]>(adminEndpoints.inventory, {
      query: { limit: 500 },
      next: { revalidate: 30 }
    }),
    apiRequest<ProductReportResponse>(adminEndpoints.topProducts, { next: { revalidate: 30 } })
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
      image: item.thumbnail ?? "https://placehold.co/96x96/f5f5f5/202020?text=SP"
    } satisfies AdminProduct;
  });
}

export async function listAdminProducts(query: AdminProductListQuery = {}) {
  if (!shouldUseMockApi()) {
    try {
      return await loadAdminProductDataset(query);
    } catch {
      return adminProducts;
    }
  }

  return adminProducts;
}

export async function getAdminProductStats() {
  if (!shouldUseMockApi()) {
    try {
      const items = await loadAdminProductDataset({ limit: 100 });
      const active = items.filter((item) => item.status === "active").length;
      const low = items.filter((item) => item.status === "low").length;
      const out = items.filter((item) => item.status === "out").length;

      return [
        { label: "Tổng sản phẩm", value: String(items.length), tone: "neutral" },
        { label: "Đang bán", value: String(active), tone: "success" },
        { label: "Sắp hết hàng", value: String(low), tone: "warning" },
        { label: "Hết hàng", value: String(out), tone: "danger" }
      ];
    } catch {
      return productStats;
    }
  }

  return productStats;
}
