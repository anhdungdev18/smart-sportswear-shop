import { apiFetch, API_BASE } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import { getAccessToken } from "@/lib/session";
import type { NotificationItem, UnreadCount } from "@/modules/notifications/types";

export async function listMyNotifications(query?: { page?: number; limit?: number }) {
  const result = await apiFetch<NotificationItem[]>(endpoints.notifications.mine, { query });
  return result.data;
}

export async function getUnreadCount() {
  const result = await apiFetch<UnreadCount>(endpoints.notifications.unreadCount);
  return result.data.unread;
}

export async function markNotificationRead(id: string) {
  await apiFetch(endpoints.notifications.read(id), { method: "POST" });
}

export async function markAllNotificationsRead() {
  await apiFetch(endpoints.notifications.readAll, { method: "POST" });
}

/**
 * Absolute SSE URL for the real-time stream. EventSource can't set an
 * Authorization header, so the access token rides the query string — the
 * backend accepts it there only for this one endpoint (see JwtAuthenticationFilter).
 * Returns null when the user isn't logged in.
 */
export function notificationStreamUrl(): string | null {
  const token = getAccessToken();
  if (!token) return null;
  return `${API_BASE}${endpoints.notifications.stream}?access_token=${encodeURIComponent(token)}`;
}
