"use client";

import { useEffect, useState } from "react";
import { fetchOrderDetail } from "@/modules/orders/browser-api";
import type { AdminOrderResponse } from "@/modules/orders/types";
import { ApiRequestError } from "@/modules/api/common";
import { PACKING_SLIP_STYLES, PackingSlipContent } from "@/components/orders/packingSlipShared";

export function PackingSlipBatchClient({ orderIds }: { orderIds: string[] }) {
  const [orders, setOrders] = useState<AdminOrderResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (orderIds.length === 0) {
      setError("Không có đơn hàng nào được chọn.");
      return;
    }
    let cancelled = false;
    Promise.all(orderIds.map((id) => fetchOrderDetail(id)))
      .then((data) => {
        if (!cancelled) setOrders(data);
      })
      .catch((err) => {
        if (cancelled) return;
        const payload = err instanceof ApiRequestError ? (err.payload as { message?: string } | null) : null;
        setError(payload?.message ?? "Không tải được danh sách đơn hàng.");
      });
    return () => {
      cancelled = true;
    };
    // orderIds comes from a fresh array each render of the server parent, but
    // its content is only ever set once per page load (from the URL), so
    // re-running this on identity change is harmless.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderIds.join(",")]);

  if (error) {
    return <div className="packing-slip-status">{error}</div>;
  }
  if (!orders) {
    return <div className="packing-slip-status">Đang tải...</div>;
  }

  return (
    <>
      <style>{PACKING_SLIP_STYLES}</style>
      <div className="ps-toolbar no-print">
        <button type="button" onClick={() => window.print()}>In tất cả ({orders.length} đơn)</button>
      </div>
      {orders.map((order, index) => (
        <PackingSlipContent key={order.id} order={order} pageBreakBefore={index > 0} />
      ))}
    </>
  );
}
