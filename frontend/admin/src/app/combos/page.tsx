import { AdminCombosClient } from "@/components/catalog/AdminCombosClient";

export const metadata = { title: "Combo - Admin" };

export default function CombosPage() {
  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Combo</h1>
          <p>Tạo bộ sản phẩm cố định và mức giảm tiền khi khách mua đủ bộ.</p>
        </div>
      </section>

      <AdminCombosClient />
    </main>
  );
}
