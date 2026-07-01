"use client";

import type { AuditLogResponse } from "@/modules/audit/types";

export function AdminAuditLogsClient({ initialLogs }: { initialLogs: AuditLogResponse[] }) {
  return (
    <section className="card panel">
      <div className="panel-header">
        <div>
          <h2>Nhật ký thao tác</h2>
          <p className="panel-copy">Theo dõi hành động quản trị để audit và truy vết thay đổi.</p>
        </div>
      </div>
      <table className="data-table">
        <thead>
          <tr>
            <th>Thời gian</th>
            <th>Người thao tác</th>
            <th>Action</th>
            <th>Entity</th>
            <th>IP</th>
          </tr>
        </thead>
        <tbody>
          {initialLogs.map((item) => (
            <tr key={item.id}>
              <td>{new Date(item.createdAt).toLocaleString("vi-VN")}</td>
              <td>
                <strong>{item.actorName ?? "Không rõ"}</strong>
                <div className="table-subtle">{item.actorUserId ?? "-"}</div>
              </td>
              <td>{item.action}</td>
              <td>
                {item.entityType}
                <div className="table-subtle">{item.entityId ?? "-"}</div>
              </td>
              <td>{item.ipAddress ?? "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
