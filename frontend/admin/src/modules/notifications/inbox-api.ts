import { adminEndpoints } from "@/modules/api/endpoints";
import { browserApiRequest } from "@/modules/api/browser-client";
import { buildApiUrl } from "@/modules/api/common";
import { getBrowserAccessToken } from "@/modules/auth/session";
import type { NotificationResponse } from "@/modules/notifications/types";

export async function listMyNotifications() {
  return browserApiRequest<NotificationResponse[]>(adminEndpoints.myNotifications, {
    query: { limit: 20 },
  });
}

export async function getMyUnreadCount() {
  return browserApiRequest<{ unread: number }>(adminEndpoints.myNotificationUnreadCount);
}

export async function markMyNotificationRead(id: string) {
  return browserApiRequest<void>(adminEndpoints.myNotificationRead(id), { method: "POST" });
}

export async function markAllMyNotificationsRead() {
  return browserApiRequest<number>(adminEndpoints.myNotificationsReadAll, { method: "POST" });
}

export const ADMIN_ORDER_CHANGED_EVENT = "admin-order-changed";

export function myNotificationStreamUrl(): string | null {
  const token = getBrowserAccessToken();
  if (!token) return null;
  return buildApiUrl(adminEndpoints.myNotificationsStream, { access_token: token });
}
