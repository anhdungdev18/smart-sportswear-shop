import { aiApiRequest } from "@/modules/ai-api/client";
import type { ApiQuery } from "@/modules/api/common";
import type { PageResponse, ReplenishmentSuggestionResponse } from "./types";

export async function listSuggestions(query?: ApiQuery) {
  return aiApiRequest<PageResponse<ReplenishmentSuggestionResponse>>("/api/v1/admin/replenishment/suggestions", {
    query,
    next: { revalidate: 0 } // no cache
  });
}


