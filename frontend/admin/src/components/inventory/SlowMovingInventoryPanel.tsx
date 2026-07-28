"use client";

import { useMemo, useState } from "react";
import type { InventoryAgeingItemResponse, InventoryAgeingStatus, InventoryAgeingSummaryResponse } from "@/modules/ai-insights/types";

const labels: Record<InventoryAgeingStatus, string> = {
  NEW_NO_SALES: "Hàng mới chưa bán", WATCH: "Cần theo dõi", SLOW_MOVING: "Chậm luân chuyển",
  DORMANT: "Tồn lâu", DEAD_STOCK: "Tồn chết",
};
type View = "ACTION_REQUIRED" | "NEW" | "ALL";
const money = new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 });

function isActionRequired(item: InventoryAgeingItemResponse) {
  return item.status === "WATCH" || item.status === "SLOW_MOVING" || item.status === "DORMANT" || item.status === "DEAD_STOCK";
}

export function SlowMovingInventoryPanel({ summary }: { summary: InventoryAgeingSummaryResponse | null }) {
  const [view, setView] = useState<View>("ALL");
  const [keyword, setKeyword] = useState("");
  const [visible, setVisible] = useState(25);
  const filtered = useMemo(() => {
    if (!summary) return [];
    const normalized = keyword.trim().toLowerCase();
    return summary.items.filter(item => {
      const matchesView = view === "ALL" || (view === "NEW" ? item.status === "NEW_NO_SALES" : isActionRequired(item));
      return matchesView && (!normalized || item.sku.toLowerCase().includes(normalized) || item.productName.toLowerCase().includes(normalized));
    });
  }, [summary, view, keyword]);
  if (!summary) return null;

  const selectView = (next: View) => { setView(next); setVisible(25); };
  return <section className="card panel">
    <div className="panel-header"><div>
      <h2>Vòng đời tồn kho và kế hoạch xử lý</h2>
      <p className="panel-copy">Toàn bộ {summary.variantsWithStock.toLocaleString("vi-VN")} SKU còn tồn đều được phân tích; chỉ SKU đủ tín hiệu mới chuyển sang dự báo nhu cầu.</p>
    </div></div>

    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(155px,1fr))", gap: 12, marginBottom: 18 }}>
      <div className="admin-subcard"><span className="table-subtle">Hàng mới chưa bán</span><strong style={{ fontSize: 26 }}>{summary.newNoSalesVariants}</strong></div>
      <div className="admin-subcard"><span className="table-subtle">Cần theo dõi</span><strong style={{ fontSize: 26 }}>{summary.watchVariants}</strong></div>
      <div className="admin-subcard"><span className="table-subtle">Chậm luân chuyển</span><strong style={{ fontSize: 26 }}>{summary.slowMovingVariants}</strong></div>
      <div className="admin-subcard"><span className="table-subtle">Tồn lâu / tồn chết</span><strong style={{ fontSize: 26 }}>{summary.dormantVariants + summary.deadStockVariants}</strong></div>
      <div className="admin-subcard"><span className="table-subtle">Giá trị cần xử lý</span><strong style={{ fontSize: 22 }}>{money.format(summary.estimatedAtRiskValue)}</strong></div>
    </div>

    <div className="table-toolbar" style={{ alignItems: "center" }}>
      <div className="filter-tabs">
        <button className={view === "ACTION_REQUIRED" ? "active" : ""} onClick={() => selectView("ACTION_REQUIRED")}>Cần theo dõi/xử lý ({summary.watchVariants + summary.slowMovingVariants + summary.dormantVariants + summary.deadStockVariants})</button>
        <button className={view === "NEW" ? "active" : ""} onClick={() => selectView("NEW")}>Hàng mới ({summary.newNoSalesVariants})</button>
        <button className={view === "ALL" ? "active" : ""} onClick={() => selectView("ALL")}>Tất cả ({summary.items.length})</button>
      </div>
      <input className="admin-input" aria-label="Tìm SKU tồn kho" placeholder="Tìm SKU hoặc sản phẩm..." value={keyword} onChange={event => { setKeyword(event.target.value); setVisible(25); }} style={{ maxWidth: 330 }} />
    </div>

    {view === "ACTION_REQUIRED" && filtered.length === 0 ? <div className="empty-state" style={{ padding: "32px 16px" }}>
      <strong>Chưa có hàng chậm luân chuyển cần giải phóng.</strong>
      <div className="table-subtle">Có {summary.newNoSalesVariants} SKU đang ở giai đoạn hàng mới. Các SKU sẽ chuyển sang “cần theo dõi” sau 30 ngày không bán.</div>
    </div> : <>
      <div style={{ overflowX: "auto" }}><table className="data-table"><thead><tr>
        <th>Trạng thái</th><th>SKU / sản phẩm</th><th>Tồn</th><th>Không bán</th><th>Giá trị tồn</th><th>Điểm</th><th>Hướng xử lý</th>
      </tr></thead><tbody>
        {filtered.slice(0, visible).map(item => <tr key={item.variantId}>
          <td><span className={`status ${item.status === "DEAD_STOCK" ? "low" : item.status === "DORMANT" ? "medium" : "neutral"}`}>{labels[item.status]}</span></td>
          <td><strong>{item.sku}</strong><div className="table-subtle">{item.productName} · {item.color}/{item.size}</div></td>
          <td>{item.availableQuantity}</td><td>{item.daysWithoutSale} ngày</td><td>{money.format(item.estimatedInventoryValue)}</td><td><strong>{item.urgencyScore}/100</strong></td>
          <td><span className="table-subtle">{item.recommendedActions.join(" ")}</span></td>
        </tr>)}
        {!filtered.length && <tr><td colSpan={7} className="empty-state">Không có SKU phù hợp bộ lọc.</td></tr>}
      </tbody></table></div>
      {visible < filtered.length && <div style={{ display: "flex", justifyContent: "center", marginTop: 16 }}><button className="admin-btn secondary" onClick={() => setVisible(value => value + 25)}>Xem thêm ({filtered.length - visible})</button></div>}
    </>}
  </section>;
}
