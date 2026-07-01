"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { createPage, fetchPageDetail, updatePage } from "@/modules/pages/browser-api";
import type { PageResponse } from "@/modules/pages/types";

const pageStatuses = ["DRAFT", "PUBLISHED", "ARCHIVED"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

function createEmptyForm() {
  return {
    title: "",
    slug: "",
    summary: "",
    contentHtml: "",
    status: "DRAFT"
  };
}

export function AdminPagesClient({ initialItems }: { initialItems: PageResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [loadingId, setLoadingId] = useState<string | null>(null);

  async function choose(item: PageResponse) {
    setSelectedId(item.id);
    setLoadingId(item.id);
    setMessage(null);

    try {
      const detail = await fetchPageDetail(item.id);
      setItems((current) => current.map((currentItem) => (currentItem.id === detail.id ? detail : currentItem)));
      setForm({
        title: detail.title,
        slug: detail.slug,
        summary: detail.summary ?? "",
        contentHtml: detail.contentHtml,
        status: detail.status
      });
    } catch (error) {
      setMessage(extractError(error, "Không tải được chi tiết trang nội dung"));
      setForm({
        title: item.title,
        slug: item.slug,
        summary: item.summary ?? "",
        contentHtml: item.contentHtml,
        status: item.status
      });
    } finally {
      setLoadingId(null);
    }
  }

  function startCreate() {
    setSelectedId("");
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo trang nội dung mới.");
  }

  async function handleSave() {
    try {
      setSaving(true);
      setMessage(null);

      if (selectedId) {
        const updated = await updatePage(selectedId, {
          title: form.title,
          slug: form.slug,
          summary: form.summary || null,
          contentHtml: form.contentHtml,
          status: form.status
        });
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        setMessage(`Đã cập nhật trang ${updated.title}.`);
        return;
      }

      const created = await createPage({
        title: form.title,
        slug: form.slug,
        summary: form.summary || null,
        contentHtml: form.contentHtml,
        status: form.status
      });
      setItems((current) => [created, ...current]);
      setSelectedId(created.id);
      setMessage(`Đã tạo trang ${created.title}.`);
    } catch (error) {
      setMessage(extractError(error, "Không lưu được trang nội dung"));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách trang nội dung</h2>
            <p className="panel-copy">Quản lý trang giới thiệu, chính sách và nội dung tĩnh từ backend.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>Tạo trang</button>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Tiêu đề</th>
              <th>Slug</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => void choose(item)}>
                <td>{item.title}</td>
                <td>{item.slug}</td>
                <td>{loadingId === item.id ? "Đang tải..." : item.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>{selectedId ? "Cập nhật trang" : "Tạo trang mới"}</h2>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Tiêu đề" value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} />
          <input className="admin-input" placeholder="Slug" value={form.slug} onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))} />
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>
            {pageStatuses.map((status) => (
              <option value={status} key={status}>{status}</option>
            ))}
          </select>
          <div className="admin-form-full">
            <input className="admin-input" placeholder="Tóm tắt ngắn" value={form.summary} onChange={(event) => setForm((current) => ({ ...current, summary: event.target.value }))} />
          </div>
          <div className="admin-form-full">
            <textarea className="admin-textarea" placeholder="Nội dung HTML" value={form.contentHtml} onChange={(event) => setForm((current) => ({ ...current, contentHtml: event.target.value }))} />
          </div>
        </div>
        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSave()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật trang" : "Tạo trang"}
          </button>
        </div>
      </section>
    </div>
  );
}
