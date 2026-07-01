"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import {
  createRefund,
  fetchAdminReturnDetail,
  updateRefundStatus,
  updateReturnStatus
} from "@/modules/returns/browser-api";
import type { RefundResponse, ReturnResponse } from "@/modules/returns/types";

const returnStatuses = ["REQUESTED", "APPROVED", "REJECTED", "RECEIVED", "REFUNDED", "CANCELLED"] as const;
const refundStatuses = ["PENDING", "PROCESSING", "COMPLETED", "FAILED", "CANCELLED"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

export function AdminReturnsClient({
  initialReturns,
  initialRefundsByReturn
}: {
  initialReturns: ReturnResponse[];
  initialRefundsByReturn: Record<string, RefundResponse[]>;
}) {
  const [items, setItems] = useState(initialReturns);
  const [selectedId, setSelectedId] = useState("");
  const [refundsByReturn, setRefundsByReturn] = useState(initialRefundsByReturn);
  const [statusDrafts, setStatusDrafts] = useState<Record<string, string>>(
    Object.fromEntries(initialReturns.map((item) => [item.id, item.status]))
  );
  const [refundStatusDrafts, setRefundStatusDrafts] = useState<Record<string, string>>(
    Object.fromEntries(
      Object.values(initialRefundsByReturn)
        .flat()
        .map((item) => [item.id, item.status])
    )
  );
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState<string | null>(null);

  async function handleLoadDetail(id: string) {
    try {
      setSaving(`detail:${id}`);
      setSelectedId(id);
      setMessage(null);
      const detail = await fetchAdminReturnDetail(id);
      setItems((current) => current.map((item) => (item.id === detail.id ? detail : item)));
    } catch (error) {
      setMessage(extractError(error, "Không tải được chi tiết yêu cầu đổi trả"));
    } finally {
      setSaving(null);
    }
  }

  async function handleUpdateStatus(item: ReturnResponse) {
    try {
      setSaving(`return:${item.id}`);
      setMessage(null);
      const updated = await updateReturnStatus(item.id, { status: statusDrafts[item.id] ?? item.status });
      setItems((current) => current.map((currentItem) => (currentItem.id === updated.id ? updated : currentItem)));
      setMessage(`Đã cập nhật trạng thái yêu cầu ${updated.returnCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được trạng thái đổi trả"));
    } finally {
      setSaving(null);
    }
  }

  async function handleCreateRefund(item: ReturnResponse) {
    try {
      setSaving(`refund-create:${item.id}`);
      setMessage(null);
      const created = await createRefund(item.id);
      setRefundsByReturn((current) => ({
        ...current,
        [item.id]: [created, ...(current[item.id] ?? [])]
      }));
      setRefundStatusDrafts((current) => ({ ...current, [created.id]: created.status }));
      setMessage(`Đã tạo hoàn tiền ${created.refundCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không tạo được hoàn tiền"));
    } finally {
      setSaving(null);
    }
  }

  async function handleUpdateRefund(returnId: string, refund: RefundResponse) {
    try {
      setSaving(`refund:${refund.id}`);
      setMessage(null);
      const updated = await updateRefundStatus(refund.id, {
        status: refundStatusDrafts[refund.id] ?? refund.status
      });
      setRefundsByReturn((current) => ({
        ...current,
        [returnId]: (current[returnId] ?? []).map((item) => (item.id === updated.id ? updated : item))
      }));
      setMessage(`Đã cập nhật hoàn tiền ${updated.refundCode}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được hoàn tiền"));
    } finally {
      setSaving(null);
    }
  }

  return (
    <section className="card panel">
      <div className="panel-header">
        <div>
          <h2>Quản lý đổi trả và hoàn tiền</h2>
          <p className="panel-copy">Theo dõi return workflow và refund ngay trên cùng một màn hình.</p>
        </div>
      </div>
      {message ? <p className="action-message">{message}</p> : null}

      <div className="admin-stack">
        {items.map((item) => {
          const refunds = refundsByReturn[item.id] ?? [];

          return (
            <article className={`admin-subcard${selectedId === item.id ? " row-selected" : ""}`} key={item.id}>
              <div className="admin-return-head">
                <div>
                  <strong>{item.returnCode}</strong>
                  <div className="table-subtle">
                    Đơn {item.orderCode} · {item.reason} · {new Date(item.createdAt).toLocaleString("vi-VN")}
                  </div>
                  {item.description ? <div className="table-subtle">{item.description}</div> : null}
                </div>
                <div className="admin-inline-form wrap">
                  <select
                    className="select"
                    value={statusDrafts[item.id] ?? item.status}
                    onChange={(event) =>
                      setStatusDrafts((current) => ({ ...current, [item.id]: event.target.value }))
                    }
                  >
                    {returnStatuses.map((status) => (
                      <option value={status} key={status}>{status}</option>
                    ))}
                  </select>
                  <button className="admin-btn" type="button" onClick={() => void handleUpdateStatus(item)} disabled={saving === `return:${item.id}`}>
                    {saving === `return:${item.id}` ? "Đang lưu..." : "Lưu trạng thái"}
                  </button>
                  <button className="admin-btn secondary" type="button" onClick={() => void handleLoadDetail(item.id)} disabled={saving === `detail:${item.id}`}>
                    {saving === `detail:${item.id}` ? "Đang tải..." : "Tải chi tiết"}
                  </button>
                  <button className="admin-btn secondary" type="button" onClick={() => void handleCreateRefund(item)} disabled={saving === `refund-create:${item.id}`}>
                    {saving === `refund-create:${item.id}` ? "Đang tạo..." : "Tạo hoàn tiền"}
                  </button>
                </div>
              </div>

              <table className="data-table">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Số lượng</th>
                    <th>Tình trạng</th>
                    <th>Hướng xử lý</th>
                    <th>Refund</th>
                  </tr>
                </thead>
                <tbody>
                  {item.items.map((returnItem) => (
                    <tr key={returnItem.id}>
                      <td>{returnItem.productName}</td>
                      <td>{returnItem.sku}</td>
                      <td>{returnItem.quantity}</td>
                      <td>{returnItem.conditionStatus ?? "-"}</td>
                      <td>{returnItem.resolution ?? "-"}</td>
                      <td>{returnItem.refundAmount != null ? `${Math.round(returnItem.refundAmount).toLocaleString("vi-VN")}₫` : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {refunds.length ? (
                <div className="admin-stack">
                  {refunds.map((refund) => (
                    <div className="admin-inline-form wrap" key={refund.id}>
                      <strong>{refund.refundCode}</strong>
                      <span className="table-subtle">{refund.provider} · {Math.round(refund.amount).toLocaleString("vi-VN")}₫</span>
                      <select
                        className="select"
                        value={refundStatusDrafts[refund.id] ?? refund.status}
                        onChange={(event) =>
                          setRefundStatusDrafts((current) => ({ ...current, [refund.id]: event.target.value }))
                        }
                      >
                        {refundStatuses.map((status) => (
                          <option value={status} key={status}>{status}</option>
                        ))}
                      </select>
                      <button className="admin-btn secondary" type="button" onClick={() => void handleUpdateRefund(item.id, refund)} disabled={saving === `refund:${refund.id}`}>
                        {saving === `refund:${refund.id}` ? "Đang lưu..." : "Lưu hoàn tiền"}
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">Yêu cầu này chưa có giao dịch hoàn tiền.</div>
              )}
            </article>
          );
        })}
      </div>
    </section>
  );
}
