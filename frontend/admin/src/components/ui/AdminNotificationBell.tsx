"use client";

import { Bell } from "@phosphor-icons/react";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import {
  getMyUnreadCount,
  listMyNotifications,
  markAllMyNotificationsRead,
  markMyNotificationRead,
} from "@/modules/notifications/inbox-api";
import type { NotificationResponse } from "@/modules/notifications/types";

export function AdminNotificationBell() {
  const [items, setItems] = useState<NotificationResponse[]>([]);
  const [unread, setUnread] = useState(0);
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);

  async function load() {
    try {
      const [notifications, count] = await Promise.all([listMyNotifications(), getMyUnreadCount()]);
      setItems(notifications);
      setUnread(count.unread);
    } catch {
      // The admin shell remains usable if notifications are temporarily unavailable.
    }
  }

  useEffect(() => {
    void load();
    const timer = window.setInterval(() => void load(), 15000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    if (!open) return;
    const close = (event: MouseEvent) => {
      if (root.current && !root.current.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  async function markRead(item: NotificationResponse) {
    if (item.readAt) return;
    setItems((current) => current.map((entry) => entry.id === item.id
      ? { ...entry, readAt: new Date().toISOString() }
      : entry));
    setUnread((current) => Math.max(0, current - 1));
    try { await markMyNotificationRead(item.id); } catch { /* optimistic UI */ }
  }

  async function markAllRead() {
    setItems((current) => current.map((entry) => ({ ...entry, readAt: entry.readAt ?? new Date().toISOString() })));
    setUnread(0);
    try { await markAllMyNotificationsRead(); } catch { /* optimistic UI */ }
  }

  return (
    <div className="admin-notification" ref={root}>
      <button type="button" className="admin-icon-btn" aria-label="Thông báo" onClick={() => setOpen((value) => !value)}>
        <Bell size={20} weight="duotone" />
        {unread > 0 && <span className="admin-notification-count">{unread > 9 ? "9+" : unread}</span>}
      </button>
      {open && (
        <div className="admin-notification-panel">
          <div className="admin-notification-head">
            <strong>Thông báo</strong>
            {unread > 0 && <button type="button" onClick={() => void markAllRead()}>Đánh dấu đã đọc</button>}
          </div>
          <div className="admin-notification-list">
            {items.length === 0 ? <p className="admin-notification-empty">Chưa có thông báo nào.</p> : items.map((item) => (
              <Link key={item.id} href="/orders"
                className={`admin-notification-item${item.readAt ? "" : " unread"}`}
                onClick={() => void markRead(item)}>
                <strong>{item.subject}</strong>
                <span>{item.body}</span>
                <small>{new Date(item.createdAt).toLocaleString("vi-VN")}</small>
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
