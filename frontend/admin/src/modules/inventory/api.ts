import { apiRequest } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { InventoryItemResponse, InventoryTransactionResponse } from "@/modules/inventory/types";

export async function listInventoryItems() {
  return apiRequest<InventoryItemResponse[]>(adminEndpoints.inventory, {
    query: { limit: 200 },
    next: { revalidate: 30 }
  });
}

export async function listInventoryTransactions() {
  return apiRequest<InventoryTransactionResponse[]>(adminEndpoints.inventoryTransactions, {
    query: { limit: 50 },
    next: { revalidate: 30 }
  });
}
