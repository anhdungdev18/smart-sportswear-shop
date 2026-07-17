import { apiRequest } from "@/modules/api/client";
import type { PageResponse } from "@/modules/api/types";
import type { ReplenishmentSuggestionResponse, ReplenishmentSuggestionDetailResponse } from "./types";

export async function listSuggestions(query?: Record<string, any>) {
  return apiRequest<PageResponse<ReplenishmentSuggestionResponse>>("/api/v1/admin/replenishment/suggestions", {
    query,
    next: { revalidate: 0 } // no cache
  });
}


