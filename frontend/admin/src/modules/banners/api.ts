import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { BannerResponse } from "@/modules/banners/types";

export async function listAdminBanners() {
  return apiRequest<BannerResponse[]>(adminEndpoints.banners, {
    next: { revalidate: 30 }
  });
}
