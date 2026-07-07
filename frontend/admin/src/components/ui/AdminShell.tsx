"use client";

import { usePathname } from "next/navigation";
import { AdminTopbar, Sidebar } from "@/components/ui/AdminChrome";

export function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isLoginPage = pathname === "/login";

  if (isLoginPage) {
    return <>{children}</>;
  }

  return (
    <div className="admin-shell">
      <Sidebar />
      <div className="content-area">
        <AdminTopbar />
        <div className="content">{children}</div>
      </div>
    </div>
  );
}
