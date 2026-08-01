import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import type { VisualSearchResult } from "@/modules/product/types";

export async function searchProductsByImage(image: Blob, limit = 20): Promise<VisualSearchResult[]> {
  const body = new FormData();
  body.append("image", image, "visual-search.jpg");
  const { data } = await apiFetch<VisualSearchResult[]>(endpoints.visualSearch, {
    method: "POST",
    query: { limit },
    body,
  });
  return data;
}
