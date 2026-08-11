import { AdminPromotionsClient } from "@/components/catalog/AdminPromotionsClient";
import { getPromotions } from "@/modules/promotions/api";

export const metadata = { title: "Khuyến mãi - Admin" };

export default async function PromotionsPage() {
  const promotions = await getPromotions().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Khuyến mãi</h1>
          <p>Tạo, sửa và theo dõi chương trình giảm giá theo sản phẩm cho Flash Sale.</p>
        </div>
      </section>

      <AdminPromotionsClient initialItems={promotions} />
    </main>
  );
}
