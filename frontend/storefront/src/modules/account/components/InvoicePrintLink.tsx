"use client";

import type { MouseEvent } from "react";
import type { OrderResponse } from "@/modules/account/types";

const INVOICE_PAYLOAD_PREFIX = "invoice-print:";

export function InvoicePrintLink({ order, className }: { order: OrderResponse; className?: string }) {
  const href = `/tai-khoan/don-hang/${order.id}/hoa-don`;

  function handleClick(event: MouseEvent<HTMLAnchorElement>) {
    // Match the admin print flow: hand the data already on screen to the new
    // tab so it can render immediately, then let that tab refresh from the
    // invoice endpoint to validate eligibility and obtain the invoice number.
    const payloadKey = `${INVOICE_PAYLOAD_PREFIX}${Date.now()}-${Math.random().toString(36).slice(2)}`;
    try {
      window.localStorage.setItem(payloadKey, JSON.stringify(order));
      event.preventDefault();
      window.open(`${href}?payload=${encodeURIComponent(payloadKey)}`, "_blank", "noopener,noreferrer");
    } catch {
      // Storage may be disabled; the normal page still works via its API call.
    }
  }

  return (
    <a href={href} target="_blank" rel="noopener noreferrer" onClick={handleClick} className={className}>
      In hóa đơn
    </a>
  );
}

export function readTransferredInvoice(payloadKey: string | undefined, orderId: string) {
  if (typeof window === "undefined") return null;
  if (!payloadKey?.startsWith(INVOICE_PAYLOAD_PREFIX)) return null;
  try {
    const payload = window.localStorage.getItem(payloadKey);
    window.localStorage.removeItem(payloadKey);
    if (!payload) return null;
    const parsed = JSON.parse(payload) as OrderResponse;
    return parsed?.id === orderId ? parsed : null;
  } catch {
    return null;
  }
}
