"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8082";

export function InventoryRealtimeRefresh() {
  const router = useRouter();

  useEffect(() => {
    if (typeof EventSource === "undefined") return;
    const stream = new EventSource(`${API_BASE}/api/v1/inventory/stream`);
    let timer: number | undefined;
    stream.addEventListener("stock-changed", (event) => {
      window.dispatchEvent(new CustomEvent("inventory:stock-changed", { detail: JSON.parse(event.data) }));
      window.clearTimeout(timer);
      timer = window.setTimeout(() => router.refresh(), 150);
    });
    return () => {
      window.clearTimeout(timer);
      stream.close();
    };
  }, [router]);

  return null;
}
