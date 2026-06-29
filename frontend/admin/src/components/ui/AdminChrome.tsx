"use client";

import { Bell, ChartLineUp, GearSix, House, MagnifyingGlass, Package, Receipt, Robot, ShieldCheck, SoccerBall, TShirt, Truck, Users } from "@phosphor-icons/react";
import { usePathname } from "next/navigation";

const navItems = [
  { label: "Tổng quan", href: "/", icon: House },
  { label: "Đơn hàng", href: "/orders", icon: Receipt },
  { label: "Tồn kho", href: "/inventory", icon: Package },
  { label: "Sản phẩm", href: "/products", icon: TShirt },
  { label: "Khách hàng", href: "/customers", icon: Users },
  { label: "Báo cáo", href: "/reports", icon: ChartLineUp },
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
      </div>
    </header>
  );
}
