"use client";

import { useDeferredValue, useMemo, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { adjustStock, exportStock, importStock } from "@/modules/inventory/browser-api";
import type { InventoryItemResponse, InventoryTransactionResponse } from "@/modules/inventory/types";

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

type ActionType = "IMPORT" | "EXPORT" | "ADJUSTMENT_UP" | "ADJUSTMENT_DOWN";

export function AdminInventoryClient({
  initialItems,
  initialTransactions
}: {
  initialItems: InventoryItemResponse[];
  initialTransactions: InventoryTransactionResponse[];
}) {
  const [items, setItems] = useState(initialItems);
  const [transactions, setTransactions] = useState(initialTransactions);
  const [variantId, setVariantId] = useState(initialItems[0]?.variantId ?? "");
  const [quantity, setQuantity] = useState(1);
  const [actionType, setActionType] = useState<ActionType>("IMPORT");
  const [note, setNote] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);

  const filteredItems = useMemo(() => {
    const query = deferredSearch.trim().toLowerCase();
    if (!query) return items;
    return items.filter((item) => [item.sku, item.productName, item.size ?? "", item.color ?? ""].join(" ").toLowerCase().includes(query));
  }, [deferredSearch, items]);

  async function handleSubmit() {
    try {
      setSaving(true);
      setMessage(null);
      let updated: InventoryItemResponse;

      if (actionType === "IMPORT") {
        updated = await importStock({ variantId, quantity, note: note.trim() || undefined });
      } else if (actionType === "EXPORT") {
        updated = await exportStock({ variantId, quantity, note: note.trim() || undefined });
      } else {
        updated = await adjustStock({ variantId, quantity, type: actionType, note: note.trim() || undefined });
      }

      setItems((current) => current.map((item) => (item.variantId === updated.variantId ? updated : item)));
      setTransactions((current) => [
        {
          id: `${Date.now()}`,
          variantId: updated.variantId,
          sku: updated.sku,
          orderId: null,
          type: actionType,
          quantity,
          beforeStockQuantity: 0,
          afterStockQuantity: updated.stockQuantity,
          beforeReservedQuantity: 0,
          afterReservedQuantity: updated.reservedQuantity,
          note,
          createdById: null,
          createdByName: "Bạn",
          createdAt: new Date().toISOString()
        },
        ...current
      ]);
      setMessage("Đã cập nhật tồn kho thành công.");
      setNote("");
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được tồn kho"));
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <section className="card panel">
        <div className="panel-header">
          <h2>Thao tác kho</h2>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-inline-form wrap">
          <select className="select" value={variantId} onChange={(event) => setVariantId(event.target.value)}>
            {items.map((item) => (
              <option value={item.variantId} key={item.variantId}>
                {item.sku} · {item.productName}
              </option>
            ))}
          </select>
          <select className="select" value={actionType} onChange={(event) => setActionType(event.target.value as ActionType)}>
            <option value="IMPORT">Nhập kho</option>
            <option value="EXPORT">Xuất kho</option>
            <option value="ADJUSTMENT_UP">Điều chỉnh tăng</option>
            <option value="ADJUSTMENT_DOWN">Điều chỉnh giảm</option>
          </select>
          <input className="admin-input" type="number" min={1} value={quantity} onChange={(event) => setQuantity(Number(event.target.value) || 1)} />
          <input className="admin-input" placeholder="Ghi chú" value={note} onChange={(event) => setNote(event.target.value)} />
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : "Xác nhận"}
          </button>
        </div>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Tồn kho hiện tại</h2>
            <p className="panel-copy">Theo dõi số lượng thực, số lượng đang giữ và lượng khả dụng theo từng biến thể.</p>
          </div>
          <input
            className="admin-input"
            style={{ width: 280 }}
            placeholder="Tìm theo SKU, tên sản phẩm, size hoặc màu..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>Sản phẩm</th>
              <th>Size/Màu</th>
              <th>Tồn thực</th>
              <th>Đang giữ</th>
              <th>Khả dụng</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ textAlign: "center", color: "var(--admin-muted)" }}>
                  Không có biến thể nào khớp bộ lọc hiện tại.
                </td>
              </tr>
            ) : null}
            {filteredItems.map((item) => (
              <tr key={item.variantId}>
                <td>{item.sku}</td>
                <td>{item.productName}</td>
                <td>
                  {item.size ?? "-"} / {item.color ?? "-"}
                </td>
                <td>{item.stockQuantity}</td>
                <td>{item.reservedQuantity}</td>
                <td>{item.availableQuantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>Giao dịch kho gần đây</h2>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Thời gian</th>
              <th>SKU</th>
              <th>Loại</th>
              <th>Số lượng</th>
              <th>Người thao tác</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((item) => (
              <tr key={item.id}>
                <td>{new Date(item.createdAt).toLocaleString("vi-VN")}</td>
                <td>{item.sku}</td>
                <td>{item.type}</td>
                <td>{item.quantity}</td>
                <td>{item.createdByName ?? "-"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
