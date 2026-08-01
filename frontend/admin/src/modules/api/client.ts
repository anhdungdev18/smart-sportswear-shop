import { cookies } from "next/headers";
import { ApiEnvelope, ApiQuery, ApiRequestError, buildApiUrl, shouldUseMockApi } from "@/modules/api/common";

export { ApiRequestError, buildApiUrl, shouldUseMockApi } from "@/modules/api/common";
export type { ApiEnvelope, ApiQuery } from "@/modules/api/common";

type ApiRequestInit = RequestInit & {
  query?: ApiQuery;
  next?: {
    revalidate?: number | false;
    tags?: string[];
  };
};

export async function apiRequest<T>(path: string, init?: ApiRequestInit) {
  const payload = await apiRequestEnvelope<T>(path, init);
  return payload.data;
}

export async function apiRequestEnvelope<T>(path: string, init?: ApiRequestInit): Promise<ApiEnvelope<T>> {
  if (shouldUseMockApi()) {
    throw new ApiRequestError(0, "NEXT_PUBLIC_API_BASE_URL is not configured", null);
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

  const response = await fetch(buildApiUrl(path, query), {
    ...requestInit,
    headers: requestHeaders
  });

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiRequestError(response.status, response.statusText, payload);
  }

  if (payload && typeof payload === "object" && "data" in payload) {
    return payload as ApiEnvelope<T>;
  }
  return { data: payload as T };
}
