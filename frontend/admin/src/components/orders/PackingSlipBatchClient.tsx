"use client";

import { useEffect, useState } from "react";
import { fetchPackingSlipBatch, updateOrderStatus } from "@/modules/orders/browser-api";
import type { AdminOrderResponse } from "@/modules/orders/types";
import { ApiRequestError } from "@/modules/api/common";
import { PACKING_SLIP_STYLES, PackingSlipContent } from "@/components/orders/packingSlipShared";

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }
  return fallback;
}

/**
 * Printing here has no side effect - it just renders each order's slip.
 * Advancing an order to PACKING is a separate, explicit action ("Đã in
 * xong...") taken after staff have actually printed, rather than happening
 * automatically when the print tab opens - window.open() can be blocked by
 * the browser and the print dialog can be cancelled, so tying a real state
 * change to either of those would risk marking an order "packed" when
 * nothing was actually printed.
 */
function readTransferredOrders(payloadKey?: string) {
  if (!payloadKey?.startsWith("packing-slip:")) return null;
  try {
    const payload = window.localStorage.getItem(payloadKey);
    window.localStorage.removeItem(payloadKey);
    if (!payload) return null;
    const parsed = JSON.parse(payload) as unknown;
    return Array.isArray(parsed) ? parsed as AdminOrderResponse[] : null;
  } catch {
    return null;
  }
}

export function PackingSlipBatchClient({ orderIds, payloadKey }: { orderIds: string[]; payloadKey?: string }) {
  const [orders, setOrders] = useState<AdminOrderResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [confirming, setConfirming] = useState(false);
  const [resultMessage, setResultMessage] = useState<string | null>(null);

  useEffect(() => {
    const transferredOrders = readTransferredOrders(payloadKey);
    if (transferredOrders) {
      setOrders(transferredOrders);
      return;
    }
    if (orderIds.length === 0) {
      setError("Không có đơn hàng nào được chọn.");
      return;
    }
    let cancelled = false;
    fetchPackingSlipBatch(orderIds)
      .then((data) => {
        if (!cancelled) setOrders(data);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(extractError(err, "Không tải được danh sách đơn hàng."));
      });
    return () => {
      cancelled = true;
    };
  }, [orderIds, payloadKey]);

  if (error) {
    return <div className="packing-slip-status">{error}</div>;
  }
  if (!orders) {
    return <div className="packing-slip-status">Đang tải...</div>;
  }

  const confirmedIds = orders.filter((order) => order.orderStatus === "CONFIRMED").map((order) => order.id);

  async function handleConfirmPacked() {
    if (confirmedIds.length === 0) return;
    if (!window.confirm(`Xác nhận đã in xong và chuyển ${confirmedIds.length} đơn sang "Đang đóng gói"?`)) return;
    setConfirming(true);
    setResultMessage(null);
    try {
      const results = await Promise.allSettled(
        confirmedIds.map((id) => updateOrderStatus(id, { status: "PACKING" }))
      );
      const succeeded: AdminOrderResponse[] = [];
      results.forEach((result) => {
        if (result.status === "fulfilled") succeeded.push(result.value);
      });
      setOrders((current) => current?.map((order) => succeeded.find((s) => s.id === order.id) ?? order) ?? current);
      const failedCount = confirmedIds.length - succeeded.length;
      setResultMessage(failedCount > 0
        ? `Đã chuyển ${succeeded.length} đơn sang đóng gói; ${failedCount} đơn thất bại (có thể đã bị thay đổi trạng thái).`
        : `Đã chuyển ${succeeded.length} đơn sang Đang đóng gói.`);
    } finally {
      setConfirming(false);
    }
  }

  return (
    <>
      <style>{PACKING_SLIP_STYLES}</style>
      <div className="ps-toolbar no-print">
        {resultMessage ? <span className="ps-toolbar-message">{resultMessage}</span> : null}
        {confirmedIds.length > 0 ? (
          <button type="button" onClick={() => void handleConfirmPacked()} disabled={confirming}>
            {confirming ? "Đang xử lý..." : `Đã in xong — chuyển ${confirmedIds.length} đơn sang đóng gói`}
          </button>
        ) : null}
        <button type="button" onClick={() => window.print()}>In tất cả ({orders.length} đơn)</button>
      </div>
      {orders.map((order, index) => (
        <PackingSlipContent key={order.id} order={order} pageBreakBefore={index > 0} />
      ))}
    </>
  );
}
