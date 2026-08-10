export type NotificationResponse = {
  id: string;
  userId: string;
  orderId: string | null;
  type: string;
  channel: string;
  recipient: string;
  subject: string;
  body: string;
  status: string;
  errorMessage: string | null;
  createdAt: string;
  sentAt: string | null;
  readAt: string | null;
  resendOfId: string | null;
  resendCount: number;
  lastResendAt: string | null;
};

export type NotificationTemplateResponse = {
  id: string;
  type: string;
  channel: string;
  subject: string;
  body: string;
  allowedPlaceholders: string[];
  createdAt: string;
  updatedAt: string;
};
