import { apiRequest, shouldUseMockApi, type ApiQuery } from "@/modules/api/client";
import { storefrontEndpoints } from "@/modules/api/endpoints";
import { blogDetails, getBlogDetail, mockImages } from "@/modules/catalog/mockContent";
import {
  blogPosts,
  brandTiles,
  heroSlides,
  popularCategories,
  productTabs,
  products,
  quickCategories,
  type Product
} from "@/modules/catalog/products";

export type ProductListQuery = ApiQuery & {
  q?: string;
  collection?: string;
  brand?: string | string[];
  size?: string | string[];
  minPrice?: number;
  maxPrice?: number;
  sort?: "manual" | "price-ascending" | "price-descending" | "title-ascending" | "title-descending" | "created-ascending" | "created-descending" | "best-selling";
  page?: number;
  limit?: number;
};

export type StorefrontHomeData = {
  heroSlides: typeof heroSlides;
  quickCategories: typeof quickCategories;
  popularCategories: typeof popularCategories;
  brandTiles: typeof brandTiles;
  productTabs: typeof productTabs;
  blogPosts: typeof blogPosts;
  featuredProducts: Product[];
  hotDeals: Product[];
  hotSale: Product[];
  images: typeof mockImages;
};

function normalize(value: string) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

function searchMockProducts(query: string) {
  const keyword = normalize(query.trim());
  if (!keyword) return products;

  return products.filter((product) =>
    [product.name, product.brand, product.category, product.tag, product.description].some((value) => normalize(value).includes(keyword))
  );
}

export async function getStorefrontHomeData(): Promise<StorefrontHomeData> {
  if (!shouldUseMockApi()) {
    return apiRequest<StorefrontHomeData>(storefrontEndpoints.home, { next: { revalidate: 60 } });
  }

  return {
    heroSlides,
    quickCategories,
    popularCategories,
    brandTiles,
    productTabs,
    blogPosts,
    featuredProducts: products.slice(0, 4),
    hotDeals: products.slice(0, 8),
    hotSale: [...products].reverse().slice(0, 8),
    images: mockImages
  };
}

export async function listStorefrontProducts(query: ProductListQuery = {}) {
  if (!shouldUseMockApi()) {
    return apiRequest<Product[]>(storefrontEndpoints.products, { query, next: { revalidate: 60 } });
  }

  if (query.q) return searchMockProducts(query.q);
  return products;
}

export async function getStorefrontProduct(slug: string) {
  if (!shouldUseMockApi()) {
    return apiRequest<Product | null>(storefrontEndpoints.productDetail(slug), { next: { revalidate: 60 } });
  }

  return products.find((product) => product.slug === slug) ?? null;
}

export async function listStorefrontProductSlugs() {
  if (!shouldUseMockApi()) {
    const apiProducts = await listStorefrontProducts({ limit: 1000 });
    return apiProducts.map((product) => ({ slug: product.slug }));
  }

  return products.map((product) => ({ slug: product.slug }));
}

export async function searchStorefrontProducts(q: string) {
  if (!shouldUseMockApi()) {
    return apiRequest<Product[]>(storefrontEndpoints.searchProducts, { query: { q }, next: { revalidate: 30 } });
  }

  return searchMockProducts(q);
}

export async function listBlogPosts() {
  if (!shouldUseMockApi()) {
    return apiRequest<typeof blogDetails>(storefrontEndpoints.blogs, { next: { revalidate: 300 } });
  }

  return blogDetails;
}

export async function getStorefrontBlogPost(slug: string) {
  if (!shouldUseMockApi()) {
    return apiRequest<(typeof blogDetails)[number] | null>(storefrontEndpoints.blogDetail(slug), { next: { revalidate: 300 } });
  }

  return getBlogDetail(slug) ?? null;
}
