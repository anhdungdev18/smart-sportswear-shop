"use client";

import { useEffect, useState } from "react";
import { fetchOrderDetail } from "@/modules/orders/browser-api";
import type { AdminOrderResponse } from "@/modules/orders/types";
import { ApiRequestError } from "@/modules/api/common";
import { PACKING_SLIP_STYLES, PackingSlipContent } from "@/components/orders/packingSlipShared";

export function PackingSlipClient({ orderId }: { orderId: string }) {
  const [order, setOrder] = useState<AdminOrderResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchOrderDetail(orderId)
      .then((data) => {
        if (!cancelled) setOrder(data);
      })
      .catch((err) => {
        if (cancelled) return;
        const payload = err instanceof ApiRequestError ? (err.payload as { message?: string } | null) : null;
        setError(payload?.message ?? "Không tải được đơn hàng");
      });
    return () => {
      cancelled = true;
    };
  }, [orderId]);

  if (error) {
    return <div className="packing-slip-status">{error}</div>;
  }
  if (!order) {
    return <div className="packing-slip-status">Đang tải...</div>;
  }

  return (
    <>
      <style>{PACKING_SLIP_STYLES}</style>
      <div className="ps-toolbar no-print">
        <button type="button" onClick={() => window.print()}>In phiếu</button>
      </div>
      <PackingSlipContent order={order} />
    </>
  );
}
