import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { SiteSettingResponse } from "@/modules/settings/types";

export async function listAdminSettings() {
  return apiRequest<SiteSettingResponse[]>(adminEndpoints.adminSettings, {
    next: { revalidate: 30 }
  });
}
