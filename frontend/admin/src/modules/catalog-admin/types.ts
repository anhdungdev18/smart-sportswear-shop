export type CatalogRef = {
  id: string;
  name: string;
};

export type ProductImageResponse = {
  id: string;
  imageUrl: string;
  publicId: string | null;
  altText: string | null;
  color: string | null;
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
  parentId?: string | null;
  parentName?: string | null;
  parentSlug?: string | null;
  nodeType?: "GROUP" | "LEAF";
  sortOrder?: number;
};

export type BrandResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  status: string;
};

export type CollectionResponse = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  shortDescription: string | null;
  collectionType: string;
  brand: { id: string; name: string } | null;
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

export type ComboProductResponse = {
  productId: string;
  productName: string;
  quantity: number;
};

export type ComboResponse = {
  id: string;
  name: string;
  description: string | null;
  discountAmount: number;
  status: "ACTIVE" | "INACTIVE";
  products: ComboProductResponse[];
  createdAt: string;
  updatedAt: string;
};

export type ComboInput = {
  name: string;
  description?: string | null;
  discountAmount: number;
  status?: "ACTIVE" | "INACTIVE";
  productIds: string[];
};
