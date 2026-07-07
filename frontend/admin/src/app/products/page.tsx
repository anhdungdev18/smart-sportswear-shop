import { Suspense } from "react";
import { AdminProductsCatalogClient } from "@/components/products/AdminProductsCatalogClient";
import { listAdminBrands, listAdminCategories } from "@/modules/catalog-admin/api";
import { buildAdminProductStats, listAdminProducts } from "@/modules/product-management/api";

export default function ProductsPage() {
  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Sản phẩm</h1>
          <p>Quản lý catalog thật: tạo, sửa, thêm biến thể và quản lý ảnh trực tiếp qua API admin.</p>
        </div>
      </section>

      <Suspense fallback={<ProductStatsSkeleton />}>
        <ProductCatalogStats />
      </Suspense>

      <Suspense fallback={<ProductTableSkeleton />}>
        <ProductCatalog />
      </Suspense>
    </main>
  );
}

async function ProductCatalogStats() {
  const adminProducts = await listAdminProducts().catch(() => []);
  const productStats = buildAdminProductStats(adminProducts);

  return (
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
  );
}

async function ProductCatalog() {
  const [adminProducts, categories, brands] = await Promise.all([
    listAdminProducts().catch(() => []),
    listAdminCategories().catch(() => []),
    listAdminBrands().catch(() => []),
  ]);

  return <AdminProductsCatalogClient initialProducts={adminProducts} categories={categories} brands={brands} />;
}

function ProductStatsSkeleton() {
  return (
    <div className="kpi-grid">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="card kpi-card">
          <div className="skeleton" style={{ width: 90, height: 12 }} />
          <div className="skeleton" style={{ width: 100, height: 30 }} />
        </div>
      ))}
    </div>
  );
}

function ProductTableSkeleton() {
  return (
    <div className="card panel">
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 18, gap: 10 }}>
        <div style={{ display: "flex", gap: 8 }}>
          {[100, 80, 90].map((w, i) => (
            <div key={i} className="skeleton" style={{ width: w, height: 36, borderRadius: 999 }} />
          ))}
        </div>
        <div className="skeleton" style={{ width: 160, height: 36, borderRadius: 12 }} />
      </div>
      {[1, 2, 3, 4, 5, 6].map((row) => (
        <div
          key={row}
          style={{ display: "grid", gridTemplateColumns: "2fr 1fr 1fr 1fr 1fr", gap: 12, alignItems: "center", padding: "14px 0", borderBottom: "1px solid var(--admin-line)" }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <div className="skeleton" style={{ width: 52, height: 52, flexShrink: 0, borderRadius: 12 }} />
            <div style={{ display: "grid", gap: 6 }}>
              <div className="skeleton" style={{ width: 140, height: 13 }} />
              <div className="skeleton" style={{ width: 80, height: 11 }} />
            </div>
          </div>
          <div className="skeleton" style={{ height: 13 }} />
          <div className="skeleton" style={{ width: 55, height: 22, borderRadius: 999 }} />
          <div className="skeleton" style={{ height: 13 }} />
          <div className="skeleton" style={{ height: 13 }} />
        </div>
      ))}
    </div>
  );
}
