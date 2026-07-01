export type OrderItemResponse = {
  id: string;
  productId: string;
  variantId: string;
  productName: string;
  sku: string;
  size: string | null;
  color: string | null;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type AdminOrderResponse = {
  id: string;
  orderCode: string;
  customerId: string;
  customerName: string;
  customerPhone: string | null;
  orderStatus: string;
  paymentStatus: string;
  paymentMethod: string;
  subtotalAmount: number;
  shippingFee: number;
  discountAmount: number;
  totalAmount: number;
  note: string | null;
  internalNote: string | null;
  items: OrderItemResponse[];
  createdAt: string;
};
