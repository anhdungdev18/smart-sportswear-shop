import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { BannerItemResponse, BannerResponse } from "@/modules/banners/types";

export async function fetchBannerDetail(id: string) {
  return browserApiRequest<BannerResponse>(adminEndpoints.bannerDetail(id), {
    method: "GET"
  });
}

export async function createBanner(input: Record<string, unknown>) {
  return browserApiRequest<BannerResponse>(adminEndpoints.banners, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateBanner(id: string, input: Record<string, unknown>) {
  return browserApiRequest<BannerResponse>(adminEndpoints.bannerDetail(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function addBannerItem(id: string, input: Record<string, unknown>) {
  return browserApiRequest<BannerItemResponse>(adminEndpoints.bannerItems(id), {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updateBannerItem(id: string, input: Record<string, unknown>) {
  return browserApiRequest<BannerItemResponse>(adminEndpoints.bannerItem(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function deleteBannerItem(id: string) {
  return browserApiRequest<void>(adminEndpoints.bannerItem(id), {
    method: "DELETE"
  });
}
