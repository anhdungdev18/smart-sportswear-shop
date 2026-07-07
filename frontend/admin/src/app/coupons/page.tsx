import { AdminCouponsClient } from "@/components/catalog/AdminCouponsClient";
import { listCoupons } from "@/modules/catalog-admin/api";

export default async function CouponsPage() {
  const coupons = await listCoupons().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Mã giảm giá</h1>
          <p>Tạo và quản lý coupon gắn với promotion, dùng đúng coupon API của backend.</p>
        </div>
      </section>

      <AdminCouponsClient initialItems={coupons} />
    </main>
  );
}
