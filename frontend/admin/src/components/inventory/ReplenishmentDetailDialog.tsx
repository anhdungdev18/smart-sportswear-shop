"use client";

import { useEffect, useMemo, useState } from "react";
import { CartesianGrid, Legend, Line, LineChart, ReferenceArea, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { ReplenishmentSuggestionDetailResponse, ReplenishmentSuggestionResponse } from "@/modules/replenishment/types";
import { acceptSuggestion, adjustSuggestion, dismissSuggestion, getSuggestionDetail } from "@/modules/replenishment/browser-api";

interface Props {
  suggestion: ReplenishmentSuggestionResponse;
  onClose: () => void;
  onUpdated: () => void;
  onFillImport: (draft: { variantId: string; quantity: number; recommendationId: string; sku: string }) => void;
}

export function ReplenishmentDetailDialog({ suggestion, onClose, onUpdated, onFillImport }: Props) {
  const [detail, setDetail] = useState<ReplenishmentSuggestionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [adjustQuantity, setAdjustQuantity] = useState(suggestion.suggestedQuantity);
  const [note, setNote] = useState("");

  useEffect(() => {
    setLoading(true);
    setLoadError(null);
    getSuggestionDetail(suggestion.id)
      .then(setDetail)
      .catch((error) => {
        console.error(error);
        setLoadError("Không tải được lịch sử dự báo. Hãy kiểm tra phiên Admin và AI service.");
      })
      .finally(() => setLoading(false));
  }, [suggestion.id]);

  const chartData = useMemo(() => detail ? [...detail.historyData, ...detail.futureForecastData] : [], [detail]);
  const backtestPoints = detail?.historyData.filter((point) => point.backtestPeriod) ?? [];
  const formula = detail?.explanationJson?.formula ?? {};

  async function runAction(action: () => Promise<unknown>, fallback: string) {
    setActionLoading(true);
    try {
      await action();
      onUpdated();
      onClose();
    } catch (error) {
      console.error(error);
      window.alert(fallback);
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div role="dialog" aria-modal="true" aria-label="Chi tiết đề xuất nhập hàng" style={{ position: "fixed", inset: 0, backgroundColor: "rgba(0,0,0,.5)", zIndex: 9999, display: "flex", alignItems: "center", justifyContent: "center", padding: "2rem" }}>
      <div className="card panel" style={{ width: "100%", maxWidth: 980, maxHeight: "92vh", overflowY: "auto", position: "relative" }}>
        <button aria-label="Đóng" onClick={onClose} style={{ position: "absolute", top: 10, right: 10, background: "none", border: 0, cursor: "pointer", fontSize: 20 }}>✕</button>
        <div className="panel-header"><h2>Chi tiết đề xuất nhập hàng</h2><p className="panel-copy">{suggestion.sku} · {suggestion.productName}</p></div>

        {loading && <div className="empty-state">Đang tải lịch sử và backtest…</div>}
        {loadError && <div className="empty-state" role="alert" style={{ color: "var(--admin-danger, #b91c1c)" }}>{loadError}</div>}
        {!loading && !loadError && detail && <>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(220px,1fr))", gap: "1rem" }}>
            <section className="card panel"><h3>Dự báo</h3><p>Mô hình: <strong>{detail.selectedModel}</strong></p><p>Nhu cầu/ngày: <strong>{detail.averageDailyDemand?.toFixed(2)}</strong></p><p>Độ tin cậy: <strong>{detail.confidence}</strong></p><small>{detail.selectionReason}</small></section>
            <section className="card panel"><h3>Chính sách tồn kho</h3><p>Lead time: {detail.policyLeadTimeDays} ngày</p><p>Target cover: {detail.policyTargetCoverDays} ngày</p><p>Service level: {(detail.policyServiceLevel * 100).toFixed(0)}%</p></section>
            <section className="card panel"><h3>Đề xuất</h3><p>Khả dụng: {detail.availableQuantity}</p><p>Safety stock: {detail.safetyStock}</p><p>Reorder point: {detail.reorderPoint}</p><p>Đề xuất nhập: <strong>{detail.suggestedQuantity}</strong></p></section>
          </div>

          <section style={{ marginTop: "1.25rem" }}><h3>So sánh mô hình</h3><div style={{ overflowX: "auto" }}><table className="data-table"><thead><tr><th>Mô hình</th><th>MAE</th><th>WAPE</th><th>Kết quả</th></tr></thead><tbody>{detail.modelMetrics.map((metric) => <tr key={metric.algorithm} style={metric.selected ? { fontWeight: 700, background: "rgba(21,128,61,.08)" } : undefined}><td>{metric.algorithm}</td><td>{metric.mae.toFixed(3)}</td><td>{metric.wape == null ? "—" : `${(metric.wape * 100).toFixed(1)}%`}</td><td>{metric.selected ? "Được chọn" : "—"}</td></tr>)}</tbody></table></div></section>

          <section style={{ marginTop: "1.25rem" }}><h3>Lịch sử, backtest và dự báo tương lai</h3>{chartData.length === 0 ? <div className="empty-state">SKU chưa có dữ liệu lịch sử để vẽ biểu đồ.</div> : <div style={{ width: "100%", height: 360 }}><ResponsiveContainer><LineChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="date" minTickGap={36} /><YAxis allowDecimals={false} /><Tooltip /><Legend />{backtestPoints.length > 0 && <ReferenceArea x1={backtestPoints[0].date} x2={backtestPoints.at(-1)?.date} fill="#f59e0b" fillOpacity={0.1} label="Backtest" />}<Line type="monotone" dataKey="actual" stroke="#4f46e5" dot={false} name="Thực tế" connectNulls={false} /><Line type="monotone" dataKey="forecast" stroke="#16a34a" dot={false} strokeWidth={2} name="Dự báo" connectNulls /></LineChart></ResponsiveContainer></div>}</section>

          <div style={{ marginTop: "1rem" }}><button className="admin-btn" type="button" onClick={() => onFillImport({ variantId: suggestion.variantId, quantity: suggestion.suggestedQuantity, recommendationId: suggestion.id, sku: suggestion.sku })}>Điền form nhập kho</button><small style={{ marginLeft: ".75rem" }}>Chỉ điền dữ liệu, chưa thay đổi tồn kho.</small></div><section className="card panel" style={{ marginTop: "1.25rem" }}><h3>Giải thích công thức</h3><p>{String(detail.explanationJson?.summary ?? "Đề xuất được tính từ nhu cầu dự báo và chính sách tồn kho.")}</p><ul>{((detail.explanationJson?.reasons as string[] | undefined) ?? []).map((reason) => <li key={reason}>{reason}</li>)}</ul>{Object.keys(formula).length > 0 && <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(180px,1fr))", gap: ".5rem" }}>{Object.entries(formula).map(([key, value]) => <div key={key}><small>{key}</small><div><strong>{value}</strong></div></div>)}</div>}</section>

          {suggestion.status === "PENDING" && <section style={{ background: "var(--admin-bg)", padding: "1rem", borderRadius: 8, marginTop: "1rem" }}><h3>Quyết định của Admin</h3><div style={{ display: "flex", flexWrap: "wrap", gap: "1rem", marginTop: ".5rem" }}><input className="admin-input" placeholder="Ghi chú (bắt buộc khi bỏ qua)" value={note} onChange={(event) => setNote(event.target.value)} /><input className="admin-input" type="number" min={0} value={adjustQuantity} onChange={(event) => setAdjustQuantity(Number(event.target.value))} style={{ width: 120 }} /></div><div style={{ display: "flex", flexWrap: "wrap", gap: ".5rem", marginTop: "1rem" }}><button className="admin-btn" disabled={actionLoading} onClick={() => void runAction(() => acceptSuggestion(suggestion.id, { note: note || undefined }), "Lỗi khi duyệt đề xuất")}>Duyệt {suggestion.suggestedQuantity}</button><button className="admin-btn" disabled={actionLoading || adjustQuantity < 0} onClick={() => void runAction(() => adjustSuggestion(suggestion.id, { quantity: adjustQuantity, note: note || undefined }), "Lỗi khi điều chỉnh")}>Điều chỉnh</button><button className="admin-btn" disabled={actionLoading} onClick={() => { if (!note.trim()) { window.alert("Vui lòng nhập ghi chú khi bỏ qua"); return; } void runAction(() => dismissSuggestion(suggestion.id, { note }), "Lỗi khi bỏ qua"); }} style={{ background: "var(--admin-danger)", color: "white" }}>Bỏ qua</button></div></section>}
        </>}
      </div>
    </div>
  );
}