import { AdminCouponsClient } from "@/components/catalog/AdminCouponsClient";
import { listCoupons, listPromotions } from "@/modules/catalog-admin/api";

export default async function CouponsPage() {
  const [coupons, promotions] = await Promise.all([listCoupons(), listPromotions()]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Mã giảm giá</h1>
          <p>Quản lý coupon gắn với từng promotion, theo đúng ràng buộc create/update hiện tại của backend.</p>
        </div>
      </section>

      <AdminCouponsClient initialItems={coupons} promotions={promotions} />
    </main>
  );
}
