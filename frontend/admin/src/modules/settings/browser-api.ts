import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { SiteSettingResponse } from "@/modules/settings/types";

export async function upsertSiteSetting(
  key: string,
  input: { settingValue?: string | null; valueType: string; description?: string | null; isPublic: boolean }
) {
  return browserApiRequest<SiteSettingResponse>(adminEndpoints.adminSetting(key), {
    method: "PUT",
    body: JSON.stringify(input)
  });
}
