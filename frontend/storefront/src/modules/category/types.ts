import type { ProductListItem } from "@/modules/product/types";

export interface PageMeta {
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
  parentName?: string;
  parentSlug?: string;
  nodeType?: "GROUP" | "LEAF";
  sortOrder?: number;
  children?: Category[];
}

export interface CollectionSummary {
  id: string;
  name: string;
  slug: string;
  shortDescription?: string;
  season?: string;
  year?: number;
  coverImageUrl?: string;
  bannerImageUrl?: string;
  isFeatured: boolean;
}

export interface CollectionDetail {
  id: string;
  name: string;
  slug: string;
  description?: string;
  shortDescription?: string;
  season?: string;
  year?: number;
  coverImageUrl?: string;
  bannerImageUrl?: string;
  products: ProductListItem[];
}
