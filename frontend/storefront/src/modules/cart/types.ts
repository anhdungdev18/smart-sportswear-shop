export interface CartItem {
  id: string;
  variantId: string;
  productId: string;
  productName: string;
  sku: string;
  size?: string | null;
  color?: string | null;
  price: number;
  quantity: number;
  lineTotal: number;
  thumbnail?: string | null;
}

export interface CartResponse {
  id: string;
  items: CartItem[];
  subtotal: number;
}
