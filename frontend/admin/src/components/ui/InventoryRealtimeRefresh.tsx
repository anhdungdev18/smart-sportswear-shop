"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8082";
const INVENTORY_PATH = "/inventory";
const REFRESH_DEBOUNCE_MS = 1_000;

export function InventoryRealtimeRefresh() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (pathname !== INVENTORY_PATH || typeof EventSource === "undefined") return;

    const stream = new EventSource(`${API_BASE}/api/v1/inventory/stream`);
    let timer: number | undefined;

    stream.addEventListener("stock-changed", (event) => {
      let detail: unknown = event.data;
      try {
        detail = JSON.parse(event.data);
      } catch {
        // Keep the raw payload so one malformed event cannot stop refreshes.
      }

      window.dispatchEvent(new CustomEvent("inventory:stock-changed", { detail }));
      window.clearTimeout(timer);
      timer = window.setTimeout(() => {
        if (document.visibilityState === "visible") {
          router.refresh();
        }
      }, REFRESH_DEBOUNCE_MS);
    });

    return () => {
      window.clearTimeout(timer);
      stream.close();
    };
  }, [pathname, router]);

  return null;
}
