import { ACCESS_TOKEN_COOKIE, ACCESS_TOKEN_STORAGE_KEY } from "@/modules/auth/session";
import { ApiEnvelope, ApiQuery, ApiRequestError, buildApiUrl, shouldUseMockApi } from "@/modules/api/common";

type BrowserApiRequestInit = RequestInit & {
  query?: ApiQuery;
};

function readCookie(name: string) {
  if (typeof document === "undefined") {
    return null;
  }

  return document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${name}=`))
    ?.split("=")[1] ?? null;
}

function getBrowserAccessToken() {
  if (typeof window === "undefined") {
    return null;
  }

  return window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY) ?? readCookie(ACCESS_TOKEN_COOKIE);
}

export async function browserApiRequest<T>(path: string, init?: BrowserApiRequestInit) {
  if (shouldUseMockApi()) {
    throw new ApiRequestError(0, "NEXT_PUBLIC_API_BASE_URL is not configured", null);
  }

  const { query, headers, ...requestInit } = init ?? {};
  const accessToken = getBrowserAccessToken();
  const isFormData = typeof FormData !== "undefined" && requestInit.body instanceof FormData;
  const requestHeaders: Record<string, string> = {
    Accept: "application/json",
    ...(headers as Record<string, string> | undefined)
  };

  if (accessToken) {
    requestHeaders.Authorization = `Bearer ${accessToken}`;
  }

  if (!isFormData && requestInit.body && !requestHeaders["Content-Type"]) {
    requestHeaders["Content-Type"] = "application/json";
  }

  const response = await fetch(buildApiUrl(path, query), {
    ...requestInit,
    credentials: requestInit.credentials ?? "include",
    headers: requestHeaders
  });

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiRequestError(response.status, response.statusText, payload);
  }

  return ((payload as ApiEnvelope<T>)?.data ?? payload) as T;
}
