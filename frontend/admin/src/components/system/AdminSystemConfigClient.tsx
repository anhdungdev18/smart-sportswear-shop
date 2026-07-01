"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { resendNotification, updateNotificationTemplate } from "@/modules/notifications/browser-api";
import type { NotificationResponse, NotificationTemplateResponse } from "@/modules/notifications/types";

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

export function AdminSystemConfigClient({
  initialTemplates,
  initialNotifications
}: {
  initialTemplates: NotificationTemplateResponse[];
  initialNotifications: NotificationResponse[];
}) {
  const [templates, setTemplates] = useState(initialTemplates);
  const [notifications, setNotifications] = useState(initialNotifications);
  const [message, setMessage] = useState<string | null>(null);
  const [savingKey, setSavingKey] = useState<string | null>(null);

  async function handleTemplateUpdate(template: NotificationTemplateResponse) {
    try {
      setSavingKey(`template:${template.id}`);
      setMessage(null);
      const updated = await updateNotificationTemplate(template.id, {
        subject: template.subject,
        body: template.body
      });
      setTemplates((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setMessage(`Đã cập nhật template ${updated.type}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được template"));
    } finally {
      setSavingKey(null);
    }
  }

  async function handleResend(id: string) {
    try {
      setSavingKey(`resend:${id}`);
      setMessage(null);
      const resent = await resendNotification(id);
      setNotifications((current) => [resent, ...current]);
      setMessage("Đã gửi lại thông báo.");
    } catch (error) {
      setMessage(extractError(error, "Không gửi lại được thông báo"));
    } finally {
      setSavingKey(null);
    }
  }

  return (
    <>
      <section className="card panel">
        <div className="panel-header">
          <h2>Mẫu thông báo</h2>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <table className="data-table">
          <thead>
            <tr>
              <th>Loại</th>
              <th>Kênh</th>
              <th>Tiêu đề</th>
              <th>Nội dung</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {templates.map((item) => (
              <tr key={item.id}>
                <td>{item.type}</td>
                <td>{item.channel}</td>
                <td>
                  <input
                    className="admin-input"
                    value={item.subject}
                    onChange={(event) =>
                      setTemplates((current) =>
                        current.map((template) => template.id === item.id ? { ...template, subject: event.target.value } : template)
                      )
                    }
                  />
                </td>
                <td>
                  <textarea
                    className="admin-textarea"
                    value={item.body}
                    onChange={(event) =>
                      setTemplates((current) =>
                        current.map((template) => template.id === item.id ? { ...template, body: event.target.value } : template)
                      )
                    }
                  />
                  <div className="table-subtle">{item.allowedPlaceholders.join(", ") || "-"}</div>
                </td>
                <td>
                  <button className="admin-btn" type="button" onClick={() => void handleTemplateUpdate(item)} disabled={savingKey === `template:${item.id}`}>
                    {savingKey === `template:${item.id}` ? "Đang lưu..." : "Lưu"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>Lịch sử gửi gần đây</h2>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Thời gian</th>
              <th>Người nhận</th>
              <th>Loại</th>
              <th>Trạng thái</th>
              <th>Gửi lại</th>
            </tr>
          </thead>
          <tbody>
            {notifications.map((item) => (
              <tr key={item.id}>
                <td>{new Date(item.createdAt).toLocaleString("vi-VN")}</td>
                <td>{item.recipient}</td>
                <td>{item.type}</td>
                <td>{item.status}</td>
                <td>
                  <button className="admin-btn secondary" type="button" onClick={() => void handleResend(item.id)} disabled={savingKey === `resend:${item.id}`}>
                    {savingKey === `resend:${item.id}` ? "Đang gửi..." : "Gửi lại"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
