"use client";

import { useState } from "react";
import type { InventoryItemResponse, InventoryTransactionResponse } from "@/modules/inventory/types";
import type { ReplenishmentSuggestionResponse } from "@/modules/replenishment/types";
import { AdminInventoryClient } from "./AdminInventoryClient";
import { ReplenishmentSuggestionTable } from "./ReplenishmentSuggestionTable";

export interface InventoryImportDraft {
  variantId: string;
  quantity: number;
  recommendationId: string;
  sku: string;
}

export function InventoryWorkspace({ initialItems, initialTransactions, initialSuggestions }: {
  initialItems: InventoryItemResponse[];
  initialTransactions: InventoryTransactionResponse[];
  initialSuggestions: ReplenishmentSuggestionResponse[];
}) {
  const [importDraft, setImportDraft] = useState<InventoryImportDraft | null>(null);
  return <>
    <ReplenishmentSuggestionTable initialSuggestions={initialSuggestions} onFillImport={setImportDraft} />
    <AdminInventoryClient initialItems={initialItems} initialTransactions={initialTransactions} importDraft={importDraft} />
  </>;
}