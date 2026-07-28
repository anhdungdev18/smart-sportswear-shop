import { getBrowserAccessToken } from "@/modules/auth/session";
import { ApiRequestError } from "@/modules/api/common";
import type { ApprovalAction, ApprovalResponse, ChatResponse, CopilotConfig, CopilotRun } from "@/modules/admin-copilot/types";

type QueryValue = string | number | boolean | null | undefined;

const publicCopilotBaseUrl = process.env.NEXT_PUBLIC_ADMIN_COPILOT_API_BASE_URL?.replace(/\/$/, "") ?? "";

function buildCopilotUrl(path: string, query?: Record<string, QueryValue>) {
  const pathname = path.startsWith("/") ? path : `/${path}`;
  const url = new URL(`${publicCopilotBaseUrl}${pathname}`, publicCopilotBaseUrl || "http://localhost");

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    url.searchParams.set(key, String(value));
  });

  return publicCopilotBaseUrl ? url.toString() : `${pathname}${url.search}`;
}

async function copilotRequest<T>(path: string, init?: RequestInit & { query?: Record<string, QueryValue> }) {
  if (!publicCopilotBaseUrl) {
    throw new ApiRequestError(0, "NEXT_PUBLIC_ADMIN_COPILOT_API_BASE_URL is not configured", null);
  }

  const { query, headers, ...requestInit } = init ?? {};
  const accessToken = getBrowserAccessToken();
  const requestHeaders: Record<string, string> = {
    Accept: "application/json",
    ...(headers as Record<string, string> | undefined),
  };

  if (accessToken) {
    requestHeaders.Authorization = `Bearer ${accessToken}`;
  }

  if (requestInit.body && !requestHeaders["Content-Type"]) {
    requestHeaders["Content-Type"] = "application/json";
  }

  const response = await fetch(buildCopilotUrl(path, query), {
    ...requestInit,
    credentials: requestInit.credentials ?? "include",
    headers: requestHeaders,
  });
  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiRequestError(response.status, response.statusText, payload);
  }

  return payload as T;
}

export function sendCopilotMessage(sessionId: string, message: string) {
  return copilotRequest<ChatResponse>("/chat", {
    method: "POST",
    body: JSON.stringify({ sessionId, message }),
  });
}

export async function listCopilotRuns(limit = 50) {
  const payload = await copilotRequest<{ items: CopilotRun[] }>("/runs", { query: { limit } });
  return payload.items;
}

export function getCopilotConfig() {
  return copilotRequest<CopilotConfig>("/config");
}

export function sendCopilotFeedback(runId: string, rating: "CORRECT" | "INCORRECT", note?: string) {
  return copilotRequest<{ saved: boolean }>("/config/feedback", {
    method: "POST",
    body: JSON.stringify({ runId, rating, note }),
  });
}

export async function listApprovals(limit = 50, status?: string) {
  const payload = await copilotRequest<{ items: ApprovalResponse[] }>("/approvals", { query: { limit, status } });
  return payload.items;
}

export function createApproval(payload: {
  action: ApprovalAction;
  resourceId: string;
  payload: Record<string, unknown>;
  idempotencyKey: string;
  reason: string;
  riskLevel?: string;
}) {
  return copilotRequest<ApprovalResponse>("/approvals", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function approveApproval(id: string, note?: string) {
  return copilotRequest<ApprovalResponse>(`/approvals/${id}/approve`, {
    method: "POST",
    body: JSON.stringify({ note }),
  });
}

export function rejectApproval(id: string, note?: string) {
  return copilotRequest<ApprovalResponse>(`/approvals/${id}/reject`, {
    method: "POST",
    body: JSON.stringify({ note }),
  });
}

export function executeApproval(id: string) {
  return copilotRequest<ApprovalResponse>(`/approvals/${id}/execute`, { method: "POST" });
}
