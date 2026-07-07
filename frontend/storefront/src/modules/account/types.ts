export interface WishlistItem {
  id: string;
  productId: string;
  productName: string;
  thumbnail?: string | null;
  createdAt: string;
}

export interface WishlistResponse {
  id: string;
  items: WishlistItem[];
}

export interface AddressResponse {
  id: string;
  receiverName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  addressLine: string;
  isDefault: boolean;
  createdAt: string;
}

export interface OrderItem {
  id: string;
  productId: string;
  variantId: string;
  productName: string;
  sku: string;
  size?: string | null;
  color?: string | null;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface OrderResponse {
  id: string;
  orderCode: string;
  orderStatus: string;
  paymentStatus: string;
  paymentMethod: "COD" | "VNPAY";
  subtotalAmount: number;
  shippingFee: number;
  discountAmount: number;
  totalAmount: number;
  note?: string | null;
  items: OrderItem[];
  createdAt: string;
}

export interface PagedResult<T> {
  data: T;
  meta?: Record<string, unknown>;
}
