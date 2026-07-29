import { apiRequestEnvelope } from "@/modules/api/client";
import { adminEndpoints } from "@/modules/api/endpoints";
import type { InventoryItemResponse, InventoryPage, InventoryTransactionResponse, PageMeta } from "@/modules/inventory/types";

const EMPTY_META: PageMeta = { page: 1, limit: 0, total: 0, totalPages: 0 };

function toPage<T>(data: T[], meta: Record<string, unknown> | undefined): InventoryPage<T> {
  return { items: data, meta: { ...EMPTY_META, ...(meta as Partial<PageMeta> | undefined) } };
}

export async function listInventoryItems(page = 1, limit = 25, keyword?: string) {
  const response = await apiRequestEnvelope<InventoryItemResponse[]>(adminEndpoints.inventory, {
    query: { page, limit, keyword }, cache: "no-store"
  });
  return toPage(response.data, response.meta);
}

export async function listInventoryTransactions(page = 1, limit = 20) {
  const response = await apiRequestEnvelope<InventoryTransactionResponse[]>(adminEndpoints.inventoryTransactions, {
    query: { page, limit }, cache: "no-store"
  });
  return toPage(response.data, response.meta);
}
