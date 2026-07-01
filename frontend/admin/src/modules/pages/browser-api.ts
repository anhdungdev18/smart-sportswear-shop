import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { PageResponse } from "@/modules/pages/types";

export async function fetchPageDetail(id: string) {
  return browserApiRequest<PageResponse>(adminEndpoints.pageDetail(id), {
    method: "GET"
  });
}

export async function createPage(input: Record<string, unknown>) {
  return browserApiRequest<PageResponse>(adminEndpoints.pages, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function updatePage(id: string, input: Record<string, unknown>) {
  return browserApiRequest<PageResponse>(adminEndpoints.pageDetail(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}
