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
  attributes?: Record<string, string>;
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

export interface BannerItem {
  id: string;
  title?: string;
  subtitle?: string;
  imageUrl: string;
  targetUrl?: string;
  sortOrder: number;
  isActive: boolean;
}

export interface Banner {
  id: string;
  name: string;
  placement: string;
  items: BannerItem[];
}

export interface ProductSuggestion {
  id: string;
  name: string;
  slug: string;
  thumbnail?: string;
  minPrice: number;
}
