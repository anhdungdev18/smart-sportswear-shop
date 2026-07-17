"use client";

import { useState, useEffect } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import type { ReplenishmentSuggestionResponse, ReplenishmentSuggestionDetailResponse } from "@/modules/replenishment/types";
import { getSuggestionDetail, acceptSuggestion, adjustSuggestion, dismissSuggestion } from "@/modules/replenishment/browser-api";

interface Props {
  suggestion: ReplenishmentSuggestionResponse;
  onClose: () => void;
  onUpdated: () => void;
}

export function ReplenishmentDetailDialog({ suggestion, onClose, onUpdated }: Props) {
  const [detail, setDetail] = useState<ReplenishmentSuggestionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [adjustQuantity, setAdjustQuantity] = useState(suggestion.suggestedQuantity);
  const [note, setNote] = useState("");

  useEffect(() => {
    getSuggestionDetail(suggestion.id)
      .then((res) => {
        setDetail(res);
      })
      .catch((err) => {
        console.error(err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [suggestion.id]);

  async function handleAccept() {
    setActionLoading(true);
    try {
      await acceptSuggestion(suggestion.id, { note: note || undefined });
      onUpdated();
      onClose();
    } catch (e) {
      console.error(e);
      alert("Lỗi khi duyệt");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleAdjust() {
    setActionLoading(true);
    try {
      await adjustSuggestion(suggestion.id, { quantity: adjustQuantity, note: note || undefined });
      onUpdated();
      onClose();
    } catch (e) {
      console.error(e);
      alert("Lỗi khi điều chỉnh");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDismiss() {
    if (!note) {
      alert("Vui lòng nhập ghi chú khi bỏ qua");
      return;
    }
    setActionLoading(true);
    try {
      await dismissSuggestion(suggestion.id, { note });
      onUpdated();
      onClose();
    } catch (e) {
      console.error(e);
      alert("Lỗi khi bỏ qua");
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div style={{
      position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: "rgba(0,0,0,0.5)", zIndex: 9999,
      display: "flex", alignItems: "center", justifyContent: "center", padding: "2rem"
    }}>
      <div className="card panel" style={{ width: "100%", maxWidth: 800, maxHeight: "90vh", overflowY: "auto", position: "relative" }}>
        <button onClick={onClose} style={{ position: "absolute", top: 10, right: 10, background: "none", border: "none", cursor: "pointer", fontSize: 20 }}>
          ✕
        </button>
        <div className="panel-header">
          <h2>Chi tiết đề xuất nhập hàng</h2>
          <p className="panel-copy">{suggestion.sku} - {suggestion.productName}</p>
        </div>

        {loading ? (
          <p>Đang tải dữ liệu...</p>
        ) : detail ? (
          <div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem", marginBottom: "1rem" }}>
              <div>
                <h3>Thông số dự báo</h3>
                <ul style={{ paddingLeft: "1.5rem", marginTop: "0.5rem" }}>
                  <li>Thuật toán: {detail.algorithm}</li>
                  <li>Tồn khả dụng: {detail.availableQuantity}</li>
                  <li>Nhu cầu / ngày: {detail.averageDailyDemand?.toFixed(2)}</li>
                  <li>Safety Stock: {detail.safetyStock}</li>
                  <li>Reorder Point: {detail.reorderPoint}</li>
                  <li>Ngày hết dự kiến: {detail.estimatedStockoutDays ?? "Không hết"}</li>
                </ul>
              </div>
              <div>
                <h3>Giải thích ({detail.explanationJson?.summary})</h3>
                <ul style={{ paddingLeft: "1.5rem", marginTop: "0.5rem" }}>
                  {(detail.explanationJson?.reasons as string[] || []).map((r, i) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              </div>
            </div>

            {detail.historyData && detail.historyData.length > 0 && (
              <div style={{ height: 300, marginBottom: "1.5rem" }}>
                <h3>Biểu đồ lịch sử vs backtest</h3>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={detail.historyData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="actual" stroke="#8884d8" name="Thực tế" />
                    <Line type="monotone" dataKey="forecast" stroke="#82ca9d" name="Dự báo" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            )}

            {suggestion.status === "PENDING" && (
              <div style={{ background: "var(--admin-bg)", padding: "1rem", borderRadius: "8px", marginTop: "1rem" }}>
                <h3>Quyết định</h3>
                <div style={{ display: "flex", gap: "1rem", marginTop: "0.5rem", alignItems: "center" }}>
                  <input className="admin-input" placeholder="Ghi chú (bắt buộc khi bỏ qua)" value={note} onChange={e => setNote(e.target.value)} />
                  <input className="admin-input" type="number" value={adjustQuantity} onChange={e => setAdjustQuantity(Number(e.target.value))} style={{ width: 100 }} title="Số lượng muốn điều chỉnh" />
                </div>
                <div style={{ display: "flex", gap: "0.5rem", marginTop: "1rem" }}>
                  <button className="admin-btn" disabled={actionLoading} onClick={handleAccept}>Duyệt {suggestion.suggestedQuantity}</button>
                  <button className="admin-btn" disabled={actionLoading} onClick={handleAdjust}>Điều chỉnh</button>
                  <button className="admin-btn" disabled={actionLoading} onClick={handleDismiss} style={{ background: "var(--admin-danger)", color: "white" }}>Bỏ qua</button>
                </div>
              </div>
            )}
          </div>
        ) : (
          <p>Không thể tải chi tiết.</p>
        )}
      </div>
    </div>
  );
}
