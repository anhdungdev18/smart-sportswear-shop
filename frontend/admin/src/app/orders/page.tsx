import { AdminOrdersClient } from "@/components/orders/AdminOrdersClient";
import { listAdminOrders } from "@/modules/orders/api";
import { listShippingMethods } from "@/modules/shipping/api";

export default async function OrdersPage() {
  const [orders, shippingMethods] = await Promise.all([listAdminOrders(), listShippingMethods()]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Đơn hàng</h1>
          <p>Theo dõi trạng thái xử lý đơn hàng và cập nhật giao vận ngay trong cùng một màn hình.</p>
        </div>
      </section>

      <AdminOrdersClient initialOrders={orders} shippingMethods={shippingMethods} />
    </main>
  );
}
