export type NotificationItem = {
  id: string;
  userId: string | null;
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

export type UnreadCount = {
  unread: number;
};
