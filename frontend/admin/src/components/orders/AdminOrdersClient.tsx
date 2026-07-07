"use client";

import { memo, useDeferredValue, useMemo, useRef, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { fetchOrderDetail, updateOrderStatus } from "@/modules/orders/browser-api";
import type { AdminOrderResponse } from "@/modules/orders/types";
import { fetchOrderShipment, updateOrderShipment } from "@/modules/shipping/browser-api";
import type { ShipmentResponse, ShippingMethodResponse } from "@/modules/shipping/types";

const orderStatuses = [
  "PENDING_CONFIRMATION",
  "CONFIRMED",
  "PACKING",
  "SHIPPING",
  "DELIVERED",
  "CANCELLED"
] as const;

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
  shippingMethods,
  statusDraft,
  noteDraft,
  savingId,
  onStatusChange,
  onNoteChange,
  onLoadDetail,
  onLoadShipment,
  onSaveStatus,
  onShipmentDraftChange,
  onSaveShipment
}: {
  order: AdminOrderResponse;
  shipment: ShipmentResponse | null | undefined;
  shipmentDraft: ReturnType<typeof createShipmentDraft> | undefined;
  detail: AdminOrderResponse | undefined;
  shippingMethods: ShippingMethodResponse[];
  statusDraft: string;
  noteDraft: string;
  savingId: string | null;
  onStatusChange: (value: string) => void;
  onNoteChange: (value: string) => void;
  onLoadDetail: () => void;
  onLoadShipment: () => void;
  onSaveStatus: () => void;
  onShipmentDraftChange: (patch: Partial<ReturnType<typeof createShipmentDraft>>) => void;
  onSaveShipment: () => void;
}) {
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
        <div className="admin-inline-form wrap">
          <select className="select" value={statusDraft} onChange={(event) => onStatusChange(event.target.value)}>
            {orderStatuses.map((status) => (
              <option value={status} key={status}>{status}</option>
            ))}
          </select>
          <input className="admin-input" placeholder="Ghi chú nội bộ" value={noteDraft} onChange={(event) => onNoteChange(event.target.value)} />
          <button className="admin-btn" type="button" onClick={onSaveStatus} disabled={savingId === order.id}>
            {savingId === order.id ? "Đang lưu..." : "Lưu trạng thái"}
          </button>
          <button className="admin-btn secondary" type="button" onClick={onLoadDetail} disabled={savingId === `detail:${order.id}`}>
            {savingId === `detail:${order.id}` ? "Đang tải..." : "Chi tiết"}
          </button>
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
      </td>
    </tr>
  );
});

export function AdminOrdersClient({ initialOrders, shippingMethods }: { initialOrders: AdminOrderResponse[]; shippingMethods: ShippingMethodResponse[]; }) {
  const [orders, setOrders] = useState(initialOrders);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | (typeof orderStatuses)[number]>("all");
  const deferredSearchTerm = useDeferredValue(searchTerm);
  const [statusDrafts, setStatusDrafts] = useState<Record<string, string>>(Object.fromEntries(initialOrders.map((item) => [item.id, item.orderStatus])));
  const [noteDrafts, setNoteDrafts] = useState<Record<string, string>>({});
  const [shipmentDrafts, setShipmentDrafts] = useState<Record<string, ReturnType<typeof createShipmentDraft>>>({});
  const [shipments, setShipments] = useState<Record<string, ShipmentResponse | null>>({});
  const [orderDetails, setOrderDetails] = useState<Record<string, AdminOrderResponse>>({});
  const [message, setMessage] = useState<string | null>(null);
  const [savingId, setSavingId] = useState<string | null>(null);
  const orderDetailCacheRef = useRef<Record<string, AdminOrderResponse>>({});
  const shipmentCacheRef = useRef<Record<string, ShipmentResponse | null>>({});

  const filteredOrders = useMemo(() => {
    const keyword = deferredSearchTerm.trim().toLowerCase();
    return orders.filter((order) => {
      const matchesStatus = statusFilter === "all" ? true : order.orderStatus === statusFilter;
      if (!matchesStatus) return false;
      if (!keyword) return true;
      const haystack = [order.orderCode, order.customerName, order.customerPhone ?? "", order.paymentMethod, order.paymentStatus].join(" ").toLowerCase();
      return haystack.includes(keyword);
    });
  }, [deferredSearchTerm, orders, statusFilter]);

  async function handleUpdate(id: string) {
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

  async function handleLoadDetail(id: string) {
    const cached = orderDetailCacheRef.current[id];
    if (cached) {
      setOrderDetails((current) => ({ ...current, [id]: cached }));
      return;
    }

    try {
      setSavingId(`detail:${id}`);
      setMessage(null);
      const detail = await fetchOrderDetail(id);
      orderDetailCacheRef.current[id] = detail;
      setOrderDetails((current) => ({ ...current, [id]: detail }));
      setOrders((current) => current.map((item) => (item.id === detail.id ? detail : item)));
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
        <h2>Danh sách đơn hàng</h2>
      </div>
      {message ? <p className="action-message">{message}</p> : null}
      <div className="admin-form-grid" style={{ marginBottom: 16 }}>
        <input className="admin-input" placeholder="Tìm theo mã đơn, tên khách, số điện thoại hoặc thanh toán" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} />
        <select className="select" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as "all" | (typeof orderStatuses)[number])}>
          <option value="all">Tất cả trạng thái</option>
          {orderStatuses.map((status) => <option value={status} key={status}>{status}</option>)}
        </select>
      </div>
      {filteredOrders.length === 0 ? (
        <div className="empty-state">{orders.length === 0 ? "Hiện chưa có đơn hàng nào." : "Không có đơn hàng nào khớp bộ lọc hiện tại."}</div>
      ) : (
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
            {filteredOrders.map((order) => (
              <OrderRow
                key={order.id}
                order={order}
                shipment={shipments[order.id]}
                shipmentDraft={shipmentDrafts[order.id]}
                detail={orderDetails[order.id]}
                shippingMethods={shippingMethods}
                statusDraft={statusDrafts[order.id] ?? order.orderStatus}
                noteDraft={noteDrafts[order.id] ?? ""}
                savingId={savingId}
                onStatusChange={(value) => setStatusDrafts((current) => ({ ...current, [order.id]: value }))}
                onNoteChange={(value) => setNoteDrafts((current) => ({ ...current, [order.id]: value }))}
                onLoadDetail={() => void handleLoadDetail(order.id)}
                onLoadShipment={() => void handleLoadShipment(order.id)}
                onSaveStatus={() => void handleUpdate(order.id)}
                onShipmentDraftChange={(patch) => setShipmentDrafts((current) => ({ ...current, [order.id]: { ...current[order.id], ...patch } }))}
                onSaveShipment={() => void handleSaveShipment(order.id)}
              />
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
