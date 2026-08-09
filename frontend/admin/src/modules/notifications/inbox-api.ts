import { adminEndpoints } from "@/modules/api/endpoints";
import { browserApiRequest } from "@/modules/api/browser-client";
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
