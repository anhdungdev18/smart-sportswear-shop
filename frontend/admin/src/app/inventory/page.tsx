import { AdminInventoryClient } from "@/components/inventory/AdminInventoryClient";
import { ReplenishmentSuggestionTable } from "@/components/inventory/ReplenishmentSuggestionTable";
import { listInventoryItems, listInventoryTransactions } from "@/modules/inventory/api";
import { listSuggestions } from "@/modules/replenishment/api";

export default async function InventoryPage() {
  const [items, transactions, suggestionsPage] = await Promise.all([
    listInventoryItems(), 
    listInventoryTransactions(),
    listSuggestions()
  ]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Tồn kho</h1>
          <p>Danh sách tồn kho hiện tại và nhật ký biến động kho từ hệ thống thật.</p>
        </div>
      </section>

      <ReplenishmentSuggestionTable initialSuggestions={suggestionsPage.data} />
      <AdminInventoryClient initialItems={items} initialTransactions={transactions} />
    </main>
  );
}
