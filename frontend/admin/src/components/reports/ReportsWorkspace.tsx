"use client";

import { FormEvent, useState } from "react";
import { RevenueChart } from "@/components/ui/AdminCharts";
import { fetchCustomerReport, fetchProductReport, fetchReports } from "@/modules/reports/browser-api";
import type {
  CustomerReportResponse,
  OverviewReportResponse,
  ProductReportResponse,
  ReportFilters,
  RevenueGranularity,
  RevenueReportResponse
} from "@/modules/reports/api";

type Props = {
  overview: OverviewReportResponse;
  initialRevenue: RevenueReportResponse;
  initialCustomers: CustomerReportResponse;
  initialProducts: ProductReportResponse;
};

const GRANULARITIES: Array<{ value: RevenueGranularity; label: string }> = [
  { value: "DAY", label: "Theo ngày" },
  { value: "MONTH", label: "Theo tháng" },
  { value: "YEAR", label: "Theo năm" }
];

const EMPTY_META = { page: 1, limit: 10, total: 0, totalPages: 0 };

function money(value: number) {
  return `${Math.round(value).toLocaleString("vi-VN")} ₫`;
}

export function ReportsWorkspace({ overview, initialRevenue, initialCustomers, initialProducts }: Props) {
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [granularity, setGranularity] = useState<RevenueGranularity>(initialRevenue.granularity);
  const [revenue, setRevenue] = useState(initialRevenue);
  const [customers, setCustomers] = useState(initialCustomers);
  const [products, setProducts] = useState(initialProducts);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load(nextGranularity = granularity, filters: ReportFilters = { dateFrom, dateTo }) {
    setLoading(true);
    setError(null);
    try {
      const [nextRevenue, nextCustomers, nextProducts] = await fetchReports(filters, nextGranularity);
      setRevenue(nextRevenue);
      setCustomers(nextCustomers);
      setProducts(nextProducts);
    } catch {
      setError("Không tải được dữ liệu báo cáo. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }

  function submit(event: FormEvent) {
    event.preventDefault();
    void load();
  }

  function changeGranularity(next: RevenueGranularity) {
    setGranularity(next);
    void load(next);
  }

  function clearDates() {
    setDateFrom("");
    setDateTo("");
    void load(granularity, {});
  }

  async function loadCustomerPage(page: number) {
    setLoading(true);
    setError(null);
    try {
      setCustomers(await fetchCustomerReport({ dateFrom, dateTo }, page));
    } catch {
      setError("Không tải được trang khách hàng.");
    } finally {
      setLoading(false);
    }
  }

  async function loadProductPage(page: number) {
    setLoading(true);
    setError(null);
    try {
      setProducts(await fetchProductReport({ dateFrom, dateTo }, page));
    } catch {
      setError("Không tải được trang mặt hàng.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Báo cáo bán hàng</h1>
          <p>Thống kê doanh thu theo thời gian, khách hàng và mặt hàng từ dữ liệu đơn hàng thực tế.</p>
        </div>
      </section>

      <section className="kpi-grid">
        <article className="card kpi-card"><div className="kpi-label"><span>Doanh thu gộp</span></div><div className="kpi-value">{money(overview.grossRevenue)}</div></article>
        <article className="card kpi-card"><div className="kpi-label"><span>Doanh thu thực nhận</span></div><div className="kpi-value">{money(overview.realizedRevenue)}</div></article>
        <article className="card kpi-card"><div className="kpi-label"><span>Tổng đơn</span></div><div className="kpi-value">{overview.totalOrders.toLocaleString("vi-VN")}</div></article>
        <article className="card kpi-card"><div className="kpi-label"><span>Đơn chờ xử lý</span></div><div className="kpi-value">{overview.pendingOrders.toLocaleString("vi-VN")}</div></article>
      </section>

      <section className="card panel">
        <form className="report-filter-form" onSubmit={submit}>
          <label>Từ ngày<input type="date" value={dateFrom} max={dateTo || undefined} onChange={(e) => setDateFrom(e.target.value)} /></label>
          <label>Đến ngày<input type="date" value={dateTo} min={dateFrom || undefined} onChange={(e) => setDateTo(e.target.value)} /></label>
          <button className="filter-chip active" type="submit" disabled={loading}>{loading ? "Đang tải..." : "Áp dụng"}</button>
          <button className="filter-chip" type="button" disabled={loading} onClick={clearDates}>Xóa ngày</button>
        </form>
        {error && <p className="action-message report-error">{error}</p>}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div><h2>Bán hàng theo thời gian</h2><p className="panel-copy">{revenue.dateFrom || "Tất cả"} → {revenue.dateTo || "Hiện tại"}</p></div>
          <div className="filters">
            {GRANULARITIES.map((item) => <button key={item.value} type="button" disabled={loading} className={`filter-chip${granularity === item.value ? " active" : ""}`} onClick={() => changeGranularity(item.value)}>{item.label}</button>)}
          </div>
        </div>
        {revenue.points.length ? <RevenueChart data={revenue.points} /> : <p className="report-empty">Không có dữ liệu doanh thu trong khoảng đã chọn.</p>}
      </section>

      <section className="card panel">
        <div className="panel-header"><div><h2>Bán hàng theo khách hàng</h2><p className="panel-copy">Xếp hạng theo tổng giá trị đơn không bị hủy.</p></div></div>
        <div className="table-scroll"><table className="data-table"><thead><tr><th>#</th><th>Khách hàng</th><th>Email</th><th>Số đơn</th><th>Doanh thu</th></tr></thead><tbody>
          {!customers.customers.length && <tr><td colSpan={5} className="report-empty">Không có dữ liệu khách hàng.</td></tr>}
          {customers.customers.map((item, index) => <tr key={item.customerId}><td>{((customers.meta ?? EMPTY_META).page - 1) * (customers.meta ?? EMPTY_META).limit + index + 1}</td><td>{item.customerName}</td><td>{item.email}</td><td>{item.totalOrders.toLocaleString("vi-VN")}</td><td>{money(item.totalRevenue)}</td></tr>)}
        </tbody></table></div>
        <ReportPager label="khách hàng" meta={customers.meta ?? EMPTY_META} loading={loading} onPage={loadCustomerPage} />
      </section>

      <section className="card panel">
        <div className="panel-header"><div><h2>Bán hàng theo mặt hàng</h2><p className="panel-copy">Số lượng và doanh thu từ các đơn không bị hủy.</p></div></div>
        <div className="table-scroll"><table className="data-table"><thead><tr><th>#</th><th>Mặt hàng</th><th>Số lượng bán</th><th>Doanh thu</th></tr></thead><tbody>
          {!products.bestSelling.length && <tr><td colSpan={4} className="report-empty">Không có dữ liệu mặt hàng.</td></tr>}
          {products.bestSelling.map((item, index) => <tr key={item.productId}><td>{((products.meta ?? EMPTY_META).page - 1) * (products.meta ?? EMPTY_META).limit + index + 1}</td><td>{item.productName}</td><td>{item.totalQuantitySold.toLocaleString("vi-VN")}</td><td>{money(item.totalRevenue)}</td></tr>)}
        </tbody></table></div>
        <ReportPager label="mặt hàng" meta={products.meta ?? EMPTY_META} loading={loading} onPage={loadProductPage} />
      </section>
    </main>
  );
}

function ReportPager({ label, meta, loading, onPage }: { label: string; meta: { page: number; limit: number; total: number; totalPages: number }; loading: boolean; onPage: (page: number) => Promise<void> }) {
  return <div className="admin-pager">
    <span className="admin-pager-info">Tổng {meta.total.toLocaleString("vi-VN")} {label}</span>
    <div className="admin-pager-controls">
      <button className="admin-pager-btn" type="button" disabled={loading || meta.page <= 1} onClick={() => void onPage(meta.page - 1)}>Trước</button>
      <span className="admin-pager-page">Trang {meta.page}/{Math.max(1, meta.totalPages)}</span>
      <button className="admin-pager-btn" type="button" disabled={loading || meta.page >= meta.totalPages} onClick={() => void onPage(meta.page + 1)}>Sau</button>
    </div>
  </div>;
}
