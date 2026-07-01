import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type {
  BrandResponse,
  CategoryResponse,
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
