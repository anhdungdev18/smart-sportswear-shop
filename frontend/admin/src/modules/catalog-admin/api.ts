import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type {
  BrandResponse,
  CategoryResponse,
  CollectionResponse,
  CouponResponse,
  ProductDetailResponse,
  PromotionResponse
} from "@/modules/catalog-admin/types";

export async function listAdminCategories() {
  return apiRequest<CategoryResponse[]>(adminEndpoints.categories, { next: { revalidate: 30 } });
}

export async function listAdminBrands() {
  return apiRequest<BrandResponse[]>(adminEndpoints.brands, { next: { revalidate: 30 } });
}

export async function listCoupons() {
  return apiRequest<CouponResponse[]>(adminEndpoints.coupons, { next: { revalidate: 30 } });
}

export async function listPromotions() {
  return apiRequest<PromotionResponse[]>(adminEndpoints.promotions, { next: { revalidate: 30 } });
}

export async function getAdminProductDetail(id: string) {
  return apiRequest<ProductDetailResponse>(adminEndpoints.productDetail(id), { next: { revalidate: 30 } });
}

export async function listCollections() {
  return apiRequest<CollectionResponse[]>(adminEndpoints.collections, { next: { revalidate: 30 } });
}
