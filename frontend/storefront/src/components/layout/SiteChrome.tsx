"use client";

import { usePathname } from "next/navigation";
import { Footer } from "@/components/layout/Footer";
import { ChatWidget } from "@/modules/chat/ChatWidget";

/**
 * Print pages (e.g. customer invoice) render standalone, without the storefront
 * chrome. Header is an async Server Component (fetches the category tree), so
 * it must stay instantiated by the Server Component tree (RootLayout) and be
 * passed in already-rendered rather than imported here - a Client Component
 * cannot import and render an async Server Component itself.
 */
export function SiteChrome({ header, children }: { header: React.ReactNode; children: React.ReactNode }) {
  const pathname = usePathname();
  const isPrintPage = pathname.endsWith("/hoa-don");

  if (isPrintPage) {
    return <>{children}</>;
  }

  return (
    <>
      {header}
      {children}
      <Footer />
      <ChatWidget />
    </>
  );
}
