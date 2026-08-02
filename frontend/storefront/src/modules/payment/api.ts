import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";

export interface PaymentResponse {
  id: string;
  provider: "COD" | "VNPAY";
  transactionRef: string;
  amount: number;
  status: "UNPAID" | "PENDING" | "PAID" | "FAILED" | "CANCELLED" | "REFUNDED";
  gatewayTransactionNo?: string | null;
  bankCode?: string | null;
  paidAt?: string | null;
  createdAt: string;
}

export async function getPaymentsByOrder(orderId: string) {
  const result = await apiFetch<PaymentResponse[]>(endpoints.payments.byOrder(orderId));
  return result.data;
}

export async function processVnpayReturn(params: URLSearchParams) {
  const query: Record<string, string> = {};
  params.forEach((value, key) => {
    if (key.startsWith("vnp_")) query[key] = value;
  });
  if (!query.vnp_SecureHash) return;
  await apiFetch<Record<string, string>>(endpoints.payments.callback, { query, cache: "no-store" });
}
