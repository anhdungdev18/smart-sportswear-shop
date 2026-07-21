import { AdminInventoryClient } from "@/components/inventory/AdminInventoryClient";
import type { InventoryItemResponse } from "@/modules/inventory/types";

const mockItems: InventoryItemResponse[] = [
  { variantId: "1", productId: "p1", productName: "Clamp Shorts", thumbnail: null, sku: "SKU-8905f0b2-aaa1-496d-943b-9812f97e68c6", size: "M", color: "Black", stockQuantity: 10, reservedQuantity: 0, availableQuantity: 10 },
  { variantId: "2", productId: "p2", productName: "Clamp Tee", thumbnail: null, sku: "SKU-26197e1-4d75-4d22-b00e-9e7181a14c72", size: "M", color: "Black", stockQuantity: 10, reservedQuantity: 0, availableQuantity: 10 },
  { variantId: "3", productId: "p3", productName: "Giay Da Bong Control Pro", thumbnail: null, sku: "SEED-BOOT-RED-43", size: "43", color: "Red", stockQuantity: 5, reservedQuantity: 0, availableQuantity: 5 },
  { variantId: "4", productId: "p4", productName: "Giay Da Bong Control Pro", thumbnail: null, sku: "SEED-BOOT-BLK-42", size: "42", color: "Black", stockQuantity: 5, reservedQuantity: 0, availableQuantity: 5 },
  { variantId: "5", productId: "p5", productName: "Tat Tap Training Grip", thumbnail: null, sku: "SEED-SOCK-BLK-42", size: "42-44", color: "Black", stockQuantity: 20, reservedQuantity: 0, availableQuantity: 20 }
];

export default function ComboboxTestPage() {
  return (
    <main className="workspace">
      <AdminInventoryClient initialItems={mockItems} initialTransactions={[]} />
    </main>
  );
}
