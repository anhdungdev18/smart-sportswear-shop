import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { NotificationResponse, NotificationTemplateResponse } from "@/modules/notifications/types";

export async function listAdminNotifications() {
  return apiRequest<NotificationResponse[]>(adminEndpoints.notifications, {
    query: { limit: 20 },
    next: { revalidate: 30 }
  });
}

export async function listNotificationTemplates() {
  return apiRequest<NotificationTemplateResponse[]>(adminEndpoints.notificationTemplates, {
    next: { revalidate: 30 }
  });
}
