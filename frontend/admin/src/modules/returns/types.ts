export type ReturnItemResponse = {
  id: string;
  orderItemId: string;
  productName: string;
  sku: string;
  quantity: number;
  reason: string | null;
  conditionStatus: string | null;
  resolution: string | null;
  refundAmount: number | null;
};

export type ReturnResponse = {
  id: string;
  orderId: string;
  orderCode: string;
  userId: string;
  returnCode: string;
  status: string;
  reason: string;
  description: string | null;
  requestedAt: string | null;
  approvedAt: string | null;
  receivedAt: string | null;
  resolvedAt: string | null;
  items: ReturnItemResponse[];
  createdAt: string;
  updatedAt: string;
};

export type RefundResponse = {
  id: string;
  returnId: string;
  orderId: string;
  paymentId: string | null;
  refundCode: string;
  amount: number;
  provider: string;
  status: string;
  reason: string | null;
  refundedAt: string | null;
  createdAt: string;
  updatedAt: string;
};
