import { AdminPromotionsClient } from "@/components/catalog/AdminPromotionsClient";
import { listPromotions } from "@/modules/catalog-admin/api";

export default async function PromotionsPage() {
  const promotions = await listPromotions().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Khuyến mãi</h1>
          <p>Tạo chiến dịch giảm giá theo đơn hàng hoặc theo sản phẩm, bám đúng promotion API của backend.</p>
        </div>
      </section>

      <AdminPromotionsClient initialItems={promotions} />
    </main>
  );
}
