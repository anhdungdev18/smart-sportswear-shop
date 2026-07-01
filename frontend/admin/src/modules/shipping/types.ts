export type ShippingMethodResponse = {
  id: string;
  name: string;
  code: string;
  description: string | null;
  provider: string | null;
  baseFee: number;
  estimatedDaysMin: number | null;
  estimatedDaysMax: number | null;
};

export type ShipmentResponse = {
  id: string;
  orderId: string;
  shippingMethodId: string | null;
  shippingMethodName: string | null;
  shipmentCode: string;
  carrierName: string | null;
  trackingNumber: string | null;
  status: string;
  shippingFee: number;
  receiverName: string;
  receiverPhone: string;
  province: string;
  district: string;
  ward: string;
  addressLine: string;
  shippedAt: string | null;
  deliveredAt: string | null;
  estimatedDeliveryDateFrom: string | null;
  estimatedDeliveryDateTo: string | null;
  note: string | null;
  createdAt: string;
  updatedAt: string;
};
