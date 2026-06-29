import { DownloadSimple, FunnelSimple, Plus, Sparkle } from "@phosphor-icons/react/dist/ssr";
import Image from "next/image";
import { getAdminProductStats, listAdminProducts } from "@/modules/product-management/api";

const statusLabel = {
  active: "Đang bán",
  draft: "Bản nháp",
  low: "Sắp hết",
  out: "Hết hàng"
};

export default async function ProductsPage() {
  const [adminProducts, productStats] = await Promise.all([listAdminProducts(), getAdminProductStats()]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Sản phẩm</h1>
          <p>Quản lý catalog giày đá bóng, futsal và phụ kiện theo chuẩn hiển thị của storefront.</p>
        </div>
        <div className="page-actions">
          <button className="admin-btn secondary">
            <DownloadSimple size={18} weight="duotone" />
            Xuất file
          </button>
          <button className="admin-btn">
            <Plus size={18} weight="duotone" />
            Thêm sản phẩm
          </button>
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

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Catalog sản phẩm</h2>
            <p className="panel-copy">Quản lý SKU, giá bán, danh mục, tồn kho và trạng thái hiển thị.</p>
          </div>
          <button className="admin-btn secondary">
            <FunnelSimple size={18} weight="duotone" />
            Bộ lọc
          </button>
        </div>

        <div className="table-toolbar">
          <div className="filters">
            <span className="filter-chip active">Tất cả</span>
            <span className="filter-chip">Đang bán</span>
            <span className="filter-chip">Sắp hết hàng</span>
            <span className="filter-chip">Bản nháp</span>
          </div>
          <select className="select" aria-label="Sắp xếp sản phẩm">
            <option>Sắp xếp theo mới nhất</option>
            <option>Sắp xếp theo bán chạy</option>
            <option>Sắp xếp theo tồn kho thấp</option>
          </select>
        </div>

        <table className="data-table product-table">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>SKU</th>
              <th>Danh mục</th>
              <th>Thương hiệu</th>
              <th>Giá</th>
              <th>Tồn kho</th>
              <th>Đã bán</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {adminProducts.map((product) => (
              <tr key={product.sku}>
                <td>
                  <div className="product-cell">
                    <Image src={product.image} alt={product.name} width={52} height={52} />
                    <div>
                      <strong>{product.name}</strong>
                      <span>
                        <Sparkle size={14} weight="fill" />
                        SEO metadata sẵn sàng
                      </span>
                    </div>
                  </div>
                </td>
                <td>{product.sku}</td>
                <td>{product.category}</td>
                <td>{product.brand}</td>
                <td>{product.price}</td>
                <td>{product.stock}</td>
                <td>{product.sold}</td>
                <td>
                  <span className={`status ${product.status}`}>{statusLabel[product.status]}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  );
}
