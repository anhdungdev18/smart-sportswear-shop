import { getAccessToken } from "@/lib/session";

// Next.js inlines NEXT_PUBLIC_* at build time for both server and client code.
const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8080";

export type ApiEnvelope<T> = {
  data: T;
  meta?: Record<string, unknown>;
  message?: string;
};

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public payload: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

type QueryValue = string | number | boolean | null | undefined;

function buildUrl(
  path: string,
  query?: Record<string, QueryValue | QueryValue[]>,
): string {
  const base = API_BASE.replace(/\/$/, "");
  const pathname = path.startsWith("/") ? path : `/${path}`;
  const url = new URL(`${base}${pathname}`);
  for (const [k, v] of Object.entries(query ?? {})) {
    if (v == null || v === "") continue;
    if (Array.isArray(v)) {
      v.forEach((item) => item != null && url.searchParams.append(k, String(item)));
    } else {
      url.searchParams.set(k, String(v));
    }
  }
  return url.toString();
}

export async function apiFetch<T>(
  path: string,
  options?: RequestInit & {
    query?: Record<string, QueryValue | QueryValue[]>;
    next?: { revalidate?: number | false; tags?: string[] };
  },
): Promise<{ data: T; meta?: Record<string, unknown> }> {
  const { query, headers: extraHeaders, next, ...rest } = options ?? {};
  const token = getAccessToken();

  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(extraHeaders as Record<string, string> | undefined),
  };
  if (token) headers.Authorization = `Bearer ${token}`;
  if (rest.body && !(rest.body instanceof FormData) && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  const isGet = !rest.method || rest.method.toUpperCase() === "GET";
  const nextOptions = next ?? (isGet && !token ? { revalidate: 60 } : undefined);

  const res = await fetch(buildUrl(path, query), {
    ...rest,
    credentials: "include",
    headers,
    ...(nextOptions ? { next: nextOptions } : {}),
  });
  const payload = await res.json().catch(() => null);
  if (!res.ok) throw new ApiError(res.status, res.statusText, payload);

  const envelope = payload as ApiEnvelope<T>;
  return { data: envelope.data ?? (payload as T), meta: envelope.meta };
}
