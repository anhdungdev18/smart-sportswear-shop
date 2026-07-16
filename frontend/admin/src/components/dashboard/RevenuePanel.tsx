"use client";

import { useState } from "react";
import { RevenueChart } from "@/components/ui/AdminCharts";
import type { RevenueGranularity, RevenueReport } from "@/modules/analytics/api";
import { fetchRevenueReport } from "@/modules/analytics/browser-api";

const OPTIONS: { value: RevenueGranularity; label: string }[] = [
  { value: "DAY", label: "Ngày" },
  { value: "MONTH", label: "Tháng" },
  { value: "YEAR", label: "Năm" }
];

export function RevenuePanel({ initialReport }: { initialReport: RevenueReport }) {
  const [report, setReport] = useState(initialReport);
  const [granularity, setGranularity] = useState<RevenueGranularity>(initialReport.granularity);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function select(next: RevenueGranularity) {
    if (next === granularity || loading) {
      return;
    }
    setGranularity(next);
    setError(null);
    setLoading(true);
    try {
      const data = await fetchRevenueReport(next);
      setReport(data);
    } catch {
      setError("Không tải được doanh thu. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }

  const total = report.points.reduce((sum, point) => sum + point.revenue, 0);
  const hasData = report.points.some((point) => point.revenue > 0);

  return (
    <article className="card panel">
      <div className="panel-header">
        <div>
          <h2>Doanh thu theo thời gian</h2>
          <p className="panel-copy">
            Tổng {Math.round(total).toLocaleString("vi-VN")} ₫ · {report.dateFrom} → {report.dateTo}
          </p>
        </div>
        <div className="filters" role="tablist" aria-label="Mốc thời gian doanh thu">
          {OPTIONS.map((option) => (
            <button
              key={option.value}
              type="button"
              className={`filter-chip${granularity === option.value ? " active" : ""}`}
              onClick={() => void select(option.value)}
              disabled={loading}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {error ? (
        <p className="action-message" style={{ color: "var(--admin-danger)" }}>
          {error}
        </p>
      ) : null}

      <div style={{ opacity: loading ? 0.5 : 1, transition: "opacity 160ms ease" }}>
        {hasData ? (
          <RevenueChart data={report.points} />
        ) : (
          <div className="empty-state">Chưa có doanh thu (đơn đã thanh toán) trong khoảng thời gian này.</div>
        )}
      </div>
    </article>
  );
}
