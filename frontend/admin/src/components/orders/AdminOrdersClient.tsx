"use client";

import { useState } from "react";
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

export function AdminOrdersClient({
  initialOrders,
  shippingMethods
}: {
  initialOrders: AdminOrderResponse[];
  shippingMethods: ShippingMethodResponse[];
}) {
  const [orders, setOrders] = useState(initialOrders);
  const [statusDrafts, setStatusDrafts] = useState<Record<string, string>>(
    Object.fromEntries(initialOrders.map((item) => [item.id, item.orderStatus]))
  );
  const [noteDrafts, setNoteDrafts] = useState<Record<string, string>>({});
  const [shipmentDrafts, setShipmentDrafts] = useState<Record<string, ReturnType<typeof createShipmentDraft>>>({});
  const [shipments, setShipments] = useState<Record<string, ShipmentResponse | null>>({});
  const [orderDetails, setOrderDetails] = useState<Record<string, AdminOrderResponse>>({});
  const [message, setMessage] = useState<string | null>(null);
  const [savingId, setSavingId] = useState<string | null>(null);

  async function handleUpdate(id: string) {
    try {
      setSavingId(id);
      setMessage(null);
      const updated = await updateOrderStatus(id, {
        status: statusDrafts[id] ?? "PENDING_CONFIRMATION",
        note: noteDrafts[id]?.trim() || undefined
      });
      setOrders((current) => current.map((item) => (item.id === id ? updated : item)));
      setMessage(`Đã cập nhật trạng thái đơn ${updated.orderCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được trạng thái đơn hàng"));
    } finally {
      setSavingId(null);
    }
  }

  async function handleLoadShipment(id: string) {
    try {
      setSavingId(`load-shipment:${id}`);
      setMessage(null);
      const shipment = await fetchOrderShipment(id);
      setShipments((current) => ({ ...current, [id]: shipment }));
      setShipmentDrafts((current) => ({ ...current, [id]: createShipmentDraft(shipment) }));
    } catch (error) {
      setMessage(extractError(error, "Chưa lấy được thông tin giao vận"));
      setShipments((current) => ({ ...current, [id]: null }));
      setShipmentDrafts((current) => ({ ...current, [id]: createShipmentDraft(null) }));
    } finally {
      setSavingId(null);
    }
  }

  async function handleLoadDetail(id: string) {
    try {
      setSavingId(`detail:${id}`);
      setMessage(null);
      const detail = await fetchOrderDetail(id);
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
    if (!draft) {
      return;
    }

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
          {orders.map((order) => {
            const shipment = shipments[order.id];
            const shipmentDraft = shipmentDrafts[order.id];
            const detail = orderDetails[order.id];

            return (
              <tr key={order.id}>
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
                    <select
                      className="select"
                      value={statusDrafts[order.id] ?? order.orderStatus}
                      onChange={(event) =>
                        setStatusDrafts((current) => ({ ...current, [order.id]: event.target.value }))
                      }
                    >
                      {orderStatuses.map((status) => (
                        <option value={status} key={status}>{status}</option>
                      ))}
                    </select>
                    <input
                      className="admin-input"
                      placeholder="Ghi chú nội bộ"
                      value={noteDrafts[order.id] ?? ""}
                      onChange={(event) =>
                        setNoteDrafts((current) => ({ ...current, [order.id]: event.target.value }))
                      }
                    />
                    <button className="admin-btn" type="button" onClick={() => void handleUpdate(order.id)} disabled={savingId === order.id}>
                      {savingId === order.id ? "Đang lưu..." : "Lưu trạng thái"}
                    </button>
                    <button
                      className="admin-btn secondary"
                      type="button"
                      onClick={() => void handleLoadDetail(order.id)}
                      disabled={savingId === `detail:${order.id}`}
                    >
                      {savingId === `detail:${order.id}` ? "Đang tải..." : "Chi tiết"}
                    </button>
                    <button
                      className="admin-btn secondary"
                      type="button"
                      onClick={() => void handleLoadShipment(order.id)}
                      disabled={savingId === `load-shipment:${order.id}`}
                    >
                      {savingId === `load-shipment:${order.id}` ? "Đang tải..." : "Giao vận"}
                    </button>
                  </div>

                  {shipmentDraft ? (
                    <div className="admin-subcard admin-subcard-tight">
                      <div className="table-subtle">
                        {shipment?.shipmentCode ? `Mã vận đơn: ${shipment.shipmentCode}` : "Chưa có shipment, sẽ tạo khi lưu lần đầu."}
                      </div>
                      <div className="admin-form-grid">
                        <select
                          className="select"
                          value={shipmentDraft.shippingMethodId}
                          onChange={(event) =>
                            setShipmentDrafts((current) => ({
                              ...current,
                              [order.id]: { ...current[order.id], shippingMethodId: event.target.value }
                            }))
                          }
                        >
                          <option value="">Chọn phương thức giao hàng</option>
                          {shippingMethods.map((item) => (
                            <option value={item.id} key={item.id}>{item.name}</option>
                          ))}
                        </select>
                        <select
                          className="select"
                          value={shipmentDraft.status}
                          onChange={(event) =>
                            setShipmentDrafts((current) => ({
                              ...current,
                              [order.id]: { ...current[order.id], status: event.target.value }
                            }))
                          }
                        >
                          {shipmentStatuses.map((status) => (
                            <option value={status} key={status}>{status}</option>
                          ))}
                        </select>
                        <input
                          className="admin-input"
                          placeholder="Đơn vị vận chuyển"
                          value={shipmentDraft.carrierName}
                          onChange={(event) =>
                            setShipmentDrafts((current) => ({
                              ...current,
                              [order.id]: { ...current[order.id], carrierName: event.target.value }
                            }))
                          }
                        />
                        <input
                          className="admin-input"
                          placeholder="Mã tracking"
                          value={shipmentDraft.trackingNumber}
                          onChange={(event) =>
                            setShipmentDrafts((current) => ({
                              ...current,
                              [order.id]: { ...current[order.id], trackingNumber: event.target.value }
                            }))
                          }
                        />
                        <div className="admin-form-full">
                          <input
                            className="admin-input"
                            placeholder="Ghi chú giao vận"
                            value={shipmentDraft.note}
                            onChange={(event) =>
                              setShipmentDrafts((current) => ({
                                ...current,
                                [order.id]: { ...current[order.id], note: event.target.value }
                              }))
                            }
                          />
                        </div>
                      </div>
                      <button
                        className="admin-btn secondary"
                        type="button"
                        onClick={() => void handleSaveShipment(order.id)}
                        disabled={savingId === `save-shipment:${order.id}`}
                      >
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
          })}
        </tbody>
      </table>
    </section>
  );
}
