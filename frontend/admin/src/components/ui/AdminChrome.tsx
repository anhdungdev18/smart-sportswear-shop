"use client";

import {
  Bell,
  ChartLineUp,
  GearSix,
  House,
  MagnifyingGlass,
  Package,
  Receipt,
  Robot,
  ShieldCheck,
  SoccerBall,
  Scroll,
  SignOut,
  Tag,
  TShirt,
  Truck,
  Users
} from "@phosphor-icons/react";
import { usePathname, useRouter } from "next/navigation";
import { adminLogout } from "@/modules/auth/api";
import { clearAuthSession, getBrowserRefreshToken } from "@/modules/auth/session";

const navItems = [
  { label: "Tổng quan", href: "/", icon: House },
  { label: "Đơn hàng", href: "/orders", icon: Receipt },
  { label: "Tồn kho", href: "/inventory", icon: Package },
  { label: "Sản phẩm", href: "/products", icon: TShirt },
  { label: "Nội dung", href: "/pages", icon: Tag },
  { label: "Danh mục", href: "/categories", icon: Tag },
  { label: "Thương hiệu", href: "/brands", icon: Tag },
  { label: "Khuyến mãi", href: "/promotions", icon: Tag },
  { label: "Mã giảm giá", href: "/coupons", icon: Tag },
  { label: "Banner", href: "/banners", icon: Tag },
  { label: "Đánh giá", href: "/reviews", icon: Tag },
  { label: "Đổi trả", href: "/returns", icon: Receipt },
  { label: "Khách hàng", href: "/customers", icon: Users },
  { label: "Báo cáo", href: "/reports", icon: ChartLineUp },
  { label: "Nhật ký", href: "/audit-logs", icon: Scroll },
  { label: "Chatbot", href: "/chatbot-config", icon: Robot },
  { label: "Phân quyền", href: "/users-roles", icon: ShieldCheck },
  { label: "Hệ thống", href: "/system-config", icon: GearSix }
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="sidebar">
      <div className="admin-brand">
        <span>
          <SoccerBall size={20} weight="fill" />
        </span>
        <strong>THF Admin</strong>
      </div>
      <nav className="side-nav" aria-label="Admin">
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);

          return (
            <a className={active ? "active" : ""} href={item.href} key={item.label}>
              <Icon size={20} weight="duotone" />
              <span>{item.label}</span>
            </a>
          );
        })}
      </nav>
    </aside>
  );
}

export function AdminTopbar() {
  const router = useRouter();

  async function handleLogout() {
    const refreshToken = getBrowserRefreshToken();

    if (refreshToken) {
      try {
        await adminLogout(refreshToken);
      } catch {
        // Best-effort: clear session even if the API call fails
      }
    }

    clearAuthSession();
    router.push("/login");
    router.refresh();
  }

  return (
    <header className="admin-topbar">
      <div className="admin-search">
        <MagnifyingGlass size={20} />
        <span>Tìm đơn hàng, SKU, khách hàng...</span>
      </div>
      <div className="admin-actions">
        <button className="admin-icon-btn" aria-label="Thông báo">
          <Bell size={20} weight="duotone" />
        </button>
        <button className="admin-btn">
          <Truck size={18} weight="duotone" />
          Xuất báo cáo
        </button>
        <button
          className="admin-btn secondary"
          onClick={handleLogout}
          aria-label="Đăng xuất"
        >
          <SignOut size={18} weight="duotone" />
          Đăng xuất
        </button>
      </div>
    </header>
  );
}
