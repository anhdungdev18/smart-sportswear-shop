import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { AdminReviewResponse } from "@/modules/reviews/types";

export async function listAdminReviews() {
  return apiRequest<AdminReviewResponse[]>(adminEndpoints.adminReviews, {
    next: { revalidate: 30 }
  });
}

export async function getAdminReview(id: string) {
  return apiRequest<AdminReviewResponse>(adminEndpoints.adminReviewDetail(id), {
    next: { revalidate: 0 }
  });
}
