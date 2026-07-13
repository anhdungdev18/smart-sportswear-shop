import { AdminInventoryClient } from "@/components/inventory/AdminInventoryClient";
import { listInventoryItems, listInventoryTransactions } from "@/modules/inventory/api";

export default async function InventoryPage() {
  const [items, transactions] = await Promise.all([listInventoryItems(), listInventoryTransactions()]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Tồn kho</h1>
          <p>Danh sách tồn kho hiện tại và nhật ký biến động kho từ hệ thống thật.</p>
        </div>
      </section>

      <AdminInventoryClient initialItems={items} initialTransactions={transactions} />
    </main>
  );
}
