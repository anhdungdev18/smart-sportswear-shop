export type InventoryItemResponse = {
  variantId: string;
  productId: string;
  productName: string;
  sku: string;
  size: string | null;
  color: string | null;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
};

export type InventoryTransactionResponse = {
  id: string;
  variantId: string;
  sku: string;
  orderId: string | null;
  type: string;
  quantity: number;
  beforeStockQuantity: number;
  afterStockQuantity: number;
  beforeReservedQuantity: number;
  afterReservedQuantity: number;
  note: string | null;
  createdById: string | null;
  createdByName: string | null;
  createdAt: string;
};
