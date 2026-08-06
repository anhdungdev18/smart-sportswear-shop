"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getApiErrorMessage } from "@/lib/api-errors";
import { useAuthenticated } from "@/lib/use-authenticated";
import { cancelOrder, listMyOrders } from "@/modules/account/api";
import type { OrderResponse } from "@/modules/account/types";

export function OrderHistoryPageClient() {
  const authenticated = useAuthenticated();
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [cancellingId, setCancellingId] = useState<string | null>(null);

  const loadOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await listMyOrders({ limit: 20 });
      setOrders(result.data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể tải lịch sử đơn hàng."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authenticated) {
      setLoading(false);
      return;
    }
    void loadOrders();
  }, [authenticated]);

  const handleCancel = async (id: string) => {
    setCancellingId(id);
    setError(null);
    setSuccess(null);
    try {
      await cancelOrder(id, "Khách hàng yêu cầu hủy");
      await loadOrders();
      setSuccess("Đơn hàng đã được hủy.");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể hủy đơn hàng."));
    } finally {
      setCancellingId(null);
    }
  };

  const canCancel = (order: OrderResponse) => order.orderStatus === "PENDING_CONFIRMATION";

  if (!authenticated) {
    return (
      <main className="site-main page-below-header-spacious flex-1 border-b border-ivy-hairline">
        <div className="mx-auto max-w-[1380px] px-4 pb-24">
          <div className="border border-ivy-hairline px-6 py-10">
            <h1 className="text-[32px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Tra cứu đơn hàng</h1>
            <p className="mt-4 text-[15px] leading-7 text-ivy-text">Vui lòng đăng nhập tài khoản khách hàng để xem lịch sử đơn hàng thật từ hệ thống.</p>
            <Link
              href="/dang-nhap"
              className="mt-8 inline-flex h-12 items-center justify-center rounded-tl-[20px] rounded-br-[20px] bg-ivy-dark px-8 text-[14px] font-semibold uppercase tracking-[0.05em] text-white"
            >
              Đăng nhập
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="site-main page-below-header-spacious flex-1 border-b border-ivy-hairline">
      <div className="mx-auto max-w-[1380px] px-4 pb-24">
        <div className="mb-10">
          <p className="mb-3 text-[13px] uppercase tracking-[0.24em] text-ivy-text-muted">Khách hàng</p>
          <h1 className="text-[40px] font-semibold uppercase tracking-[0.06em] text-ivy-dark">Tra cứu đơn hàng</h1>
        </div>

        {error ? <p className="mb-4 text-[14px] text-[#C62127]">{error}</p> : null}
        {success ? <p className="mb-4 text-[14px] text-[#257A4D]">{success}</p> : null}

        {loading ? (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">Đang tải đơn hàng...</div>
        ) : orders.length > 0 ? (
          <div className="space-y-6">
            {orders.map((order) => (
              <article key={order.id} className="border border-ivy-hairline px-6 py-6">
                <div className="flex flex-col gap-4 border-b border-ivy-hairline pb-5 md:flex-row md:items-start md:justify-between">
                  <div>
                    <h2 className="text-[22px] font-semibold uppercase tracking-[0.04em] text-ivy-dark">{order.orderCode}</h2>
                    <p className="mt-2 text-[14px] text-ivy-text">Ngày tạo: {new Date(order.createdAt).toLocaleString("vi-VN")}</p>
                    <p className="mt-1 text-[14px] text-ivy-text">Trạng thái đơn: {order.orderStatus}</p>
                    <p className="mt-1 text-[14px] text-ivy-text">Thanh toán: {order.paymentMethod} / {order.paymentStatus}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-[13px] uppercase tracking-[0.08em] text-ivy-text-muted">Tổng thanh toán</p>
                    <p className="mt-2 text-[24px] font-semibold text-ivy-dark">{order.totalAmount.toLocaleString("vi-VN")}đ</p>
                    {canCancel(order) ? (
                      <button
                        type="button"
                        onClick={() => void handleCancel(order.id)}
                        disabled={cancellingId === order.id}
                        className="mt-4 h-10 rounded-tl-[18px] rounded-br-[18px] border border-ivy-dark px-5 text-[12px] font-semibold uppercase tracking-[0.05em] text-ivy-dark disabled:opacity-60"
                      >
                        {cancellingId === order.id ? "Đang hủy..." : "Hủy đơn"}
                      </button>
                    ) : null}
                  </div>
                </div>

                <div className="mt-5 space-y-4">
                  {order.items.map((item) => (
                    <div key={item.id} className="flex flex-col gap-2 border-b border-dashed border-ivy-hairline pb-4 last:border-none last:pb-0 md:flex-row md:items-center md:justify-between">
                      <div>
                        <p className="text-[16px] font-medium text-ivy-dark">{item.productName}</p>
                        <p className="mt-1 text-[14px] text-ivy-text">SKU: {item.sku}</p>
                        <p className="mt-1 text-[14px] text-ivy-text">Màu / Size: {item.color || "N/A"} / {item.size || "N/A"}</p>
                        <p className="mt-1 text-[14px] text-ivy-text">Số lượng: {item.quantity}</p>
                      </div>
                      <div className="text-[18px] font-semibold text-ivy-dark">{item.lineTotal.toLocaleString("vi-VN")}đ</div>
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="border border-ivy-hairline px-6 py-10 text-[15px] text-ivy-text">Bạn chưa có đơn hàng nào.</div>
        )}
      </div>
    </main>
  );
}
