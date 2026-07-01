import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { NotificationResponse, NotificationTemplateResponse } from "@/modules/notifications/types";

export async function resendNotification(id: string) {
  return browserApiRequest<NotificationResponse>(adminEndpoints.notificationResend(id), {
    method: "POST"
  });
}

export async function updateNotificationTemplate(id: string, input: { subject?: string; body?: string }) {
  return browserApiRequest<NotificationTemplateResponse>(adminEndpoints.notificationTemplate(id), {
    method: "PATCH",
    body: JSON.stringify(input)
  });
}
