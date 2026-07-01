export interface PageMeta {
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface CatalogRef {
  id: string;
  name: string;
  slug: string;
}

export interface ProductListItem {
  id: string;
  name: string;
  slug: string;
  shortDescription?: string;
  brand?: CatalogRef;
  category?: CatalogRef;
  thumbnail?: string;
  minPrice: number;
  maxPrice: number;
  status: string;
  productType: string;
}

export interface ProductVariant {
  id: string;
  sku?: string;
  size?: string;
  color?: string;
  price: number;
  compareAtPrice?: number;
  availableQuantity: number;
  status: string;
}

export interface ProductImage {
  id: string;
  imageUrl: string;
  publicId?: string;
  altText?: string;
  isPrimary: boolean;
  sortOrder: number;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingPercentage: number;
}

export interface ProductDetail {
  id: string;
  name: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  gender?: string;
  sportType?: string;
  productType: string;
  brand?: CatalogRef;
  category?: CatalogRef;
  images: ProductImage[];
  variants: ProductVariant[];
  status: string;
  isFeatured: boolean;
  reviewSummary?: ReviewSummary;
  relatedProducts: ProductListItem[];
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
}

export interface ProductSuggestion {
  id: string;
  name: string;
  slug: string;
  thumbnail?: string;
  minPrice: number;
}
