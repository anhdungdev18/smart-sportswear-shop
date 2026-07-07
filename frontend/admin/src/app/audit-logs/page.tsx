import { AdminAuditLogsClient } from "@/components/audit/AdminAuditLogsClient";
import { listAuditLogs } from "@/modules/audit/api";

export default async function AuditLogsPage() {
  const logs = await listAuditLogs().catch(() => []);

  return (
    <main className="workspace">
      <section className="page-title">
        <div>
          <h1>Audit logs</h1>
          <p>Xem lịch sử hành động quản trị để kiểm tra thay đổi và truy vết sự cố.</p>
        </div>
      </section>

      <AdminAuditLogsClient initialLogs={logs} />
    </main>
  );
}
