import { getOrderReport, getOverviewReport } from "@/modules/reports/api";

export default async function ReportsPage() {
  const emptyOverview = { grossRevenue: 0, realizedRevenue: 0, totalOrders: 0, pendingOrders: 0 };
  const emptyOrderReport = { byStatus: [] as { status: string; count: number }[] };
  const [overview, orderReport] = await Promise.all([
    getOverviewReport().catch(() => emptyOverview),
    getOrderReport().catch(() => emptyOrderReport)
  ]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Báo cáo</h1>
          <p>Tổng hợp doanh thu, đơn hàng và số lượng đơn chờ xử lý từ API báo cáo hiện tại.</p>
        </div>
      </section>

      <section className="kpi-grid">
        <article className="card kpi-card"><div className="kpi-label"><span>Doanh thu gộp</span></div><div className="kpi-value">{Math.round(overview.grossRevenue).toLocaleString("vi-VN")}₫</div></article>
        <article className="card kpi-card"><div className="kpi-label"><span>Doanh thu thực nhận</span></div><div className="kpi-value">{Math.round(overview.realizedRevenue).toLocaleString("vi-VN")}₫</div></article>
        <article className="card kpi-card"><div className="kpi-label"><span>Tổng đơn</span></div><div className="kpi-value">{overview.totalOrders}</div></article>
        <article className="card kpi-card"><div className="kpi-label"><span>Đơn chờ xử lý</span></div><div className="kpi-value">{overview.pendingOrders}</div></article>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>Phân bổ trạng thái đơn hàng</h2>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Trạng thái</th>
              <th>Số lượng</th>
            </tr>
          </thead>
          <tbody>
            {orderReport.byStatus.map((item) => (
              <tr key={item.status}>
                <td>{item.status}</td>
                <td>{item.count}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  );
}
