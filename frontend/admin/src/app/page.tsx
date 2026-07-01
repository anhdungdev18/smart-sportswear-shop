import { Brain, CurrencyCircleDollar, Package, Receipt, Users } from "@phosphor-icons/react/dist/ssr";
import { RevenueChart, TopProductChart } from "@/components/ui/AdminCharts";
import { getDashboardData } from "@/modules/analytics/api";

const kpis = [
  { label: "Doanh thu hôm nay", value: "128,4 triệu", trend: "+12,8%", icon: CurrencyCircleDollar },
  { label: "Đơn hàng mới", value: "246", trend: "+31", icon: Receipt },
  { label: "Cảnh báo kho", value: "18 SKU", trend: "Cần xử lý", icon: Package, warn: true },
  { label: "Khách hàng mới", value: "1.284", trend: "+8,1%", icon: Users }
];

export default async function AdminDashboard() {
  const dashboard = await getDashboardData();

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Tổng quan vận hành</h1>
          <p>Dashboard quản trị đơn hàng, tồn kho, sản phẩm và cảnh báo bán hàng cho Thanh Hùng Futsal.</p>
        </div>
        <button className="admin-btn">
          <Brain size={18} weight="duotone" />
          Phân tích bán hàng
        </button>
      </section>

      <section className="kpi-grid" aria-label="KPI tổng quan">
        {kpis.map((kpi) => {
          const Icon = kpi.icon;
          return (
            <article className="card kpi-card" key={kpi.label}>
              <div className="kpi-label">
                <span>{kpi.label}</span>
                <Icon size={22} weight="duotone" />
              </div>
              <div className="kpi-value">{kpi.value}</div>
              <span className={kpi.warn ? "trend warn" : "trend"}>{kpi.trend}</span>
            </article>
          );
        })}
      </section>

      <section className="dashboard-grid">
        <article className="card panel">
          <div className="panel-header">
            <h2>Doanh thu theo thời gian</h2>
            <select className="select" aria-label="Khoảng thời gian">
              <option>6 tháng gần nhất</option>
              <option>30 ngày</option>
              <option>12 tháng</option>
            </select>
          </div>
          <RevenueChart data={dashboard.revenue} />
        </article>

        <article className="card panel">
          <div className="panel-header">
            <h2>Top sản phẩm</h2>
            <span className="filter-chip active">Theo doanh số</span>
          </div>
          <TopProductChart data={dashboard.topProducts} />
          <div className="insight">
            <strong>Gợi ý vận hành</strong>
            <p>Nike Gato IC còn ít size phổ biến. Nên ưu tiên nhập thêm size 41-43 và đẩy combo vớ thi đấu trong tuần này.</p>
          </div>
        </article>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>Cảnh báo tồn kho thấp</h2>
          <button className="admin-btn">Export CSV</button>
        </div>
        <div className="table-toolbar">
          <div className="filters">
            <span className="filter-chip active">Tất cả</span>
            <span className="filter-chip">Critical</span>
            <span className="filter-chip">Low</span>
            <span className="filter-chip">Dự báo bán</span>
          </div>
          <select className="select" aria-label="Sắp xếp">
            <option>Sắp xếp theo mức độ</option>
            <option>Sắp xếp theo SKU</option>
          </select>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>Sản phẩm</th>
              <th>Tồn kho</th>
              <th>Dự báo cần</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {dashboard.stockAlerts.map((item) => (
              <tr key={item.sku}>
                <td>{item.sku}</td>
                <td>{item.product}</td>
                <td>{item.stock}</td>
                <td>{item.forecast}</td>
                <td>
                  <span className={`status ${item.status}`}>{item.status === "critical" ? "Critical" : "Low"}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="dashboard-grid">
        <div className="loading-state">
          <strong>Loading state</strong>
          <div className="skeleton" style={{ marginTop: 14, width: "72%" }} />
          <div className="skeleton" style={{ marginTop: 10, width: "46%" }} />
        </div>
        <div className="empty-state">
          <strong>Empty state</strong>
          <p>Chưa có yêu cầu đổi size hoặc hoàn tiền mới trong khoảng thời gian đã chọn.</p>
        </div>
      </section>
    </main>
  );
}
