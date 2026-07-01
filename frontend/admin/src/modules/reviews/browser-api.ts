import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminReviewResponse } from "@/modules/reviews/types";

export async function fetchAdminReviewDetail(id: string) {
  return browserApiRequest<AdminReviewResponse>(adminEndpoints.adminReviewDetail(id), {
    method: "GET"
  });
}

export async function updateAdminReviewStatus(id: string, status: string) {
  return browserApiRequest<AdminReviewResponse>(adminEndpoints.adminReviewStatus(id), {
    method: "PATCH",
    body: JSON.stringify({ status })
  });
}
