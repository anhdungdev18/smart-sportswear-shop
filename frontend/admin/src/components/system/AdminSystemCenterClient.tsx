"use client";

import { useDeferredValue, useMemo, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { resendNotification, updateNotificationTemplate } from "@/modules/notifications/browser-api";
import type { NotificationResponse, NotificationTemplateResponse } from "@/modules/notifications/types";
import { upsertSiteSetting } from "@/modules/settings/browser-api";
import type { SiteSettingResponse } from "@/modules/settings/types";

const settingValueTypes = ["STRING", "NUMBER", "BOOLEAN", "JSON"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

export function AdminSystemCenterClient({
  initialTemplates,
  initialNotifications,
  initialSettings
}: {
  initialTemplates: NotificationTemplateResponse[];
  initialNotifications: NotificationResponse[];
  initialSettings: SiteSettingResponse[];
}) {
  const [templates, setTemplates] = useState(initialTemplates);
  const [notifications, setNotifications] = useState(initialNotifications);
  const [settings, setSettings] = useState(initialSettings);
  const [message, setMessage] = useState<string | null>(null);
  const [savingKey, setSavingKey] = useState<string | null>(null);
  const [notificationSearch, setNotificationSearch] = useState("");
  const deferredNotificationSearch = useDeferredValue(notificationSearch);

  const filteredNotifications = useMemo(() => {
    const query = deferredNotificationSearch.trim().toLowerCase();
    if (!query) return notifications;
    return notifications.filter((item) => [item.recipient, item.type, item.status, item.subject].join(" ").toLowerCase().includes(query));
  }, [deferredNotificationSearch, notifications]);

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

  async function handleSettingSave(setting: SiteSettingResponse) {
    try {
      setSavingKey(`setting:${setting.id}`);
      setMessage(null);
      const updated = await upsertSiteSetting(setting.settingKey, {
        settingValue: setting.settingValue,
        valueType: setting.valueType,
        description: setting.description,
        isPublic: setting.isPublic
      });
      setSettings((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setMessage(`Đã lưu cấu hình ${updated.settingKey}.`);
    } catch (error) {
      setMessage(extractError(error, "Không lưu được cấu hình hệ thống"));
    } finally {
      setSavingKey(null);
    }
  }

  return (
    <>
      <section className="card panel">
        <div className="panel-header">
          <h2>Cấu hình hệ thống</h2>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <table className="data-table">
          <thead>
            <tr>
              <th>Key</th>
              <th>Giá trị</th>
              <th>Kiểu</th>
              <th>Mô tả</th>
              <th>Public</th>
              <th>Lưu</th>
            </tr>
          </thead>
          <tbody>
            {settings.map((item) => (
              <tr key={item.id}>
                <td>{item.settingKey}</td>
                <td>
                  <input
                    className="admin-input"
                    value={item.settingValue ?? ""}
                    onChange={(event) =>
                      setSettings((current) =>
                        current.map((setting) => (setting.id === item.id ? { ...setting, settingValue: event.target.value } : setting))
                      )
                    }
                  />
                </td>
                <td>
                  <select
                    className="select"
                    value={item.valueType}
                    onChange={(event) =>
                      setSettings((current) =>
                        current.map((setting) => (setting.id === item.id ? { ...setting, valueType: event.target.value } : setting))
                      )
                    }
                  >
                    {settingValueTypes.map((type) => (
                      <option value={type} key={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <input
                    className="admin-input"
                    value={item.description ?? ""}
                    onChange={(event) =>
                      setSettings((current) =>
                        current.map((setting) => (setting.id === item.id ? { ...setting, description: event.target.value } : setting))
                      )
                    }
                  />
                </td>
                <td>
                  <label className="admin-check">
                    <input
                      type="checkbox"
                      checked={item.isPublic}
                      onChange={(event) =>
                        setSettings((current) =>
                          current.map((setting) => (setting.id === item.id ? { ...setting, isPublic: event.target.checked } : setting))
                        )
                      }
                    />
                    Public
                  </label>
                </td>
                <td>
                  <button className="admin-btn" type="button" onClick={() => void handleSettingSave(item)} disabled={savingKey === `setting:${item.id}`}>
                    {savingKey === `setting:${item.id}` ? "Đang lưu..." : "Lưu"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>Mẫu thông báo</h2>
        </div>
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
                        current.map((template) => (template.id === item.id ? { ...template, subject: event.target.value } : template))
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
                        current.map((template) => (template.id === item.id ? { ...template, body: event.target.value } : template))
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
          <div>
            <h2>Lịch sử gửi gần đây</h2>
            <p className="panel-copy">Theo dõi trạng thái gửi thực tế và hỗ trợ gửi lại nhanh khi cần.</p>
          </div>
          <input
            className="admin-input"
            style={{ width: 280 }}
            placeholder="Tìm theo người nhận, loại hoặc tiêu đề..."
            value={notificationSearch}
            onChange={(event) => setNotificationSearch(event.target.value)}
          />
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
            {filteredNotifications.length === 0 ? (
              <tr>
                <td colSpan={5} style={{ textAlign: "center", color: "var(--admin-muted)" }}>
                  Không có thông báo nào khớp bộ lọc hiện tại.
                </td>
              </tr>
            ) : null}
            {filteredNotifications.map((item) => (
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
