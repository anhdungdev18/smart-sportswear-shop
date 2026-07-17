"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { cancelOrder, getOrderDetail } from "@/modules/account/api";
import type { OrderResponse } from "@/modules/account/types";
import { getApiErrorMessage } from "@/lib/api-errors";
import { getAccessToken } from "@/lib/session";

const money = (n: number) => `${n.toLocaleString("vi-VN")}đ`;

export function OrderDetailPageClient({ orderId }: { orderId: string }) {
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setOrder(await getOrderDetail(orderId));
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể tải chi tiết đơn hàng."));
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    // Defer off the effect body so no setState runs synchronously inline.
    const timer = setTimeout(() => {
      if (!getAccessToken()) {
        setLoading(false);
        setError("Vui lòng đăng nhập để xem chi tiết đơn hàng.");
        return;
      }
      void load();
    }, 0);
    return () => clearTimeout(timer);
  }, [load]);

  const handleCancel = async () => {
    if (!order) return;
    setCancelling(true);
    try {
      const updated = await cancelOrder(order.id);
      setOrder(updated);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể hủy đơn hàng."));
    } finally {
      setCancelling(false);
    }
  };

  const canCancel = order?.orderStatus === "PENDING_CONFIRMATION";

  return (
    <section className="mx-auto w-full max-w-4xl px-4 py-10 md:px-6">
      <Link
        href="/tai-khoan"
        className="text-[13px] uppercase tracking-[0.08em] text-ivy-text hover:text-ivy-accent"
      >
        ← Về tài khoản
      </Link>

      {loading ? (
        <div className="mt-6 border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">
          Đang tải chi tiết đơn hàng...
        </div>
      ) : error ? (
        <div className="mt-6 border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">{error}</div>
      ) : order ? (
        <article className="mt-6 border border-ivy-hairline px-6 py-6">
          <div className="flex flex-col gap-4 border-b border-ivy-hairline pb-5 md:flex-row md:items-start md:justify-between">
            <div>
              <h1 className="text-[26px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">
                {order.orderCode}
              </h1>
              <p className="mt-2 text-[14px] text-ivy-text">
                Ngày tạo: {new Date(order.createdAt).toLocaleString("vi-VN")}
              </p>
              <p className="mt-1 text-[14px] text-ivy-text">Trạng thái đơn: {order.orderStatus}</p>
              <p className="mt-1 text-[14px] text-ivy-text">
                Thanh toán: {order.paymentMethod} / {order.paymentStatus}
              </p>
            </div>
            <div className="text-right">
              <p className="text-[13px] uppercase tracking-[0.08em] text-ivy-text-muted">Tổng thanh toán</p>
              <p className="mt-2 text-[24px] font-semibold text-ivy-dark">{money(order.totalAmount)}</p>
              {canCancel ? (
                <button
                  type="button"
                  onClick={() => void handleCancel()}
                  disabled={cancelling}
                  className="mt-4 h-10 rounded-tl-[18px] rounded-br-[18px] border border-ivy-dark px-5 text-[12px] font-semibold uppercase tracking-[0.05em] text-ivy-dark disabled:opacity-60"
                >
                  {cancelling ? "Đang hủy..." : "Hủy đơn"}
                </button>
              ) : null}
            </div>
          </div>

          <div className="mt-5 space-y-4">
            {order.items.map((item) => (
              <div
                key={item.id}
                className="flex flex-col gap-2 border-b border-dashed border-ivy-hairline pb-4 last:border-none last:pb-0 md:flex-row md:items-center md:justify-between"
              >
                <div>
                  <p className="text-[16px] font-medium text-ivy-dark">{item.productName}</p>
                  <p className="mt-1 text-[14px] text-ivy-text">SKU: {item.sku}</p>
                  <p className="mt-1 text-[14px] text-ivy-text">
                    Màu / Size: {item.color || "N/A"} / {item.size || "N/A"}
                  </p>
                  <p className="mt-1 text-[14px] text-ivy-text">
                    {money(item.unitPrice)} × {item.quantity}
                  </p>
                </div>
                <div className="text-[18px] font-semibold text-ivy-dark">{money(item.lineTotal)}</div>
              </div>
            ))}
          </div>

          <dl className="mt-6 space-y-2 border-t border-ivy-hairline pt-5 text-[14px] text-ivy-text">
            <div className="flex justify-between">
              <dt>Tạm tính</dt>
              <dd>{money(order.subtotalAmount)}</dd>
            </div>
            <div className="flex justify-between">
              <dt>Phí vận chuyển</dt>
              <dd>{money(order.shippingFee)}</dd>
            </div>
            {order.discountAmount > 0 ? (
              <div className="flex justify-between">
                <dt>Giảm giá</dt>
                <dd>-{money(order.discountAmount)}</dd>
              </div>
            ) : null}
            <div className="flex justify-between pt-2 text-[16px] font-semibold text-ivy-dark">
              <dt>Tổng cộng</dt>
              <dd>{money(order.totalAmount)}</dd>
            </div>
          </dl>

          {order.note ? (
            <p className="mt-5 border-t border-ivy-hairline pt-4 text-[14px] text-ivy-text">
              Ghi chú: {order.note}
            </p>
          ) : null}
        </article>
      ) : null}
    </section>
  );
}
