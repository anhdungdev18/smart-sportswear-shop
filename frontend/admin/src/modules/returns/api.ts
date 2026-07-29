import { apiRequest, apiRequestEnvelope } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminReturnPage, PageMeta, RefundResponse, ReturnResponse } from "@/modules/returns/types";

export async function listAdminReturns(page = 1, limit = 10): Promise<AdminReturnPage> {
  const response = await apiRequestEnvelope<ReturnResponse[]>(adminEndpoints.adminReturns, {
    query: { page, limit }, cache: "no-store"
  });
  return { items: response.data, meta: { page, limit, total: 0, totalPages: 0, ...(response.meta as Partial<PageMeta> | undefined) } };
}

export async function getAdminReturn(id: string) {
  return apiRequest<ReturnResponse>(adminEndpoints.adminReturnDetail(id), {
    next: { revalidate: 0 }
  });
}

export async function listRefundsForReturn(id: string) {
  return apiRequest<RefundResponse[]>(adminEndpoints.adminReturnRefunds(id), {
    next: { revalidate: 0 }
  });
}
