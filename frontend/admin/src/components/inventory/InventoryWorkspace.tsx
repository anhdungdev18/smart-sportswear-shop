"use client";

import { useState } from "react";
import type { InventoryItemResponse, InventoryPage, InventoryTransactionResponse } from "@/modules/inventory/types";
import type { ReplenishmentSuggestionResponse } from "@/modules/replenishment/types";
import { AdminInventoryClient } from "./AdminInventoryClient";
import { ReplenishmentSuggestionTable } from "./ReplenishmentSuggestionTable";

export interface InventoryImportDraft {
  variantId: string;
  quantity: number;
  recommendationId: string;
  sku: string;
}

export function InventoryWorkspace({ initialItemsPage, initialTransactionsPage, initialSuggestions }: {
  initialItemsPage: InventoryPage<InventoryItemResponse>;
  initialTransactionsPage: InventoryPage<InventoryTransactionResponse>;
  initialSuggestions: ReplenishmentSuggestionResponse[];
}) {
  const [importDraft, setImportDraft] = useState<InventoryImportDraft | null>(null);
  return <>
    <ReplenishmentSuggestionTable initialSuggestions={initialSuggestions} onFillImport={setImportDraft} />
    <AdminInventoryClient initialItemsPage={initialItemsPage} initialTransactionsPage={initialTransactionsPage} importDraft={importDraft} />
  </>;
}
