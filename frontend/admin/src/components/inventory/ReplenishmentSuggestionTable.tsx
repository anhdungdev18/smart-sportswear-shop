"use client";

import { useMemo, useState } from "react";
import type { ReplenishmentPriority, ReplenishmentStatus, ReplenishmentSuggestionResponse } from "@/modules/replenishment/types";
import { generateForecast, listSuggestions } from "@/modules/replenishment/browser-api";
import { ReplenishmentDetailDialog } from "./ReplenishmentDetailDialog";

interface Props {
  initialSuggestions: ReplenishmentSuggestionResponse[];
  onFillImport: (draft: { variantId: string; quantity: number; recommendationId: string; sku: string }) => void;
}
type StatusFilter = "ALL" | ReplenishmentStatus;
type PriorityFilter = "ALL" | ReplenishmentPriority;

export function ReplenishmentSuggestionTable({ initialSuggestions, onFillImport }: Props) {
  const [suggestions, setSuggestions] = useState(initialSuggestions);
  const [selected, setSelected] = useState<ReplenishmentSuggestionResponse | null>(null);
  const [status, setStatus] = useState<StatusFilter>("PENDING");
  const [priority, setPriority] = useState<PriorityFilter>("ALL");
  const [keyword, setKeyword] = useState("");
  const [generating, setGenerating] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const filtered = useMemo(() => suggestions.filter((item) => {
    const normalized = keyword.trim().toLowerCase();
    return (status === "ALL" || item.status === status)
      && (priority === "ALL" || item.priority === priority)
      && (!normalized || item.sku.toLowerCase().includes(normalized) || item.productName.toLowerCase().includes(normalized));
  }), [suggestions, status, priority, keyword]);

  const pending = suggestions.filter((item) => item.status === "PENDING");
  const criticalCount = pending.filter((item) => item.priority === "CRITICAL").length;
  const highCount = pending.filter((item) => item.priority === "HIGH").length;
  const suggestedUnits = pending.reduce((total, item) => total + item.suggestedQuantity, 0);

  async function refresh() {
    const page = await listSuggestions();
    setSuggestions(page.content);
  }

  async function handleGenerate() {
    if (generating) return;
    setGenerating(true); setError(null); setMessage(null);
    try {
      await generateForecast();
      await refresh();
      setMessage("Đã đồng bộ dữ liệu và sinh đề xuất mới thành công.");
    } catch (cause) {
      console.error(cause);
      setError("Không thể sinh đề xuất. Hãy kiểm tra phiên Admin và trạng thái AI service.");
    } finally { setGenerating(false); }
  }

  return <>
    <section className="card panel">
      <div className="panel-header" style={{ display: "flex", justifyContent: "space-between", gap: "1rem", alignItems: "center" }}>
        <div><h2>AI đề xuất nhập hàng</h2><p className="panel-copy">Dự báo từ lịch sử bán hàng, không tự động thay đổi tồn kho.</p></div>
        <button className="admin-btn" onClick={() => void handleGenerate()} disabled={generating}>
          {generating ? "Đang đồng bộ và dự báo..." : "Chạy dự báo AI"}
        </button>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(150px,1fr))", gap: "0.75rem", marginBottom: "1rem" }}>
        <div className="card panel"><strong>{pending.length}</strong><div>Đang chờ xử lý</div></div>
        <div className="card panel"><strong>{criticalCount}</strong><div>Khẩn cấp</div></div>
        <div className="card panel"><strong>{highCount}</strong><div>Ưu tiên cao</div></div>
        <div className="card panel"><strong>{suggestedUnits}</strong><div>Tổng lượng đề xuất</div></div>
      </div>

      {message && <p role="status" style={{ color: "var(--admin-success, #15803d)" }}>{message}</p>}
      {error && <p role="alert" style={{ color: "var(--admin-danger, #b91c1c)" }}>{error}</p>}

      <div style={{ display: "flex", flexWrap: "wrap", gap: "0.75rem", margin: "1rem 0" }}>
        <input className="admin-input" aria-label="Tìm SKU hoặc sản phẩm" placeholder="Tìm SKU hoặc sản phẩm" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
        <select className="admin-input" aria-label="Lọc trạng thái" value={status} onChange={(event) => setStatus(event.target.value as StatusFilter)}>
          <option value="ALL">Tất cả trạng thái</option><option value="PENDING">Chờ xử lý</option><option value="ACCEPTED">Đã duyệt</option><option value="ADJUSTED">Đã điều chỉnh</option><option value="DISMISSED">Đã bỏ qua</option><option value="RECEIVED">Đã nhận hàng</option>
        </select>
        <select className="admin-input" aria-label="Lọc ưu tiên" value={priority} onChange={(event) => setPriority(event.target.value as PriorityFilter)}>
          <option value="ALL">Tất cả ưu tiên</option><option value="CRITICAL">Khẩn cấp</option><option value="HIGH">Cao</option><option value="MEDIUM">Trung bình</option><option value="LOW">Thấp</option>
        </select>
      </div>

      <div style={{ overflowX: "auto" }}><table className="data-table"><thead><tr>
        <th>Ưu tiên</th><th>SKU / Sản phẩm</th><th>Khả dụng</th><th>Đề xuất</th><th>Hết sau</th><th>Mô hình</th><th>WAPE</th><th>Trạng thái</th><th></th>
      </tr></thead><tbody>
        {filtered.length === 0 && <tr><td colSpan={9} className="empty-state">Không có đề xuất phù hợp bộ lọc.</td></tr>}
        {filtered.map((item) => <tr key={item.id}>
          <td><strong>{item.priority}</strong></td><td><div>{item.sku}</div><small>{item.productName} · {item.color}/{item.size}</small></td>
          <td>{item.availableQuantity}</td><td><strong>{item.suggestedQuantity}</strong></td><td>{item.estimatedStockoutDays == null ? "—" : `${item.estimatedStockoutDays} ngày`}</td>
          <td>{item.algorithm}</td><td>{item.wape == null ? "—" : `${(item.wape * 100).toFixed(1)}%`}</td><td>{item.status}</td>
          <td><button className="admin-btn" onClick={() => setSelected(item)}>Chi tiết</button></td>
        </tr>)}
      </tbody></table></div>
    </section>
    {selected && <ReplenishmentDetailDialog suggestion={selected} onClose={() => setSelected(null)} onUpdated={() => { setSelected(null); void refresh(); }} onFillImport={(draft) => { onFillImport(draft); setSelected(null); }} />}
  </>;
}