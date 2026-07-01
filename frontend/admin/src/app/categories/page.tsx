import { AdminCategoriesClient } from "@/components/catalog/AdminCategoriesClient";
import { listAdminCategories } from "@/modules/catalog-admin/api";

export default async function CategoriesPage() {
  const categories = await listAdminCategories();

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Danh mục</h1>
          <p>Thêm và cập nhật danh mục sản phẩm bằng tiếng Việt có dấu, dùng trực tiếp API category admin.</p>
        </div>
      </section>

      <AdminCategoriesClient initialItems={categories} />
    </main>
  );
}
