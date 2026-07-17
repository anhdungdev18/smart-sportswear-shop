export interface CheckoutItemPreview {
  variantId: string;
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  valid: boolean;
  errorMessage?: string | null;
}

export interface CheckoutPreviewResponse {
  items: CheckoutItemPreview[];
  subtotal: number;
  // Combo (bundle) discount that applies to the current cart, if any.
  discountAmount: number;
  shippingFee: number;
  totalAmount: number;
  canCheckout: boolean;
}
