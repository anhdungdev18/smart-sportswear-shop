import { AdminReturnsClient } from "@/components/returns/AdminReturnsClient";
import { listRefundsForReturn, listAdminReturns } from "@/modules/returns/api";

export default async function ReturnsPage() {
  const items = await listAdminReturns();
  const refunds = await Promise.all(items.map(async (item) => [item.id, await listRefundsForReturn(item.id)] as const));
  const refundsByReturn = Object.fromEntries(refunds);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Đổi trả</h1>
          <p>Xử lý yêu cầu đổi trả của khách, tạo hoàn tiền và cập nhật trạng thái refund.</p>
        </div>
      </section>

      <AdminReturnsClient initialReturns={items} initialRefundsByReturn={refundsByReturn} />
    </main>
  );
}
