"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { API_BASE } from "@/lib/api";

const REFRESH_DEBOUNCE_MS = 250;

export function DataRealtimeRefresh() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (typeof EventSource === "undefined") return;
    const stream = new EventSource(`${API_BASE}/api/v1/realtime/stream`);
    let timer: number | undefined;
    const refresh = (event: MessageEvent) => {
      let scope = "application";
      try { scope = (JSON.parse(event.data) as { scope?: string }).scope ?? scope; } catch { /* ignore malformed event */ }
      if (!isRelevantToStorefront(scope, pathname)) return;
      window.clearTimeout(timer);
      timer = window.setTimeout(() => router.refresh(), REFRESH_DEBOUNCE_MS);
    };
    stream.addEventListener("data-changed", refresh as EventListener);
    return () => {
      window.clearTimeout(timer);
      stream.close();
    };
  }, [pathname, router]);

  return null;
}

function isRelevantToStorefront(scope: string, pathname: string) {
  if (["categories", "settings"].includes(scope)) return true;
  if (["banners", "promotions", "collections", "brands", "products", "variants", "combos"].includes(scope)) {
    return pathname === "/" || ["/sanpham", "/danh-muc", "/cua-hang", "/tim-kiem", "/lookbook", "/bo-suu-tap"]
      .some((route) => pathname.startsWith(route));
  }
  // Order updates are delivered through the authenticated notification stream.
  // That stream is customer-specific and includes the changed order id, whereas
  // this application-wide stream would refresh every customer's whole page for
  // an order changed by somebody else.
  if (scope === "orders") return false;
  if (scope === "returns") return pathname.startsWith("/tai-khoan") || pathname.startsWith("/tra-cuu-don-hang");
  if (scope === "reviews") return pathname.startsWith("/sanpham");
  if (scope === "pages") return pathname.startsWith("/about") || pathname.startsWith("/tin-tuc");
  return false;
}
