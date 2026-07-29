import { Suspense } from "react";
import { AdminOrdersClient } from "@/components/orders/AdminOrdersClient";
import { listAdminOrders } from "@/modules/orders/api";
import { listShippingMethods } from "@/modules/shipping/api";

export default function OrdersPage() {
  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Đơn hàng</h1>
          <p>Theo dõi trạng thái xử lý đơn hàng và cập nhật giao vận ngay trong cùng một màn hình.</p>
        </div>
      </section>

      <Suspense fallback={<OrderTableSkeleton />}>
        <OrdersContent />
      </Suspense>
    </main>
  );
}

async function OrdersContent() {
  const [ordersPage, shippingMethods] = await Promise.all([listAdminOrders(), listShippingMethods()]);

  return <AdminOrdersClient initialOrders={ordersPage.items} initialMeta={ordersPage.meta} shippingMethods={shippingMethods} />;
}

function OrderTableSkeleton() {
  return (
    <div className="card panel">
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 18, gap: 10 }}>
        <div style={{ display: "flex", gap: 8 }}>
          {[90, 80, 100, 90].map((w, i) => (
            <div key={i} className="skeleton" style={{ width: w, height: 36, borderRadius: 999 }} />
          ))}
        </div>
        <div className="skeleton" style={{ width: 140, height: 36, borderRadius: 12 }} />
      </div>
      {[1, 2, 3, 4, 5, 6, 7].map((row) => (
        <div
          key={row}
          style={{ display: "grid", gridTemplateColumns: "1fr 2fr 1fr 1fr 1fr", gap: 12, alignItems: "center", padding: "13px 0", borderBottom: "1px solid var(--admin-line)" }}
        >
          <div className="skeleton" style={{ height: 13 }} />
          <div style={{ display: "grid", gap: 6 }}>
            <div className="skeleton" style={{ width: "80%", height: 13 }} />
            <div className="skeleton" style={{ width: "50%", height: 11 }} />
          </div>
          <div className="skeleton" style={{ width: 70, height: 22, borderRadius: 999 }} />
          <div className="skeleton" style={{ height: 13 }} />
          <div className="skeleton" style={{ height: 13 }} />
        </div>
      ))}
    </div>
  );
}
