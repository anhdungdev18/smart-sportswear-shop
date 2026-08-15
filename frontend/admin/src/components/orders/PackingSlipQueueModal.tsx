"use client";

import { useEffect, useState } from "react";
import { listAdminOrdersPage } from "@/modules/orders/browser-api";
import type { AdminOrderResponse } from "@/modules/orders/types";
import { ApiRequestError } from "@/modules/api/common";

const QUEUE_LIMIT = 100;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }
  return fallback;
}

/**
 * Lists every CONFIRMED order (not just the ones on the current page/filter
 * of the main order table - that's the whole point of pulling this into its
 * own queue) so staff can print a batch of packing slips in one go. Printing
 * itself has no side effect here; advancing the order to PACKING happens as
 * its own explicit step on the print page after staff have actually printed,
 * not automatically when this modal opens the print tab.
 */
export function PackingSlipQueueModal({ onClose }: { onClose: () => void }) {
  const [orders, setOrders] = useState<AdminOrderResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [total, setTotal] = useState(0);

  useEffect(() => {
    let cancelled = false;
    listAdminOrdersPage(1, QUEUE_LIMIT, undefined, "CONFIRMED")
      .then((page) => {
        if (cancelled) return;
        setOrders(page.items);
        setTotal(page.meta.total || page.items.length);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(extractError(err, "Không tải được danh sách đơn chờ đóng gói"));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function toggle(id: string) {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  }

  function toggleAll() {
    if (!orders) return;
    setSelectedIds((current) =>
      current.size === orders.length ? new Set() : new Set(orders.map((order) => order.id))
    );
  }

  function handlePrint() {
    if (selectedIds.size === 0) return;
    window.open(`/orders/packing-slip-batch?ids=${Array.from(selectedIds).join(",")}`, "_blank");
    onClose();
  }

  return (
    <div className="admin-modal-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
        <div className="admin-modal-header">
          <h3>Đơn hàng chờ đóng gói {orders ? `(${orders.length})` : ""}</h3>
          <button className="admin-btn secondary" type="button" onClick={onClose}>Đóng</button>
        </div>

        {error ? (
          <p className="action-message" role="alert">{error}</p>
        ) : !orders ? (
          <p>Đang tải...</p>
        ) : orders.length === 0 ? (
          <p>Không có đơn nào đang ở trạng thái Đã xác nhận.</p>
        ) : (
          <>
            {total > orders.length ? (
              <p className="table-subtle">Chỉ hiển thị {orders.length} / {total} đơn đầu tiên.</p>
            ) : null}
            <label className="admin-modal-select-all">
              <input type="checkbox" checked={selectedIds.size === orders.length} onChange={toggleAll} />
              Chọn tất cả
            </label>
            <div className="admin-modal-list">
              {orders.map((order) => (
                <label className="admin-modal-row" key={order.id}>
                  <input type="checkbox" checked={selectedIds.has(order.id)} onChange={() => toggle(order.id)} />
                  <span className="admin-modal-row-code">{order.orderCode}</span>
                  <span>{order.customerName}</span>
                  <span>{Math.round(order.totalAmount).toLocaleString("vi-VN")}₫</span>
                </label>
              ))}
            </div>
          </>
        )}

        <div className="admin-modal-footer">
          <button className="admin-btn secondary" type="button" onClick={onClose}>Hủy</button>
          <button className="admin-btn" type="button" onClick={handlePrint} disabled={selectedIds.size === 0}>
            In phiếu đã chọn ({selectedIds.size})
          </button>
        </div>
      </div>
    </div>
  );
}
