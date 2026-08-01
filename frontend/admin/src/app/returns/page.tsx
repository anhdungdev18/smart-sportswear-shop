import { AdminReturnsClient } from "@/components/returns/AdminReturnsClient";
import { listAdminReturns } from "@/modules/returns/api";

export default async function ReturnsPage() {
  const page = await listAdminReturns();
  const items = page.items;

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Đổi trả</h1>
          <p>Xử lý yêu cầu đổi trả của khách, tạo hoàn tiền và cập nhật trạng thái refund.</p>
        </div>
      </section>

      <AdminReturnsClient initialReturns={items} initialMeta={page.meta} initialRefundsByReturn={{}} />
    </main>
  );
}
