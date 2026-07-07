import { AdminSystemCenterClient } from "@/components/system/AdminSystemCenterClient";
import { listNotificationTemplates, listAdminNotifications } from "@/modules/notifications/api";
import { listAdminSettings } from "@/modules/settings/api";

export default async function SystemConfigPage() {
  const [templates, notifications, settings] = await Promise.all([
    listNotificationTemplates().catch(() => []),
    listAdminNotifications().catch(() => []),
    listAdminSettings().catch(() => [])
  ]);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Hệ thống</h1>
          <p>Quản lý cấu hình hệ thống, mẫu thông báo giao dịch và lịch sử gửi thực tế.</p>
        </div>
      </section>

      <AdminSystemCenterClient initialTemplates={templates} initialNotifications={notifications} initialSettings={settings} />
    </main>
  );
}
