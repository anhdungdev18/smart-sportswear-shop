import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { Category, CollectionDetail, CollectionSummary, PageMeta } from "@/modules/category/types";
import type { ProductListItem } from "@/modules/product/types";

const DEFAULT_PAGE_META: PageMeta = {
  page: 1,
  size: 20,
  total: 0,
  totalPages: 1,
};

type CategoryListingQuery = {
  categorySlug: string;
  page: number;
  limit: number;
  sort?: string;
  sortBy?: string;
  sortOrder?: string;
  size?: string;
  color?: string;
  minPrice?: string;
  maxPrice?: string;
  surface?: string;
};

export async function fetchCategoryListing(query: CategoryListingQuery) {
  try {
    const result = await apiFetch<ProductListItem[]>(endpoints.products, { query });
    return {
      products: result.data,
      meta: (result.meta as PageMeta | undefined) ?? { ...DEFAULT_PAGE_META, size: query.limit, page: query.page },
    };
  } catch {
    return {
      products: [] as ProductListItem[],
      meta: { ...DEFAULT_PAGE_META, size: query.limit, page: query.page },
    };
  }
}

export async function fetchCategoryDetail(slug: string): Promise<Category | null> {
  try {
    const result = await apiFetch<Category>(endpoints.category(slug));
    return result.data;
  } catch {
    return null;
  }
}

export async function fetchCategoryTree(): Promise<Category[]> {
  try {
    const result = await apiFetch<Category[]>(endpoints.categoryTree, {
      next: { revalidate: 300, tags: ["category-tree"] },
    });
    return result.data ?? [];
  } catch {
    return [];
  }
}

export async function fetchSearchResults(query: string, page: number, limit: number) {
  if (!query) {
    return {
      products: [] as ProductListItem[],
      meta: { ...DEFAULT_PAGE_META, page, size: limit },
    };
  }

  try {
    const result = await apiFetch<ProductListItem[]>(endpoints.products, {
      query: { q: query, page, limit },
    });
    return {
      products: result.data,
      meta: (result.meta as PageMeta | undefined) ?? { ...DEFAULT_PAGE_META, page, size: limit },
    };
  } catch {
    return {
      products: [] as ProductListItem[],
      meta: { ...DEFAULT_PAGE_META, page, size: limit },
    };
  }
}

export async function fetchCollections() {
  try {
    const result = await apiFetch<CollectionSummary[]>(endpoints.collections);
    return result.data ?? [];
  } catch {
    return [] as CollectionSummary[];
  }
}

export async function fetchCollectionDetail(slug: string): Promise<CollectionDetail | null> {
  try {
    const result = await apiFetch<CollectionDetail>(endpoints.collection(slug));
    return result.data;
  } catch {
    return null;
  }
}
