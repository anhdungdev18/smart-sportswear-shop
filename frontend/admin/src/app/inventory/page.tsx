import { AdminInventoryClient } from "@/components/inventory/AdminInventoryClient";
import { listInventoryItems, listInventoryTransactions } from "@/modules/inventory/api";

export default async function InventoryPage() {
  const [itemsResult, transactionsResult] = await Promise.allSettled([
    listInventoryItems(),
    listInventoryTransactions()
  ]);

  const items = itemsResult.status === "fulfilled" ? itemsResult.value : [];
  const transactions = transactionsResult.status === "fulfilled" ? transactionsResult.value : [];
  const loadFailed = itemsResult.status === "rejected";

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Tồn kho</h1>
          <p>Danh sách tồn kho hiện tại và nhật ký biến động kho từ hệ thống thật.</p>
        </div>
      </section>

      {loadFailed ? (
        <section className="card panel">
          <div className="empty-state">
            Không tải được dữ liệu tồn kho. Phiên đăng nhập có thể đã hết hạn — hãy tải lại trang hoặc đăng nhập lại.
          </div>
        </section>
      ) : (
        <AdminInventoryClient initialItems={items} initialTransactions={transactions} />
      )}
    </main>
  );
}
