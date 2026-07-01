import { AdminProductsCatalogClient } from "@/components/products/AdminProductsCatalogClient";
import { listAdminBrands, listAdminCategories } from "@/modules/catalog-admin/api";
import { getAdminProductStats, listAdminProducts } from "@/modules/product-management/api";

export default async function ProductsPage() {
  const [adminProducts, productStats, categories, brands] = await Promise.all([
    listAdminProducts(),
    getAdminProductStats(),
    listAdminCategories(),
    listAdminBrands()
  ]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Sản phẩm</h1>
          <p>Quản lý catalog thật: tạo, sửa, thêm biến thể và quản lý ảnh trực tiếp qua API admin.</p>
        </div>
      </section>

      <section className="kpi-grid" aria-label="Thống kê sản phẩm">
        {productStats.map((item) => (
          <article className="card kpi-card product-stat" data-tone={item.tone} key={item.label}>
            <div className="kpi-label">
              <span>{item.label}</span>
            </div>
            <div className="kpi-value">{item.value}</div>
          </article>
        ))}
      </section>

      <AdminProductsCatalogClient initialProducts={adminProducts} categories={categories} brands={brands} />
    </main>
  );
}
