"use client";

import { useState } from "react";
import type { ReplenishmentSuggestionResponse } from "@/modules/replenishment/types";
import { ReplenishmentDetailDialog } from "./ReplenishmentDetailDialog";
import { generateForecast } from "@/modules/replenishment/browser-api";

interface Props {
  initialSuggestions: ReplenishmentSuggestionResponse[];
}

export function ReplenishmentSuggestionTable({ initialSuggestions }: Props) {
  const [suggestions, setSuggestions] = useState(initialSuggestions);
  const [selected, setSelected] = useState<ReplenishmentSuggestionResponse | null>(null);
  const [generating, setGenerating] = useState(false);

  // Filter out non-pending for MVP display simplicity, or just display them
  const activeSuggestions = suggestions.filter(s => s.status === 'PENDING');

  const criticalCount = activeSuggestions.filter(s => s.priority === 'CRITICAL').length;
  const highCount = activeSuggestions.filter(s => s.priority === 'HIGH').length;

  async function handleGenerate() {
    setGenerating(true);
    try {
      await generateForecast();
      alert("Đã sinh đề xuất thành công. Vui lòng tải lại trang để xem kết quả.");
    } catch (e) {
      console.error(e);
      alert("Lỗi khi sinh đề xuất");
    } finally {
      setGenerating(false);
    }
  }

  return (
    <>
      <section className="card panel">
        <div className="panel-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <h2>AI Replenishment Suggestions</h2>
            <p className="panel-copy">
              Đang có {criticalCount} đề xuất Khẩn cấp, {highCount} đề xuất Ưu tiên cao.
            </p>
          </div>
          <button className="admin-btn" onClick={() => void handleGenerate()} disabled={generating}>
            {generating ? "Đang xử lý..." : "Chạy AI Dự báo & Đề xuất"}
          </button>
        </div>

        <table className="data-table">
          <thead>
            <tr>
              <th>Ưu tiên</th>
              <th>SKU / SP</th>
              <th>Khả dụng</th>
              <th>Cần nhập</th>
              <th>Hết sau</th>
              <th>Mô hình</th>
              <th>Tin cậy</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {activeSuggestions.length === 0 ? (
              <tr>
                <td colSpan={8} style={{ textAlign: "center", color: "var(--admin-muted)" }}>
                  Không có đề xuất nhập hàng nào đang chờ xử lý.
                </td>
              </tr>
            ) : null}
            {activeSuggestions.map((item) => (
              <tr key={item.id}>
                <td style={{ fontWeight: "bold", color: item.priority === 'CRITICAL' ? 'red' : item.priority === 'HIGH' ? 'orange' : 'inherit' }}>
                  {item.priority}
                </td>
                <td>
                  <div>{item.sku}</div>
                  <div style={{ fontSize: "0.85em", color: "var(--admin-muted)" }}>{item.productName}</div>
                </td>
                <td>{item.availableQuantity}</td>
                <td style={{ fontWeight: "bold" }}>{item.suggestedQuantity}</td>
                <td>{item.estimatedStockoutDays != null ? `${item.estimatedStockoutDays} ngày` : "-"}</td>
                <td>{item.algorithm}</td>
                <td>{item.confidence}</td>
                <td>
                  <button className="admin-btn" onClick={() => setSelected(item)} style={{ padding: "0.25rem 0.5rem" }}>
                    Chi tiết
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {selected && (
        <ReplenishmentDetailDialog
          suggestion={selected}
          onClose={() => setSelected(null)}
          onUpdated={() => {
            // refresh page
            window.location.reload();
          }}
        />
      )}
    </>
  );
}
