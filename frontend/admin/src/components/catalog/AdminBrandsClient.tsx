"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { createBrand, updateBrand } from "@/modules/catalog-admin/browser-api";
import type { BrandResponse } from "@/modules/catalog-admin/types";

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

function createEmptyForm() {
  return {
    name: "",
    slug: "",
    description: "",
    status: "ACTIVE"
  };
}

export function AdminBrandsClient({ initialItems }: { initialItems: BrandResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  function handleSelect(item: BrandResponse) {
    setSelectedId(item.id);
    setForm({
      name: item.name,
      slug: item.slug,
      description: item.description ?? "",
      status: item.status
    });
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo thương hiệu mới.");
  }

  async function handleSubmit() {
    try {
      setSaving(true);
      setMessage(null);

      if (selectedId) {
        const updated = await updateBrand(selectedId, {
          name: form.name,
          slug: form.slug,
          description: form.description || null,
          status: form.status
        });
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        setMessage(`Đã cập nhật thương hiệu ${updated.name}.`);
        return;
      }

      const created = await createBrand({
        name: form.name,
        slug: form.slug,
        description: form.description || null,
        status: form.status
      });
      setItems((current) => [created, ...current]);
      setSelectedId(created.id);
      setMessage(`Đã tạo thương hiệu ${created.name}.`);
    } catch (error) {
      setMessage(extractError(error, "Không lưu được thương hiệu"));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách thương hiệu</h2>
            <p className="panel-copy">Quản lý brand dùng chung cho storefront và admin.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo thương hiệu
          </button>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Tên</th>
              <th>Slug</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => handleSelect(item)}>
                <td>{item.name}</td>
                <td>{item.slug}</td>
                <td>{item.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedId ? "Cập nhật thương hiệu" : "Tạo thương hiệu mới"}</h2>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Tên thương hiệu" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
          <input className="admin-input" placeholder="Slug" value={form.slug} onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))} />
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
          <div className="admin-form-full">
            <textarea className="admin-textarea" placeholder="Mô tả" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} />
          </div>
        </div>
        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật thương hiệu" : "Tạo thương hiệu"}
          </button>
        </div>
      </section>
    </div>
  );
}
