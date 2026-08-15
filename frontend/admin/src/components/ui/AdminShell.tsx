"use client";

import { usePathname } from "next/navigation";
import { AdminTopbar, Sidebar } from "@/components/ui/AdminChrome";
import { ADMIN_AI_WORKSPACES_ENABLED } from "@/config/feature-flags";

/** Các route không cần topbar để trang có thể dùng toàn bộ chiều cao */
const FULLSCREEN_ROUTES = ["/admin-copilot"];

export function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isLoginPage = pathname === "/login";
  const isPrintPage = pathname.includes("/packing-slip");
  const isFullscreen = ADMIN_AI_WORKSPACES_ENABLED && FULLSCREEN_ROUTES.some((r) => pathname.startsWith(r));

  if (isLoginPage || isPrintPage) {
    return <>{children}</>;
  }

  return (
    <div className="admin-shell">
      <Sidebar />
      <div className={isFullscreen ? "content-area content-area--fullscreen" : "content-area"}>
        {!isFullscreen && <AdminTopbar />}
        <div className={isFullscreen ? "content content--fullscreen" : "content"}>{children}</div>
      </div>
    </div>
  );
}
