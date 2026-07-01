import { AdminPromotionsClient } from "@/components/catalog/AdminPromotionsClient";
import { listPromotions } from "@/modules/catalog-admin/api";
import { listAdminProducts } from "@/modules/product-management/api";

export default async function PromotionsPage() {
  const [promotions, products] = await Promise.all([listPromotions(), listAdminProducts()]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Khuyến mãi</h1>
          <p>Tạo chiến dịch giảm giá theo đơn hàng hoặc theo sản phẩm, bám đúng promotion API của backend.</p>
        </div>
      </section>

      <AdminPromotionsClient
        initialItems={promotions}
        products={products
          .filter((item): item is typeof item & { id: string } => Boolean(item.id))
          .map((item) => ({
            id: item.id,
            name: item.name
          }))}
      />
    </main>
  );
}
