export type InventoryItemResponse = {
  variantId: string;
  productId: string;
  productName: string;
  thumbnail: string | null;
  sku: string;
  size: string | null;
  color: string | null;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
};

export type PageMeta = {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
};

export type InventoryPage<T> = {
  items: T[];
  meta: PageMeta;
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
