import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { RefundResponse, ReturnResponse } from "@/modules/returns/types";

export async function fetchAdminReturnDetail(id: string) {
  return browserApiRequest<ReturnResponse>(adminEndpoints.adminReturnDetail(id), {
    method: "GET"
  });
}

export async function updateReturnStatus(id: string, input: Record<string, unknown>) {
  return browserApiRequest<ReturnResponse>(adminEndpoints.adminReturnStatus(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}

export async function createRefund(id: string, input?: Record<string, unknown>) {
  return browserApiRequest<RefundResponse>(adminEndpoints.adminReturnRefund(id), {
    method: "POST",
    body: JSON.stringify(input ?? {})
  });
}

export async function updateRefundStatus(id: string, input: Record<string, unknown>) {
  return browserApiRequest<RefundResponse>(adminEndpoints.adminRefundStatus(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}
