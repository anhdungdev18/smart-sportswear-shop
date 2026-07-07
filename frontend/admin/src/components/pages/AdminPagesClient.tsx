"use client";

import { useMemo, useState } from "react";
import { extractAdminError } from "@/modules/api/admin-errors";
import { createPage, updatePage } from "@/modules/pages/browser-api";
import type { PageResponse } from "@/modules/pages/types";
import { toSlug } from "@/modules/utils/slug";

const pageStatuses = ["DRAFT", "PUBLISHED", "ARCHIVED"] as const;

function createEmptyForm() {
  return {
    title: "",
    slug: "",
    summary: "",
    contentHtml: "",
    status: "DRAFT"
  };
}

function toPageForm(page: PageResponse) {
  return {
    title: page.title,
    slug: page.slug,
    summary: page.summary ?? "",
    contentHtml: page.contentHtml,
    status: page.status
  };
}

const errorLabels = {
  title: "Tiêu đề",
  summary: "Tóm tắt",
  contentHtml: "Nội dung HTML"
};

export function AdminPagesClient({ initialItems }: { initialItems: PageResponse[] }) {
  const initialSelected = initialItems[0] ?? null;
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialSelected?.id ?? "");
  const [form, setForm] = useState(() => (initialSelected ? toPageForm(initialSelected) : createEmptyForm()));
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [slugDirty, setSlugDirty] = useState(Boolean(initialSelected));

  const selected = useMemo(() => items.find((item) => item.id === selectedId) ?? null, [items, selectedId]);

  function applySelectedPage(page: PageResponse) {
    setSelectedId(page.id);
    setSlugDirty(true);
    setForm(toPageForm(page));
  }

  function choose(page: PageResponse) {
    applySelectedPage(page);
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setSlugDirty(false);
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo trang nội dung mới.");
  }

  async function handleSave() {
    try {
      setSaving(true);
      setMessage(null);

      const payload = {
        title: form.title,
        slug: form.slug,
        summary: form.summary || null,
        contentHtml: form.contentHtml,
        status: form.status
      };

      if (selectedId) {
        const updated = await updatePage(selectedId, payload);
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        applySelectedPage(updated);
        setMessage(`Đã cập nhật trang ${updated.title}.`);
        return;
      }

      const created = await createPage(payload);
      setItems((current) => [created, ...current]);
      applySelectedPage(created);
      setMessage(`Đã tạo trang ${created.title}.`);
    } catch (error) {
      setMessage(extractAdminError(error, "Không lưu được trang nội dung", errorLabels));
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
            <p className="panel-copy">Quản lý các trang giới thiệu, chính sách và landing page tĩnh từ backend.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo trang
          </button>
        </div>

        {message ? <p className="action-message">{message}</p> : null}

        {items.length === 0 ? (
          <div className="empty-state">
            <h3>Chưa có trang nội dung nào</h3>
            <p>Hãy tạo trang đầu tiên để quản lý nội dung public như giới thiệu hoặc chính sách.</p>
          </div>
        ) : (
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
                <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => choose(item)}>
                  <td>{item.title}</td>
                  <td>{item.slug}</td>
                  <td>{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>{selected ? "Cập nhật trang" : "Tạo trang mới"}</h2>
        </div>
        <div className="admin-form-grid">
          <input
            className="admin-input"
            placeholder="Tiêu đề"
            value={form.title}
            onChange={(event) => {
              const title = event.target.value;
              setForm((current) => ({ ...current, title, ...(!slugDirty && { slug: toSlug(title) }) }));
            }}
          />
          <input
            className="admin-input"
            placeholder="Slug"
            value={form.slug}
            onChange={(event) => {
              setSlugDirty(true);
              setForm((current) => ({ ...current, slug: event.target.value }));
            }}
          />
          <select
            className="select"
            value={form.status}
            onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
          >
            {pageStatuses.map((status) => (
              <option value={status} key={status}>
                {status}
              </option>
            ))}
          </select>
          <div className="admin-form-full">
            <input
              className="admin-input"
              placeholder="Tóm tắt ngắn"
              value={form.summary}
              onChange={(event) => setForm((current) => ({ ...current, summary: event.target.value }))}
            />
          </div>
          <div className="admin-form-full">
            <textarea
              className="admin-textarea"
              placeholder="Nội dung HTML"
              value={form.contentHtml}
              onChange={(event) => setForm((current) => ({ ...current, contentHtml: event.target.value }))}
            />
          </div>
        </div>
        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSave()} disabled={saving}>
            {saving ? "Đang lưu..." : selected ? "Cập nhật trang" : "Tạo trang"}
          </button>
        </div>
      </section>
    </div>
  );
}
