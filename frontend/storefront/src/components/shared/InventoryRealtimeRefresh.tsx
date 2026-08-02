"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { API_BASE } from "@/lib/api";

export function InventoryRealtimeRefresh() {
  const router = useRouter();

  useEffect(() => {
    if (typeof EventSource === "undefined") return;
    const stream = new EventSource(`${API_BASE}/api/v1/inventory/stream`);
    let timer: number | undefined;
    stream.addEventListener("stock-changed", () => {
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
