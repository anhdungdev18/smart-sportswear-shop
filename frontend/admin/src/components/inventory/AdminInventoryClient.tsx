"use client";

import Image from "next/image";
import { useDeferredValue, useEffect, useMemo, useRef, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { adjustStock, exportStock, importStock } from "@/modules/inventory/browser-api";
import type { InventoryItemResponse, InventoryTransactionResponse } from "@/modules/inventory/types";
import { NO_IMAGE } from "@/modules/ui/placeholder";

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

type ActionType = "IMPORT" | "EXPORT" | "ADJUSTMENT_UP" | "ADJUSTMENT_DOWN";

function variantLabel(item: InventoryItemResponse) {
  const variant = item.size || item.color ? ` · ${item.size ?? "-"}/${item.color ?? "-"}` : "";
  return `${item.sku} · ${item.productName}${variant}`;
}

// Single search box that doubles as the dropdown trigger: typing filters the
// option list shown right below the input instead of relying on a separate
// search field next to a plain <select>.
function VariantCombobox({
  options,
  value,
  onChange
}: {
  options: InventoryItemResponse[];
  value: string;
  onChange: (variantId: string) => void;
}) {
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const deferredQuery = useDeferredValue(query);

  const selected = options.find((item) => item.variantId === value) ?? null;

  const filtered = useMemo(() => {
    const q = deferredQuery.trim().toLowerCase();
    if (!q) return options;
    return options.filter((item) => [item.sku, item.productName, item.size ?? "", item.color ?? ""].join(" ").toLowerCase().includes(q));
  }, [deferredQuery, options]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function selectItem(item: InventoryItemResponse) {
    onChange(item.variantId);
    setQuery("");
    setOpen(false);
  }

  return (
    <div className="combobox" ref={containerRef}>
      <input
        className="admin-input combobox-input"
        placeholder="Tìm SKU, sản phẩm, size, màu..."
        value={open ? query : (selected ? variantLabel(selected) : "")}
        onFocus={() => {
          setQuery("");
          setOpen(true);
        }}
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
        }}
      />
      {open ? (
        <div className="combobox-list">
          {filtered.length === 0 ? (
            <div className="combobox-empty">Không tìm thấy biến thể phù hợp</div>
          ) : (
            filtered.map((item) => (
              <button
                type="button"
                key={item.variantId}
                className={`combobox-option${item.variantId === value ? " active" : ""}`}
                onClick={() => selectItem(item)}
              >
                <Image
                  className="combobox-thumb"
                  src={item.thumbnail || NO_IMAGE}
                  alt={item.productName}
                  width={40}
                  height={40}
                  unoptimized
                  onError={(event) => {
                    const img = event.currentTarget as HTMLImageElement;
                    if (img.src !== NO_IMAGE) {
                      img.src = NO_IMAGE;
                    }
                  }}
                />
                <span className="combobox-option-text">
                  <strong>{item.productName}</strong>
                  <span className="table-subtle">
                    {item.sku}
                    {item.size || item.color ? ` · ${item.size ?? "-"}/${item.color ?? "-"}` : ""}
                  </span>
                </span>
              </button>
            ))
          )}
        </div>
      ) : null}
    </div>
  );
}

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
          <div style={{ width: 460, maxWidth: "100%" }}>
            <VariantCombobox options={items} value={variantId} onChange={setVariantId} />
          </div>
          <select className="select" value={actionType} onChange={(event) => setActionType(event.target.value as ActionType)}>
            <option value="IMPORT">Nhập kho</option>
            <option value="EXPORT">Xuất kho</option>
            <option value="ADJUSTMENT_UP">Điều chỉnh tăng</option>
            <option value="ADJUSTMENT_DOWN">Điều chỉnh giảm</option>
          </select>
          <input className="admin-input" type="number" min={1} value={quantity} onChange={(event) => setQuantity(Number(event.target.value) || 1)} />
          <input className="admin-input" placeholder="Ghi chú" value={note} onChange={(event) => setNote(event.target.value)} />
          <button
            className="admin-btn"
            type="button"
            onClick={() => void handleSubmit()}
            disabled={saving || !variantId || items.length === 0}
          >
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
