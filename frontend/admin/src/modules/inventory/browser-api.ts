import { browserApiRequest } from "@/modules/api/browser-client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { InventoryItemResponse } from "@/modules/inventory/types";

export async function importStock(input: { variantId: string; quantity: number; note?: string }) {
  return browserApiRequest<InventoryItemResponse>(adminEndpoints.inventoryImport, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function exportStock(input: { variantId: string; quantity: number; note?: string }) {
  return browserApiRequest<InventoryItemResponse>(adminEndpoints.inventoryExport, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export async function adjustStock(input: { variantId: string; type: string; quantity: number; note?: string }) {
  return browserApiRequest<InventoryItemResponse>(adminEndpoints.inventoryAdjust, {
    method: "POST",
    body: JSON.stringify(input)
  });
}
