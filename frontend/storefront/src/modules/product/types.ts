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
  availableQuantity: number;
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
  color?: string | null;
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

export interface VisualSearchResult {
  product: ProductListItem;
  matchedImageId: string;
  matchedImageUrl: string;
  similarity: number;
}

export interface ProductColorSwatch {
  id: string;
  image: string;
  label: string;
  active?: boolean;
}

export interface ProductSize {
  id: string;
  label: string;
  disabled?: boolean;
}

export interface Product {
  id: string;
  name: string;
  href: string;
  image: string;
  hoverImage: string;
  price: number;
  oldPrice?: number;
  discountPercent?: number;
  ribbon?: "new" | "bestseller";
  isOutOfStock: boolean;
  colors: ProductColorSwatch[];
  sizes: ProductSize[];
}

export interface ProductTab {
  id: string;
  label: string;
  categorySlug: string;
  products: Product[];
}

export interface ProductCarouselSectionData {
  title: string;
  tabs?: ProductTab[];
  products?: Product[];
}
