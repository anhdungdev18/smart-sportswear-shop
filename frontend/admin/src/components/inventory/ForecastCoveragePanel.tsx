import type { DemandClassificationResponse } from "@/modules/ai-insights/types";

export function ForecastCoveragePanel({ rows }: { rows: DemandClassificationResponse[] }) {
  const noDemand = rows.filter(row => row.classification === "NO_DEMAND").length;
  const insufficient = rows.filter(row => row.classification === "INSUFFICIENT_DATA").length;
  const forecastable = rows.length - noDemand - insufficient;
  return <section className="card panel">
    <div className="panel-header"><div>
      <h2>Phạm vi xử lý AI</h2>
      <p className="panel-copy">Số SKU dự báo không phải tổng SKU được phân tích. Hệ thống tự chọn luồng phù hợp theo tín hiệu bán hàng.</p>
    </div></div>
    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(190px,1fr))", gap: 12 }}>
      <div className="admin-subcard"><span className="table-subtle">Tổng SKU REAL đã phân loại</span><strong style={{ fontSize: 28 }}>{rows.length}</strong></div>
      <div className="admin-subcard"><span className="table-subtle">Được backtest và dự báo</span><strong style={{ fontSize: 28 }}>{forecastable}</strong><small>Có tín hiệu bán hàng phù hợp</small></div>
      <div className="admin-subcard"><span className="table-subtle">Chuyển sang quản trị tồn kho</span><strong style={{ fontSize: 28 }}>{noDemand}</strong><small>Không phát sinh nhu cầu</small></div>
      <div className="admin-subcard"><span className="table-subtle">Tín hiệu quá thưa</span><strong style={{ fontSize: 28 }}>{insufficient}</strong><small>Theo dõi/cold-start, chưa nhập mạnh</small></div>
    </div>
  </section>;
}
