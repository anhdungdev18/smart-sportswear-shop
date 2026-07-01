import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { RefundResponse, ReturnResponse } from "@/modules/returns/types";

export async function listAdminReturns() {
  return apiRequest<ReturnResponse[]>(adminEndpoints.adminReturns, {
    next: { revalidate: 30 }
  });
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
