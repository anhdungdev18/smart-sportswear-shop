import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { BannerSlide } from "@/components/marketing/types";
import type { Banner, ProductDetail, ProductListItem } from "@/modules/product/types";

type ProductQuery = Record<string, string | number | boolean | undefined>;

export async function fetchProducts(query: ProductQuery): Promise<ProductListItem[]> {
  try {
    const result = await apiFetch<ProductListItem[]>(endpoints.products, {
      query,
      cache: "no-store"
    });
    return result.data ?? [];
  } catch {
    return [];
  }
}

export async function fetchNewestProducts(limit = 12) {
  return fetchProducts({ limit, sortBy: "createdAt", sortOrder: "desc" });
}

export async function fetchFeaturedProducts(limit = 12) {
  return fetchProducts({ featured: true, limit, sortBy: "createdAt", sortOrder: "desc" });
}

export async function fetchProductDetail(slug: string): Promise<ProductDetail | null> {
  try {
    const result = await apiFetch<ProductDetail>(endpoints.product(slug), {
      cache: "no-store"
    });
    return result.data;
  } catch {
    return null;
  }
}

type RecommendationResponse = {
  type: string;
  items: Array<{ product: ProductListItem }>;
};

// "Frequently bought together" (association-rules engine). Returns the recommended
// products for a given product; empty on any error so the section just hides.
export async function fetchFrequentlyBoughtTogether(productId: string): Promise<ProductListItem[]> {
  try {
    const result = await apiFetch<RecommendationResponse>(endpoints.frequentlyBoughtTogether(productId), {
      cache: "no-store"
    });
    return (result.data?.items ?? []).map((item) => item.product).filter(Boolean);
  } catch {
    return [];
  }
}

export async function fetchHomeBannerSlides(): Promise<BannerSlide[]> {
  try {
    const result = await apiFetch<Banner[]>(endpoints.banners, {
      query: { placement: "HOME_HERO" },
      cache: "no-store"
    });
    const hero = result.data.find((banner) => banner.placement === "HOME_HERO");
    if (!hero) return [];

    return hero.items
      .filter((item) => item.isActive)
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((item) => ({
        image: item.imageUrl,
        href: item.targetUrl ?? "/",
        alt: item.title ?? "Banner",
      }));
  } catch {
    return [];
  }
}
