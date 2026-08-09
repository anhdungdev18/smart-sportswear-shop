"use client";

import { memo, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiRequestError } from "@/modules/api/common";
import { cancelOrderByStaff, confirmManualRefund, fetchOrderDetail, fetchOrderRefunds, processCancellationRefund, refreshVnpayRefund, rejectCancellationRequest, updateOrderStatus } from "@/modules/orders/browser-api";
import type { OrderRefundResponse } from "@/modules/orders/browser-api";
import type { AdminOrderResponse, PageMeta } from "@/modules/orders/types";
import { fetchOrderShipment, updateOrderShipment } from "@/modules/shipping/browser-api";
import type { ShipmentResponse, ShippingMethodResponse } from "@/modules/shipping/types";

const orderStatuses = [
  "PENDING_CONFIRMATION",
  "CANCELLATION_REQUESTED",
  "CANCELLATION_APPROVED",
  "CONFIRMED",
  "PACKING",
  "SHIPPING",
  "DELIVERED",
  "CANCELLED"
] as const;

const nextOrderStatuses: Record<string, readonly string[]> = {
  PENDING_CONFIRMATION: ["CONFIRMED"], CANCELLATION_REQUESTED: [], CANCELLATION_APPROVED: [], CONFIRMED: ["PACKING"],
  PACKING: ["SHIPPING"], SHIPPING: ["DELIVERED"], DELIVERED: [], CANCELLED: []
};

const orderStatusLabels: Record<string, string> = {
  PENDING_CONFIRMATION: "Chờ xác nhận",
  CANCELLATION_REQUESTED: "Chờ xử lý hủy",
  CANCELLATION_APPROVED: "Đã duyệt hủy – đang hoàn tiền",
  CONFIRMED: "Đã xác nhận",
  PACKING: "Đang đóng gói",
  SHIPPING: "Đang giao",
  DELIVERED: "Đã giao",
  CANCELLED: "Đã hủy"
};

const shipmentStatuses = [
  "PENDING",
  "READY_TO_SHIP",
  "SHIPPING",
  "DELIVERED",
  "FAILED",
  "RETURNED",
  "CANCELLED"
] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

function createShipmentDraft(shipment?: ShipmentResponse | null) {
  return {
    shippingMethodId: shipment?.shippingMethodId ?? "",
    trackingNumber: shipment?.trackingNumber ?? "",
    carrierName: shipment?.carrierName ?? "",
    status: shipment?.status ?? "PENDING",
    note: shipment?.note ?? ""
  };
}

const OrderRow = memo(function OrderRow({
  order,
  shipment,
  shipmentDraft,
  detail,
  refunds,
  shippingMethods,
  statusDraft,
  noteDraft,
  savingId,
  onStatusChange,
  onNoteChange,
  onLoadDetail,
  onLoadShipment,
  onSaveStatus,
  onRefundCancellation,
  onRejectCancellation,
  onStaffCancel,
  onRefreshRefund,
  onManualConfirmRefund,
  onShipmentDraftChange,
  onSaveShipment
}: {
  order: AdminOrderResponse;
  shipment: ShipmentResponse | null | undefined;
  shipmentDraft: ReturnType<typeof createShipmentDraft> | undefined;
  detail: AdminOrderResponse | undefined;
  refunds: OrderRefundResponse[] | undefined;
  shippingMethods: ShippingMethodResponse[];
  statusDraft: string;
  noteDraft: string;
  savingId: string | null;
  onStatusChange: (value: string) => void;
  onNoteChange: (value: string) => void;
  onLoadDetail: () => void;
  onLoadShipment: () => void;
  onSaveStatus: () => void;
  onRefundCancellation: () => void;
  onRejectCancellation: () => void;
  onStaffCancel: () => void;
  onRefreshRefund: (refundId: string) => void;
  onManualConfirmRefund: (refund: OrderRefundResponse) => void;
  onShipmentDraftChange: (patch: Partial<ReturnType<typeof createShipmentDraft>>) => void;
  onSaveShipment: () => void;
}) {
  const activeRefund = refunds?.find((refund) => ["PENDING", "PROCESSING", "COMPLETED"].includes(refund.status));
  return (
    <tr>
      <td>
        <strong>{order.orderCode}</strong>
        <div className="table-subtle">{new Date(order.createdAt).toLocaleString("vi-VN")}</div>
      </td>
      <td>
        {order.customerName}
        <div className="table-subtle">{order.customerPhone ?? "Chưa có số điện thoại"}</div>
      </td>
      <td>
        {order.paymentMethod}
        <div className="table-subtle">{order.paymentStatus}</div>
      </td>
      <td>{Math.round(order.totalAmount).toLocaleString("vi-VN")}₫</td>
      <td>
        {["CANCELLATION_REQUESTED", "CANCELLATION_APPROVED"].includes(order.orderStatus) ? (
          <div className="admin-subcard admin-subcard-tight" style={{ borderColor: "#f0a32f", background: "#fff8e8", marginTop: 0 }}>
            <strong>{order.orderStatus === "CANCELLATION_APPROVED"
              ? "ĐÃ DUYỆT HỦY — ĐANG HOÀN TIỀN"
              : order.cancellationRequestedBy === "CUSTOMER" ? "KHÁCH HÀNG YÊU CẦU HỦY ĐƠN"
                : order.cancellationRequestedBy === "STAFF" ? "CỬA HÀNG CHỦ ĐỘNG HỦY ĐƠN" : "YÊU CẦU HỦY ĐƠN"}</strong>
            <div className="table-subtle">Lý do: {order.cancellationReason ?? "Không ghi lý do"}</div>
            {order.cancellationRequestedAt ? <div className="table-subtle">Gửi lúc: {new Date(order.cancellationRequestedAt).toLocaleString("vi-VN")}</div> : null}
            <div className="table-subtle">{order.paymentStatus === "PAID" ? "Đơn đã thanh toán — cần hoàn tiền trước khi hủy." : "Đơn chưa thanh toán — có thể hủy ngay."}</div>
          </div>
        ) : null}
        <div className="admin-inline-form wrap">
          {!['CANCELLATION_REQUESTED', 'CANCELLATION_APPROVED'].includes(order.orderStatus) ? <select className="select" value={statusDraft} onChange={(event) => onStatusChange(event.target.value)}>
            {[order.orderStatus, ...(nextOrderStatuses[order.orderStatus] ?? [])].map((status) => (
              <option value={status} key={status}>{orderStatusLabels[status] ?? status}</option>
            ))}
          </select> : null}
          <input className="admin-input" placeholder="Ghi chú nội bộ" value={noteDraft} onChange={(event) => onNoteChange(event.target.value)} />
          {!['CANCELLATION_REQUESTED', 'CANCELLATION_APPROVED'].includes(order.orderStatus) ? <button className="admin-btn" type="button" onClick={onSaveStatus} disabled={savingId === order.id || statusDraft === order.orderStatus}>
            {savingId === order.id ? "Đang lưu..." : "Lưu trạng thái"}
          </button> : null}
          {order.orderStatus === "PENDING_CONFIRMATION" ? (
            <button className="admin-btn secondary" type="button" onClick={onStaffCancel} disabled={savingId === `staff-cancel:${order.id}`}>
              {savingId === `staff-cancel:${order.id}` ? "Đang xử lý..." : "Cửa hàng hủy đơn"}
            </button>
          ) : null}
          <button className="admin-btn secondary" type="button" onClick={onLoadDetail} disabled={savingId === `detail:${order.id}`}>
            {savingId === `detail:${order.id}` ? "Đang tải..." : "Chi tiết"}
          </button>
          {["CANCELLATION_REQUESTED", "CANCELLATION_APPROVED"].includes(order.orderStatus) ? (
            <>
              <button className="admin-btn" type="button" onClick={onRefundCancellation} disabled={savingId === `refund:${order.id}` || activeRefund?.status === "PENDING" || activeRefund?.status === "PROCESSING"}>
                {savingId === `refund:${order.id}`
                  ? "Đang hoàn tiền..."
                  : activeRefund?.status === "PROCESSING" ? "VNPay đang xử lý"
                      : activeRefund?.status === "PENDING" ? "Hoàn tiền đang chờ"
                      : order.orderStatus === "CANCELLATION_APPROVED" ? "Thử lại hoàn tiền"
                        : "Duyệt: Hoàn tiền & hủy"}
              </button>
              {order.orderStatus === "CANCELLATION_REQUESTED" && order.cancellationRequestedBy === "CUSTOMER" ? <button className="admin-btn secondary" type="button" onClick={onRejectCancellation} disabled={savingId === `reject:${order.id}` || activeRefund?.status === "PENDING" || activeRefund?.status === "PROCESSING" || activeRefund?.status === "COMPLETED"}>
                {savingId === `reject:${order.id}` ? "Đang xử lý..." : "Từ chối yêu cầu"}
              </button> : null}
            </>
          ) : null}
          <button className="admin-btn secondary" type="button" onClick={onLoadShipment} disabled={savingId === `load-shipment:${order.id}`}>
            {savingId === `load-shipment:${order.id}` ? "Đang tải..." : "Giao vận"}
          </button>
        </div>

        {shipmentDraft ? (
          <div className="admin-subcard admin-subcard-tight">
            <div className="table-subtle">
              {shipment?.shipmentCode ? `Mã vận đơn: ${shipment.shipmentCode}` : "Chưa có shipment, sẽ tạo khi lưu lần đầu."}
            </div>
            <div className="admin-form-grid">
              <select className="select" value={shipmentDraft.shippingMethodId} onChange={(event) => onShipmentDraftChange({ shippingMethodId: event.target.value })}>
                <option value="">Chọn phương thức giao hàng</option>
                {shippingMethods.map((item) => (
                  <option value={item.id} key={item.id}>{item.name}</option>
                ))}
              </select>
              <select className="select" value={shipmentDraft.status} onChange={(event) => onShipmentDraftChange({ status: event.target.value })}>
                {shipmentStatuses.map((status) => (
                  <option value={status} key={status}>{status}</option>
                ))}
              </select>
              <input className="admin-input" placeholder="Đơn vị vận chuyển" value={shipmentDraft.carrierName} onChange={(event) => onShipmentDraftChange({ carrierName: event.target.value })} />
              <input className="admin-input" placeholder="Mã tracking" value={shipmentDraft.trackingNumber} onChange={(event) => onShipmentDraftChange({ trackingNumber: event.target.value })} />
              <div className="admin-form-full">
                <input className="admin-input" placeholder="Ghi chú giao vận" value={shipmentDraft.note} onChange={(event) => onShipmentDraftChange({ note: event.target.value })} />
              </div>
            </div>
            <button className="admin-btn secondary" type="button" onClick={onSaveShipment} disabled={savingId === `save-shipment:${order.id}`}>
              {savingId === `save-shipment:${order.id}` ? "Đang lưu..." : "Lưu giao vận"}
            </button>
          </div>
        ) : null}

        {detail ? (
          <div className="admin-subcard admin-subcard-tight">
            <strong>Chi tiết đơn</strong>
            <div className="table-subtle">Khách hàng: {detail.customerName} · {detail.customerPhone ?? "Chưa có số điện thoại"}</div>
            <div className="table-subtle">Ghi chú khách: {detail.note ?? "Không có"}</div>
            <div className="table-subtle">Ghi chú nội bộ: {detail.internalNote ?? "Không có"}</div>
            <div className="admin-stack">
              {detail.items.map((item) => (
                <div className="table-subtle" key={item.id}>
                  {item.productName} · {item.sku} · x{item.quantity} · {Math.round(item.lineTotal).toLocaleString("vi-VN")}₫
                </div>
              ))}
            </div>
          </div>
        ) : null}
        {refunds ? (
          <div className="admin-subcard admin-subcard-tight">
            <strong>Giao dịch hoàn tiền</strong>
            {refunds.length ? refunds.map((refund) => (
              <div className="admin-inline-form wrap" key={refund.id}>
                <span className="table-subtle">
                  {refund.refundCode} · {refund.provider} · {Math.round(refund.amount).toLocaleString("vi-VN")}₫ · <strong>{refund.status}</strong>
                  {refund.gatewayTransactionNo ? ` · GD: ${refund.gatewayTransactionNo}` : ""}
                </span>
                {["PENDING", "PROCESSING"].includes(refund.status) ? (
                  <button className="admin-btn secondary" type="button" onClick={() => onRefreshRefund(refund.id)} disabled={savingId === `refresh-refund:${refund.id}`}>
                    {savingId === `refresh-refund:${refund.id}` ? "Đang kiểm tra..." : "Kiểm tra VNPay"}
                  </button>
                ) : null}
                {!['COMPLETED', 'CANCELLED'].includes(refund.status) ? (
                  <button className="admin-btn secondary" type="button" onClick={() => onManualConfirmRefund(refund)} disabled={savingId === `manual-refund:${refund.id}`}>
                    {savingId === `manual-refund:${refund.id}` ? "Đang xác nhận..." : "Xác nhận đã hoàn thủ công"}
                  </button>
                ) : refund.manualReference ? <span className="table-subtle">Biên nhận: {refund.manualReference}</span> : null}
              </div>
            )) : <div className="table-subtle">Chưa có giao dịch hoàn tiền.</div>}
          </div>
        ) : null}
      </td>
    </tr>
  );
});

function visiblePages(currentPage: number, totalPages: number) {
  const count = Math.min(5, totalPages);
  const start = Math.max(1, Math.min(currentPage - 2, totalPages - count + 1));
  return Array.from({ length: count }, (_, index) => start + index);
}

export function AdminOrdersClient({
  initialOrders,
  shippingMethods,
  pageMeta,
  initialKeyword,
  initialStatus,
  loadError
}: {
  initialOrders: AdminOrderResponse[];
  shippingMethods: ShippingMethodResponse[];
  pageMeta: PageMeta;
  initialKeyword: string;
  initialStatus: string;
  loadError: string | null;
}) {
  const router = useRouter();
  const [orders, setOrders] = useState(initialOrders);
  const [searchTerm, setSearchTerm] = useState(initialKeyword);
  const [statusFilter, setStatusFilter] = useState<"all" | (typeof orderStatuses)[number]>(initialStatus as "all" | (typeof orderStatuses)[number]);
  const [statusDrafts, setStatusDrafts] = useState<Record<string, string>>(Object.fromEntries(initialOrders.map((item) => [item.id, item.orderStatus])));
  const [noteDrafts, setNoteDrafts] = useState<Record<string, string>>({});
  const [shipmentDrafts, setShipmentDrafts] = useState<Record<string, ReturnType<typeof createShipmentDraft>>>({});
  const [shipments, setShipments] = useState<Record<string, ShipmentResponse | null>>({});
  const [orderDetails, setOrderDetails] = useState<Record<string, AdminOrderResponse>>({});
  const [refundsByOrder, setRefundsByOrder] = useState<Record<string, OrderRefundResponse[]>>({});
  const [message, setMessage] = useState<string | null>(null);
  const [savingId, setSavingId] = useState<string | null>(null);
  const orderDetailCacheRef = useRef<Record<string, AdminOrderResponse>>({});
  const shipmentCacheRef = useRef<Record<string, ShipmentResponse | null>>({});
  const pendingCancellationCount = orders.filter((order) => order.orderStatus === "CANCELLATION_REQUESTED").length;

  function navigate(nextPage: number, keyword = searchTerm, status = statusFilter) {
    const params = new URLSearchParams();
    if (nextPage > 1) params.set("page", String(nextPage));
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (status !== "all") params.set("status", status);
    router.replace(`/orders${params.size ? `?${params.toString()}` : ""}`);
  }

  useEffect(() => {
    if (searchTerm === initialKeyword) return;
    const timer = window.setTimeout(() => navigate(1, searchTerm, statusFilter), 400);
    return () => window.clearTimeout(timer);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchTerm]);

  useEffect(() => {
    const cancellationOrders = initialOrders.filter((order) =>
      ["CANCELLATION_REQUESTED", "CANCELLATION_APPROVED"].includes(order.orderStatus));
    if (!cancellationOrders.length) return;
    void Promise.all(cancellationOrders.map(async (order) => {
      const refunds = await fetchOrderRefunds(order.id);
      setRefundsByOrder((current) => ({ ...current, [order.id]: refunds }));
    })).catch(() => undefined);
  }, [initialOrders]);

  async function handleUpdate(id: string) {
    const currentOrder = orders.find((item) => item.id === id);
    if (!currentOrder || (statusDrafts[id] ?? currentOrder.orderStatus) === currentOrder.orderStatus) {
      setMessage("Hãy chọn trạng thái tiếp theo trước khi lưu.");
      return;
    }
    try {
      setSavingId(id);
      setMessage(null);
      const updated = await updateOrderStatus(id, { status: statusDrafts[id] ?? "PENDING_CONFIRMATION", note: noteDrafts[id]?.trim() || undefined });
      orderDetailCacheRef.current[id] = updated;
      setOrders((current) => current.map((item) => (item.id === id ? updated : item)));
      setOrderDetails((current) => ({ ...current, [id]: updated }));
      setMessage(`Đã cập nhật trạng thái đơn ${updated.orderCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được trạng thái đơn hàng"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleLoadShipment(id: string) {
    if (Object.prototype.hasOwnProperty.call(shipmentCacheRef.current, id)) {
      const cached = shipmentCacheRef.current[id];
      setShipments((current) => ({ ...current, [id]: cached }));
      setShipmentDrafts((current) => ({ ...current, [id]: createShipmentDraft(cached) }));
      return;
    }

    try {
      setSavingId(`load-shipment:${id}`);
      setMessage(null);
      const shipment = await fetchOrderShipment(id);
      shipmentCacheRef.current[id] = shipment;
      setShipments((current) => ({ ...current, [id]: shipment }));
      setShipmentDrafts((current) => ({ ...current, [id]: createShipmentDraft(shipment) }));
    } catch (error) {
      shipmentCacheRef.current[id] = null;
      setMessage(extractError(error, "Chưa lấy được thông tin giao vận"));
      setShipments((current) => ({ ...current, [id]: null }));
      setShipmentDrafts((current) => ({ ...current, [id]: createShipmentDraft(null) }));
    } finally {
      setSavingId(null);
    }
  }

  async function handleRefundCancellation(id: string) {
    const order = orders.find((item) => item.id === id);
    if (!order || !window.confirm(`Hoàn ${Math.round(order.totalAmount).toLocaleString("vi-VN")}₫ qua VNPay và hủy đơn ${order.orderCode}?`)) return;
    try {
      setSavingId(`refund:${id}`);
      setMessage(null);
      const refund = await processCancellationRefund(id);
      const updated = await fetchOrderDetail(id);
      orderDetailCacheRef.current[id] = updated;
      setOrders((current) => current.map((item) => item.id === id ? updated : item));
      setOrderDetails((current) => ({ ...current, [id]: updated }));
      setRefundsByOrder((current) => ({
        ...current,
        [id]: [refund, ...(current[id] ?? []).filter((item) => item.id !== refund.id)]
      }));
      setStatusDrafts((current) => ({ ...current, [id]: updated.orderStatus }));
      setMessage(refund.status === "COMPLETED"
        ? `Đã hoàn tiền ${refund.refundCode} và hủy đơn ${updated.orderCode}.`
        : `Đã gửi giao dịch ${refund.refundCode}; trạng thái hiện tại: ${refund.status}.`);
    } catch (error) {
      setMessage(extractError(error, "Không xử lý được hoàn tiền hủy đơn"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleRejectCancellation(id: string) {
    const order = orders.find((item) => item.id === id);
    if (!order || !window.confirm(`Từ chối yêu cầu hủy đơn ${order.orderCode}?`)) return;
    try {
      setSavingId(`reject:${id}`);
      setMessage(null);
      const updated = await rejectCancellationRequest(id,
        noteDrafts[id]?.trim() || "Từ chối yêu cầu hủy của khách hàng");
      orderDetailCacheRef.current[id] = updated;
      setOrders((current) => current.map((item) => item.id === id ? updated : item));
      setOrderDetails((current) => ({ ...current, [id]: updated }));
      setStatusDrafts((current) => ({ ...current, [id]: updated.orderStatus }));
      setMessage(`Đã từ chối yêu cầu hủy đơn ${updated.orderCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không từ chối được yêu cầu hủy"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleStaffCancel(id: string) {
    const order = orders.find((item) => item.id === id);
    if (!order) return;
    const reason = noteDrafts[id]?.trim();
    if (!reason) {
      setMessage("Hãy nhập lý do cửa hàng hủy đơn vào ô ghi chú trước khi thực hiện.");
      return;
    }
    if (!window.confirm(`Cửa hàng chủ động hủy đơn ${order.orderCode}?`)) return;
    try {
      setSavingId(`staff-cancel:${id}`);
      setMessage(null);
      const updated = await cancelOrderByStaff(id, reason);
      orderDetailCacheRef.current[id] = updated;
      setOrders((current) => current.map((item) => item.id === id ? updated : item));
      setOrderDetails((current) => ({ ...current, [id]: updated }));
      setStatusDrafts((current) => ({ ...current, [id]: updated.orderStatus }));
      setMessage(updated.orderStatus === "CANCELLATION_REQUESTED"
        ? `Đã ghi nhận cửa hàng hủy đơn ${updated.orderCode}. Hãy duyệt hoàn tiền để hoàn tất.`
        : `Cửa hàng đã hủy đơn ${updated.orderCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không hủy được đơn hàng"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleRefreshRefund(orderId: string, refundId: string) {
    try {
      setSavingId(`refresh-refund:${refundId}`);
      setMessage(null);
      const refund = await refreshVnpayRefund(refundId);
      const updated = await fetchOrderDetail(orderId);
      setRefundsByOrder((current) => ({
        ...current,
        [orderId]: [refund, ...(current[orderId] ?? []).filter((item) => item.id !== refund.id)]
      }));
      setOrders((current) => current.map((item) => item.id === orderId ? updated : item));
      setOrderDetails((current) => ({ ...current, [orderId]: updated }));
      setStatusDrafts((current) => ({ ...current, [orderId]: updated.orderStatus }));
      setMessage(refund.status === "COMPLETED"
        ? `VNPay xác nhận đã hoàn tiền ${refund.refundCode}; đơn đã được hủy.`
        : `Trạng thái hoàn tiền ${refund.refundCode}: ${refund.status}.`);
    } catch (error) {
      setMessage(extractError(error, "Không kiểm tra được trạng thái hoàn tiền VNPay"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleManualConfirmRefund(orderId: string, refund: OrderRefundResponse) {
    const reference = window.prompt("Nhập mã giao dịch chuyển khoản/biên nhận hoàn tiền:")?.trim();
    if (!reference) { setMessage("Cần nhập mã giao dịch hoặc biên nhận để xác nhận hoàn tiền."); return; }
    const note = window.prompt("Ghi chú hoàn tiền (không bắt buộc):")?.trim();
    if (!window.confirm(`Xác nhận đã thực sự hoàn ${Math.round(refund.amount).toLocaleString("vi-VN")}₫ cho khách?`)) return;
    try {
      setSavingId(`manual-refund:${refund.id}`); setMessage(null);
      const updatedRefund = await confirmManualRefund(refund.id, reference, note);
      const updatedOrder = await fetchOrderDetail(orderId);
      setRefundsByOrder((current) => ({
        ...current,
        [orderId]: [updatedRefund, ...(current[orderId] ?? []).filter((item) => item.id !== updatedRefund.id)]
      }));
      setOrders((current) => current.map((item) => item.id === orderId ? updatedOrder : item));
      setOrderDetails((current) => ({ ...current, [orderId]: updatedOrder }));
      setStatusDrafts((current) => ({ ...current, [orderId]: updatedOrder.orderStatus }));
      setMessage(`Đã xác nhận hoàn tiền thủ công ${updatedRefund.refundCode}; đơn đã được hủy.`);
    } catch (error) { setMessage(extractError(error, "Không xác nhận được hoàn tiền thủ công")); }
    finally { setSavingId(null); }
  }

  async function handleLoadDetail(id: string) {
    const cached = orderDetailCacheRef.current[id];
    if (cached) {
      setOrderDetails((current) => ({ ...current, [id]: cached }));
      return;
    }

    try {
      setSavingId(`detail:${id}`);
      setMessage(null);
      const [detail, refunds] = await Promise.all([fetchOrderDetail(id), fetchOrderRefunds(id)]);
      orderDetailCacheRef.current[id] = detail;
      setOrderDetails((current) => ({ ...current, [id]: detail }));
      setOrders((current) => current.map((item) => (item.id === detail.id ? detail : item)));
      setRefundsByOrder((current) => ({ ...current, [id]: refunds }));
    } catch (error) {
      setMessage(extractError(error, "Không tải được chi tiết đơn hàng"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleSaveShipment(id: string) {
    const draft = shipmentDrafts[id];
    if (!draft) return;

    try {
      setSavingId(`save-shipment:${id}`);
      setMessage(null);
      const shipment = await updateOrderShipment(id, {
        shippingMethodId: draft.shippingMethodId || null,
        trackingNumber: draft.trackingNumber || null,
        carrierName: draft.carrierName || null,
        status: draft.status || null,
        note: draft.note || null
      });
      shipmentCacheRef.current[id] = shipment;
      setShipments((current) => ({ ...current, [id]: shipment }));
      setShipmentDrafts((current) => ({ ...current, [id]: createShipmentDraft(shipment) }));
      setMessage(`Đã cập nhật giao vận cho đơn ${orders.find((item) => item.id === id)?.orderCode ?? ""}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được giao vận"));
    } finally {
      setSavingId(null);
    }
  }

  return (
    <section className="card panel">
      <div className="panel-header">
        <div>
          <h2>Danh sách đơn hàng</h2>
          {pendingCancellationCount > 0 ? (
            <p style={{ marginTop: 8, color: "#b45309", fontWeight: 700 }}>
              Có {pendingCancellationCount} yêu cầu hủy đang chờ xử lý trên trang này.
            </p>
          ) : null}
        </div>
      </div>
      {message ? <p className="action-message">{message}</p> : null}
      {loadError ? <p className="action-message" role="alert">{loadError}</p> : null}
      <div className="admin-form-grid" style={{ marginBottom: 16 }}>
        <input className="admin-input" placeholder="Tìm theo mã đơn, tên khách, số điện thoại hoặc thanh toán" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} />
        <select className="select" value={statusFilter} onChange={(event) => {
          const status = event.target.value as "all" | (typeof orderStatuses)[number];
          setStatusFilter(status);
          navigate(1, searchTerm, status);
        }}>
          <option value="all">Tất cả trạng thái</option>
          {orderStatuses.map((status) => <option value={status} key={status}>{orderStatusLabels[status] ?? status}</option>)}
        </select>
      </div>
      {!loadError && orders.length === 0 ? (
        <div className="empty-state">Không có đơn hàng nào khớp bộ lọc hiện tại.</div>
      ) : orders.length > 0 ? (
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Khách hàng</th>
              <th>Thanh toán</th>
              <th>Giá trị</th>
              <th>Cập nhật</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <OrderRow
                key={order.id}
                order={order}
                shipment={shipments[order.id]}
                shipmentDraft={shipmentDrafts[order.id]}
                detail={orderDetails[order.id]}
                refunds={refundsByOrder[order.id]}
                shippingMethods={shippingMethods}
                statusDraft={statusDrafts[order.id] ?? order.orderStatus}
                noteDraft={noteDrafts[order.id] ?? ""}
                savingId={savingId}
                onStatusChange={(value) => setStatusDrafts((current) => ({ ...current, [order.id]: value }))}
                onNoteChange={(value) => setNoteDrafts((current) => ({ ...current, [order.id]: value }))}
                onLoadDetail={() => void handleLoadDetail(order.id)}
                onLoadShipment={() => void handleLoadShipment(order.id)}
                onSaveStatus={() => void handleUpdate(order.id)}
                onRefundCancellation={() => void handleRefundCancellation(order.id)}
                onRejectCancellation={() => void handleRejectCancellation(order.id)}
                onStaffCancel={() => void handleStaffCancel(order.id)}
                onRefreshRefund={(refundId) => void handleRefreshRefund(order.id, refundId)}
                onManualConfirmRefund={(refund) => void handleManualConfirmRefund(order.id, refund)}
                onShipmentDraftChange={(patch) => setShipmentDrafts((current) => ({ ...current, [order.id]: { ...current[order.id], ...patch } }))}
                onSaveShipment={() => void handleSaveShipment(order.id)}
              />
            ))}
          </tbody>
        </table>
      ) : null}
      {pageMeta.totalPages > 0 ? (
        <div className="admin-pager">
          <div className="admin-pager-info">
            {(pageMeta.page - 1) * pageMeta.limit + 1}–{Math.min(pageMeta.page * pageMeta.limit, pageMeta.total)} / {pageMeta.total.toLocaleString("vi-VN")} đơn hàng
          </div>
          <div className="admin-pager-controls" aria-label="Phân trang đơn hàng">
            <button className="admin-pager-btn" type="button" disabled={pageMeta.page <= 1} onClick={() => navigate(pageMeta.page - 1)}>Trước</button>
            {visiblePages(pageMeta.page, pageMeta.totalPages).map((page) => (
              <button
                className={`admin-pager-btn admin-pager-number${page === pageMeta.page ? " active" : ""}`}
                type="button"
                aria-current={page === pageMeta.page ? "page" : undefined}
                onClick={() => navigate(page)}
                key={page}
              >
                {page}
              </button>
            ))}
            <button className="admin-pager-btn" type="button" disabled={pageMeta.page >= pageMeta.totalPages} onClick={() => navigate(pageMeta.page + 1)}>Sau</button>
          </div>
        </div>
      ) : null}
    </section>
  );
}
