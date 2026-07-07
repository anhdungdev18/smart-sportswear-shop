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

export interface AppliedCouponSummary {
  code: string;
  discountAmount: number;
}

export interface CheckoutPreviewResponse {
  items: CheckoutItemPreview[];
  subtotal: number;
  discountAmount: number;
  shippingFee: number;
  totalAmount: number;
  appliedCoupon?: AppliedCouponSummary | null;
  couponError?: string | null;
  canCheckout: boolean;
}

export interface CouponValidationResponse {
  valid: boolean;
  couponCode: string;
  subtotal: number;
  discountAmount: number;
  message: string;
}
