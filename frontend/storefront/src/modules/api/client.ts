export type ApiEnvelope<T> = {
  data: T;
  meta?: Record<string, unknown>;
  message?: string;
};

type QueryValue = string | number | boolean | null | undefined | readonly (string | number | boolean)[];

export type ApiQuery = Record<string, QueryValue>;

type ApiRequestInit = RequestInit & {
  query?: ApiQuery;
  next?: {
    revalidate?: number | false;
    tags?: string[];
  };
};

export class ApiRequestError extends Error {
  status: number;
  payload: unknown;

  constructor(status: number, message: string, payload: unknown) {
    super(message);
    this.name = "ApiRequestError";
    this.status = status;
    this.payload = payload;
  }
}

export const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? "";

export function shouldUseMockApi() {
  return !apiBaseUrl;
}

export function buildApiUrl(path: string, query?: ApiQuery) {
  const pathname = path.startsWith("/") ? path : `/${path}`;
  const url = new URL(`${apiBaseUrl}${pathname}`, apiBaseUrl || "http://localhost");

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;

    if (Array.isArray(value)) {
      value.forEach((item) => url.searchParams.append(key, String(item)));
      return;
    }

    url.searchParams.set(key, String(value));
  });

  return apiBaseUrl ? url.toString() : `${pathname}${url.search}`;
}

export async function apiRequest<T>(path: string, init?: ApiRequestInit) {
  if (shouldUseMockApi()) {
    throw new ApiRequestError(0, "NEXT_PUBLIC_API_BASE_URL is not configured", null);
  }

  const { query, headers, ...requestInit } = init ?? {};
  const response = await fetch(buildApiUrl(path, query), {
    ...requestInit,
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...headers
    }
  });

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiRequestError(response.status, response.statusText, payload);
  }

  return ((payload as ApiEnvelope<T>)?.data ?? payload) as T;
}
