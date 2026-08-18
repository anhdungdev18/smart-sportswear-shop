import { ACCESS_TOKEN_COOKIE, ACCESS_TOKEN_STORAGE_KEY } from "@/modules/auth/session";
import { ApiEnvelope, ApiQuery, ApiRequestError, buildApiUrl, shouldUseMockApi } from "@/modules/api/common";

type BrowserApiRequestInit = RequestInit & {
  query?: ApiQuery;
};

export const ADMIN_LOCAL_MUTATION_KEY = "admin:last-local-mutation-at";
const ADMIN_API_TIMEOUT_MS = 45_000;

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
  const payload = await browserApiRequestEnvelope<T>(path, init);
  return payload.data;
}

export async function browserApiRequestEnvelope<T>(path: string, init?: BrowserApiRequestInit): Promise<ApiEnvelope<T>> {
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

  const method = (requestInit.method ?? "GET").toUpperCase();
  const isMutation = !["GET", "HEAD", "OPTIONS"].includes(method);
  if (isMutation && typeof window !== "undefined") {
    // The backend broadcasts successful mutations to every admin tab. Mark
    // this tab before sending so its realtime listener can ignore its own echo.
    sessionStorage.setItem(ADMIN_LOCAL_MUTATION_KEY, String(Date.now()));
  }

  let response: Response;
  try {
    response = await fetch(buildApiUrl(path, query), {
      ...requestInit,
      signal: requestInit.signal
        ? AbortSignal.any([requestInit.signal, AbortSignal.timeout(ADMIN_API_TIMEOUT_MS)])
        : AbortSignal.timeout(ADMIN_API_TIMEOUT_MS),
      credentials: requestInit.credentials ?? "include",
      headers: requestHeaders
    });
  } catch (error) {
    if (error instanceof DOMException && (error.name === "TimeoutError" || error.name === "AbortError")) {
      throw new ApiRequestError(408, "Máy chủ xử lý quá lâu. Vui lòng kiểm tra lại trạng thái đơn trước khi thử lại.", null);
    }
    throw error;
  }

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiRequestError(response.status, response.statusText, payload);
  }

  if (payload && typeof payload === "object" && "data" in payload) {
    return payload as ApiEnvelope<T>;
  }
  return { data: payload as T };
}
