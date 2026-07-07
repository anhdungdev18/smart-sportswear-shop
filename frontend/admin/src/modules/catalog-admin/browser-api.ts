import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type {
  BrandResponse,
  CategoryResponse,
  CollectionResponse,
  CouponResponse,
  ProductDetailResponse,
  ProductImageResponse,
  ProductImageUploadResponse,
  ProductVariantResponse,
  PromotionResponse
} from "@/modules/catalog-admin/types";

export async function createAdminProduct(input: Record<string, unknown>) {
  return browserApiRequest<ProductDetailResponse>(adminEndpoints.products, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function fetchAdminProductDetail(id: string) {
  return browserApiRequest<ProductDetailResponse>(adminEndpoints.productDetail(id), {
    method: "GET"
  });
}

export async function updateAdminProduct(id: string, input: Record<string, unknown>) {
  return browserApiRequest<ProductDetailResponse>(adminEndpoints.productDetail(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function createVariant(productId: string, input: Record<string, unknown>) {
  return browserApiRequest<ProductVariantResponse>(adminEndpoints.productVariants(productId), {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateVariant(id: string, input: Record<string, unknown>) {
  return browserApiRequest<ProductVariantResponse>(adminEndpoints.variant(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function addProductImage(productId: string, input: Record<string, unknown>) {
  return browserApiRequest<ProductImageResponse>(adminEndpoints.productImages(productId), {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function uploadProductImage(productId: string, formData: FormData) {
  return browserApiRequest<ProductImageUploadResponse>(adminEndpoints.productImageUpload(productId), {
    method: "POST",
    body: formData
  });
}

export async function deleteProductImage(productId: string, imageId: string) {
  return browserApiRequest<ProductImageResponse[]>(adminEndpoints.productImageDelete(productId, imageId), {
    method: "DELETE"
  });
}

export async function createCategory(input: Record<string, unknown>) {
  return browserApiRequest<CategoryResponse>(adminEndpoints.adminCategories, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateCategory(id: string, input: Record<string, unknown>) {
  return browserApiRequest<CategoryResponse>(adminEndpoints.adminCategory(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function createBrand(input: Record<string, unknown>) {
  return browserApiRequest<BrandResponse>(adminEndpoints.adminBrands, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateBrand(id: string, input: Record<string, unknown>) {
  return browserApiRequest<BrandResponse>(adminEndpoints.adminBrand(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function createPromotion(input: Record<string, unknown>) {
  return browserApiRequest<PromotionResponse>(adminEndpoints.promotions, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updatePromotion(id: string, input: Record<string, unknown>) {
  return browserApiRequest<PromotionResponse>(adminEndpoints.promotion(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export type ProductPickItem = { id: string; name: string; slug: string; status: string };

export async function listProductsForPicker() {
  return browserApiRequest<ProductPickItem[]>(`${adminEndpoints.products}?limit=200`, { method: "GET" });
}

export async function listCollectionProducts(collectionId: string) {
  return browserApiRequest<ProductPickItem[]>(adminEndpoints.collectionProducts(collectionId), { method: "GET" });
}

export async function listPromotionsForPicker() {
  return browserApiRequest<PromotionResponse[]>(`${adminEndpoints.promotions}?limit=200`, { method: "GET" });
}

export async function listCouponsForAdmin() {
  return browserApiRequest<CouponResponse[]>(`${adminEndpoints.coupons}?limit=200`, { method: "GET" });
}

export async function createCollection(input: Record<string, unknown>) {
  return browserApiRequest<CollectionResponse>(adminEndpoints.collections, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateCollection(id: string, input: Record<string, unknown>) {
  return browserApiRequest<CollectionResponse>(adminEndpoints.collection(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function addProductToCollection(productId: string, collectionId: string) {
  return browserApiRequest<void>(adminEndpoints.productCollections(productId), {
    method: "POST",
    body: JSON.stringify({ collectionId })
  });
}

export async function removeProductFromCollection(productId: string, collectionId: string) {
  return browserApiRequest<void>(adminEndpoints.productCollection(productId, collectionId), {
    method: "DELETE"
  });
}

export async function createCoupon(input: Record<string, unknown>) {
  return browserApiRequest<CouponResponse>(adminEndpoints.coupons, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateCoupon(id: string, input: Record<string, unknown>) {
  return browserApiRequest<CouponResponse>(adminEndpoints.coupon(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}
