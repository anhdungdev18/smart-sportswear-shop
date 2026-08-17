export type OrderItemResponse = {
  id: string;
  productId: string;
  variantId: string;
  productName: string;
  sku: string;
  size: string | null;
  color: string | null;
  thumbnail: string | null;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type ShippingAddressResponse = {
  receiverName: string | null;
  phone: string | null;
  province: string | null;
  district: string | null;
  ward: string | null;
  addressLine: string | null;
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
  cancellationRequestedBy: "CUSTOMER" | "STAFF" | null;
  cancellationReason: string | null;
  cancellationRequestedAt: string | null;
  items: OrderItemResponse[];
  shippingAddress: ShippingAddressResponse | null;
  createdAt: string;
};

export type PageMeta = { page: number; limit: number; total: number; totalPages: number };
export type AdminOrderPage = { items: AdminOrderResponse[]; meta: PageMeta };
