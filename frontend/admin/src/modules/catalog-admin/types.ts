export type CatalogRef = {
  id: string;
  name: string;
};

export type ProductImageResponse = {
  id: string;
  imageUrl: string;
  publicId: string | null;
  altText: string | null;
  isPrimary: boolean;
  sortOrder: number;
};

export type ProductVariantResponse = {
  id: string;
  sku: string;
  size: string | null;
  color: string | null;
  price: number | null;
  compareAtPrice: number | null;
  availableQuantity: number;
  status: string;
};

export type ProductDetailResponse = {
  id: string;
  name: string;
  slug: string;
  shortDescription: string | null;
  description: string | null;
  gender: string | null;
  sportType: string | null;
  brand: CatalogRef | null;
  category: CatalogRef | null;
  images: ProductImageResponse[];
  variants: ProductVariantResponse[];
  status: string;
  isFeatured: boolean;
};

export type CategoryResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  status: string;
};

export type BrandResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  status: string;
};

export type PromotionResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  type: string;
  scope: string;
  status: string;
  discountPercent: number | null;
  discountAmount: number | null;
  minOrderAmount: number | null;
  maxDiscountAmount: number | null;
  startsAt: string | null;
  endsAt: string | null;
  usageLimit: number | null;
  usageCount: number;
  productIds: string[];
  createdAt: string;
  updatedAt: string;
};

export type CouponResponse = {
  id: string;
  promotionId: string;
  code: string;
  description: string | null;
  status: string;
  startsAt: string | null;
  endsAt: string | null;
  usageLimit: number | null;
  usageCount: number;
  perUserLimit: number | null;
  createdAt: string;
  updatedAt: string;
};

export type CollectionResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  shortDescription: string | null;
  collectionType: string;
  season: string | null;
  year: number | null;
  bannerImageUrl: string | null;
  coverImageUrl: string | null;
  status: string;
  startsAt: string | null;
  endsAt: string | null;
  sortOrder: number;
  isFeatured: boolean;
  createdAt: string;
  updatedAt: string;
};

export type ProductImageUploadResponse = {
  id: string;
  imageUrl: string;
  publicId: string | null;
  altText: string | null;
  isPrimary: boolean;
  sortOrder: number;
  width: number | null;
  height: number | null;
};
