"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { fetchAdminUserDetail, updateAdminUserRole, updateAdminUserStatus } from "@/modules/users/browser-api";
import type { AdminUserResponse } from "@/modules/users/types";

const roles = ["CUSTOMER", "SALES_STAFF", "WAREHOUSE_STAFF", "ADMIN"] as const;
const statuses = ["ACTIVE", "LOCKED", "INACTIVE"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

export function AdminUsersClient({
  initialUsers,
  mode
}: {
  initialUsers: AdminUserResponse[];
  mode: "customers" | "roles";
}) {
  const [users, setUsers] = useState(initialUsers);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [selectedUser, setSelectedUser] = useState<AdminUserResponse | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [savingKey, setSavingKey] = useState<string | null>(null);
  const [loadingDetailId, setLoadingDetailId] = useState<string | null>(null);

  async function handleSelect(user: AdminUserResponse) {
    setSelectedUserId(user.id);
    setLoadingDetailId(user.id);
    setMessage(null);

    try {
      const detail = await fetchAdminUserDetail(user.id);
      setUsers((current) => current.map((item) => (item.id === detail.id ? detail : item)));
      setSelectedUser(detail);
    } catch (error) {
      setMessage(extractError(error, "Không tải được chi tiết người dùng"));
      setSelectedUser(user);
    } finally {
      setLoadingDetailId(null);
    }
  }

  async function handleRole(id: string, role: string) {
    try {
      setSavingKey(`${id}:role`);
      setMessage(null);
      const updated = await updateAdminUserRole(id, role);
      setUsers((current) => current.map((item) => (item.id === id ? updated : item)));
      setMessage(`Đã cập nhật vai trò cho ${updated.fullName}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được vai trò"));
    } finally {
      setSavingKey(null);
    }
  }

  async function handleStatus(id: string, status: string) {
    try {
      setSavingKey(`${id}:status`);
      setMessage(null);
      const updated = await updateAdminUserStatus(id, status);
      setUsers((current) => current.map((item) => (item.id === id ? updated : item)));
      setMessage(`Đã cập nhật trạng thái cho ${updated.fullName}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được trạng thái"));
    } finally {
      setSavingKey(null);
    }
  }

  return (
    <section className="card panel">
      <div className="panel-header">
        <h2>{mode === "customers" ? "Danh sách người dùng" : "Người dùng & vai trò"}</h2>
      </div>
      {message ? <p className="action-message">{message}</p> : null}
      <table className="data-table">
        <thead>
          <tr>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Vai trò</th>
            <th>Trạng thái</th>
            <th>Cập nhật</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr
              key={user.id}
              className={selectedUserId === user.id ? "row-selected" : ""}
              onClick={() => void handleSelect(user)}
            >
              <td>
                <strong>{user.fullName}</strong>
                <div className="table-subtle">{user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString("vi-VN") : "Chưa đăng nhập"}</div>
              </td>
              <td>{user.email}</td>
              <td>{user.role}</td>
              <td>{user.status}</td>
              <td>
                <div className="admin-inline-form">
                  <select className="select" defaultValue={user.role} onChange={(event) => void handleRole(user.id, event.target.value)}>
                    {roles.map((role) => (
                      <option value={role} key={role}>{role}</option>
                    ))}
                  </select>
                  <select className="select" defaultValue={user.status} onChange={(event) => void handleStatus(user.id, event.target.value)}>
                    {statuses.map((status) => (
                      <option value={status} key={status}>{status}</option>
                    ))}
                  </select>
                  <span className="table-subtle">
                    {loadingDetailId === user.id ? "Đang tải..." : (savingKey === `${user.id}:role` || savingKey === `${user.id}:status`) ? "Đang lưu..." : "Sẵn sàng"}
                  </span>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selectedUser ? (
        <div className="admin-subcard admin-subcard-tight">
          <strong>Chi tiết người dùng</strong>
          <div className="table-subtle">ID: {selectedUser.id}</div>
          <div className="table-subtle">Số điện thoại: {selectedUser.phone ?? "Chưa cập nhật"}</div>
          <div className="table-subtle">Ngày tạo: {new Date(selectedUser.createdAt).toLocaleString("vi-VN")}</div>
          <div className="table-subtle">
            Đăng nhập gần nhất: {selectedUser.lastLoginAt ? new Date(selectedUser.lastLoginAt).toLocaleString("vi-VN") : "Chưa có"}
          </div>
        </div>
      ) : null}
    </section>
  );
}
