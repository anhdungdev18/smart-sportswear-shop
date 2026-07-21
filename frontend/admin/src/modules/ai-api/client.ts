import { cookies } from "next/headers";
import { ApiEnvelope, ApiQuery, ApiRequestError, buildAiApiUrl, shouldUseMockAiApi } from "@/modules/ai-api/common";

export { ApiRequestError, buildAiApiUrl, shouldUseMockAiApi } from "@/modules/ai-api/common";
export type { ApiEnvelope, ApiQuery } from "@/modules/ai-api/common";

type ApiRequestInit = RequestInit & {
  query?: ApiQuery;
  next?: {
    revalidate?: number | false;
    tags?: string[];
  };
};

export async function aiApiRequest<T>(path: string, init?: ApiRequestInit) {
  if (shouldUseMockAiApi()) {
    throw new ApiRequestError(0, "NEXT_PUBLIC_AI_API_BASE_URL is not configured", null);
  }

  const { query, headers, ...requestInit } = init ?? {};
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("sss_access_token")?.value;
  const isFormData = typeof FormData !== "undefined" && requestInit.body instanceof FormData;
  const requestHeaders: Record<string, string> = {
    Accept: "application/json",
    ...(headers as Record<string, string> | undefined)
  };

  if (accessToken) {
    requestHeaders["Authorization"] = `Bearer ${accessToken}`;
  }

  if (!isFormData && requestInit.body && !requestHeaders["Content-Type"]) {
    requestHeaders["Content-Type"] = "application/json";
  }

  const response = await fetch(buildAiApiUrl(path, query), {
    ...requestInit,
    headers: requestHeaders
  });

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiRequestError(response.status, response.statusText, payload);
  }

  return ((payload as ApiEnvelope<T>)?.data ?? payload) as T;
}
