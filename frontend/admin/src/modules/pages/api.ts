import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { PageResponse } from "@/modules/pages/types";

export async function listAdminPages() {
  return apiRequest<PageResponse[]>(adminEndpoints.pages, {
    next: { revalidate: 30 }
  });
}
