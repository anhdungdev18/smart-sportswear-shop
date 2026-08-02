"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "@/lib/api-errors";
import { getPaymentsByOrder, processVnpayReturn, type PaymentResponse } from "@/modules/payment/api";

export function PaymentResultClient() {
  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const orderId = sessionStorage.getItem("vnpay-pending-order-id");
    if (!orderId) {
      const timer = window.setTimeout(() => {
        setError("Không tìm thấy đơn hàng VNPay vừa thanh toán.");
        setLoading(false);
      }, 0);
      return () => window.clearTimeout(timer);
    }
    let cancelled = false;
    let attempts = 0;
    const check = async () => {
      try {
        if (attempts === 0) {
          await processVnpayReturn(new URLSearchParams(window.location.search));
        }
        const payments = await getPaymentsByOrder(orderId);
        if (cancelled) return;
        const latest = payments[0] ?? null;
        setPayment(latest);
        attempts += 1;
        if (latest?.status === "PENDING" && attempts < 10) {
          window.setTimeout(check, 2000);
        } else {
          setLoading(false);
          if (latest?.status !== "PENDING") sessionStorage.removeItem("vnpay-pending-order-id");
        }
      } catch (cause) {
        if (!cancelled) {
          setError(getApiErrorMessage(cause, "Không thể xác minh trạng thái thanh toán."));
          setLoading(false);
        }
      }
    };
    void check();
    return () => { cancelled = true; };
  }, []);

  const message = payment?.status === "PAID"
    ? "Thanh toán VNPay thành công."
    : payment?.status === "FAILED"
      ? "Thanh toán không thành công. Bạn có thể thử lại từ chi tiết đơn hàng."
      : payment?.status === "CANCELLED"
        ? "Giao dịch đã bị hủy."
        : "Đang chờ VNPay xác nhận giao dịch...";

  return (
    <main className="page-below-header mx-auto min-h-[60vh] max-w-2xl px-4 py-16 text-center">
      <h1 className="text-3xl font-semibold uppercase text-ivy-dark">Kết quả thanh toán</h1>
      {error ? <p className="mt-8 text-[#C62127]">{error}</p> : <p className="mt-8 text-ivy-text">{loading ? "Đang xác minh..." : message}</p>}
      {payment ? <p className="mt-3 text-sm text-ivy-text">Mã giao dịch: {payment.transactionRef}</p> : null}
      <div className="mt-10 flex justify-center gap-4">
        <Link href="/tai-khoan" className="border border-ivy-dark px-6 py-3">Xem đơn hàng</Link>
        <Link href="/" className="bg-ivy-dark px-6 py-3 text-white">Tiếp tục mua sắm</Link>
      </div>
    </main>
  );
}
