"use client";

import { useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import {
  addBannerItem,
  createBanner,
  deleteBannerItem,
  fetchBannerDetail,
  updateBanner,
  updateBannerItem
} from "@/modules/banners/browser-api";
import type { BannerResponse } from "@/modules/banners/types";

const placements = ["HOME_HERO", "HOME_GRID", "COLLECTION_TOP", "SIDEBAR", "POPUP", "FOOTER"] as const;
const statuses = ["DRAFT", "ACTIVE", "INACTIVE"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

function toLocalDateTime(value: string | null) {
  return value ? new Date(value).toISOString().slice(0, 16) : "";
}

function toInstant(value: string) {
  return value ? new Date(value).toISOString() : null;
}

function createBannerForm() {
  return {
    name: "",
    code: "",
    placement: "HOME_HERO",
    status: "DRAFT",
    startsAt: "",
    endsAt: ""
  };
}

function createItemForm() {
  return {
    title: "",
    subtitle: "",
    imageUrl: "",
    targetUrl: "",
    productId: "",
    sortOrder: "0",
    isActive: true
  };
}

export function AdminBannersClient({ initialItems }: { initialItems: BannerResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createBannerForm());
  const [itemForms, setItemForms] = useState<Record<string, ReturnType<typeof createItemForm>>>({});
  const [newItemForm, setNewItemForm] = useState(createItemForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState<string | null>(null);
  const [loadingId, setLoadingId] = useState<string | null>(null);

  const selected = items.find((item) => item.id === selectedId) ?? null;

  async function choose(item: BannerResponse) {
    setSelectedId(item.id);
    setLoadingId(item.id);
    setMessage(null);

    const applyBanner = (banner: BannerResponse) => {
      setForm({
        name: banner.name,
        code: banner.code,
        placement: banner.placement,
        status: banner.status,
        startsAt: toLocalDateTime(banner.startsAt),
        endsAt: toLocalDateTime(banner.endsAt)
      });
      setItemForms(
        Object.fromEntries(
          banner.items.map((bannerItem) => [
            bannerItem.id,
            {
              title: bannerItem.title ?? "",
              subtitle: bannerItem.subtitle ?? "",
              imageUrl: bannerItem.imageUrl,
              targetUrl: bannerItem.targetUrl ?? "",
              productId: bannerItem.productId ?? "",
              sortOrder: String(bannerItem.sortOrder),
              isActive: bannerItem.isActive
            }
          ])
        )
      );
    };

    try {
      const detail = await fetchBannerDetail(item.id);
      setItems((current) => current.map((currentItem) => (currentItem.id === detail.id ? detail : currentItem)));
      applyBanner(detail);
    } catch (error) {
      setMessage(extractError(error, "Không tải được chi tiết banner"));
      applyBanner(item);
    } finally {
      setLoadingId(null);
    }
  }

  function startCreate() {
    setSelectedId("");
    setForm(createBannerForm());
    setItemForms({});
    setNewItemForm(createItemForm());
    setMessage("Bạn đang tạo banner mới.");
  }

  async function handleSaveBanner() {
    try {
      setSaving("banner");
      setMessage(null);

      if (selectedId) {
        const updated = await updateBanner(selectedId, {
          name: form.name,
          code: form.code,
          placement: form.placement,
          status: form.status,
          startsAt: toInstant(form.startsAt),
          endsAt: toInstant(form.endsAt)
        });
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        choose(updated);
        setMessage(`Đã cập nhật banner ${updated.name}.`);
        return;
      }

      const created = await createBanner({
        name: form.name,
        code: form.code,
        placement: form.placement,
        status: form.status,
        startsAt: toInstant(form.startsAt),
        endsAt: toInstant(form.endsAt)
      });
      setItems((current) => [created, ...current]);
      choose(created);
      setMessage(`Đã tạo banner ${created.name}.`);
    } catch (error) {
      setMessage(extractError(error, "Không lưu được banner"));
    } finally {
      setSaving(null);
    }
  }

  async function handleAddItem() {
    if (!selectedId) {
      setMessage("Hãy tạo banner trước khi thêm item.");
      return;
    }

    try {
      setSaving("banner-item-create");
      setMessage(null);
      const created = await addBannerItem(selectedId, {
        title: newItemForm.title || null,
        subtitle: newItemForm.subtitle || null,
        imageUrl: newItemForm.imageUrl,
        targetUrl: newItemForm.targetUrl || null,
        productId: newItemForm.productId || null,
        sortOrder: Number(newItemForm.sortOrder || 0),
        isActive: newItemForm.isActive
      });
      const next = items.map((item) =>
        item.id === selectedId ? { ...item, items: [...item.items, created].sort((a, b) => a.sortOrder - b.sortOrder) } : item
      );
      setItems(next);
      setNewItemForm(createItemForm());
      setMessage("Đã thêm item banner.");
    } catch (error) {
      setMessage(extractError(error, "Không thêm được item banner"));
    } finally {
      setSaving(null);
    }
  }

  async function handleUpdateItem(itemId: string) {
    const draft = itemForms[itemId];
    if (!draft) {
      return;
    }

    try {
      setSaving(`banner-item:${itemId}`);
      setMessage(null);
      const updated = await updateBannerItem(itemId, {
        title: draft.title || null,
        subtitle: draft.subtitle || null,
        imageUrl: draft.imageUrl,
        targetUrl: draft.targetUrl || null,
        productId: draft.productId || null,
        sortOrder: Number(draft.sortOrder || 0),
        isActive: draft.isActive
      });
      setItems((current) =>
        current.map((item) =>
          item.id === updated.bannerId
            ? { ...item, items: item.items.map((bannerItem) => (bannerItem.id === updated.id ? updated : bannerItem)) }
            : item
        )
      );
      setMessage("Đã cập nhật item banner.");
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được item banner"));
    } finally {
      setSaving(null);
    }
  }

  async function handleDeleteItem(itemId: string) {
    try {
      setSaving(`banner-item-delete:${itemId}`);
      setMessage(null);
      await deleteBannerItem(itemId);
      setItems((current) =>
        current.map((item) => ({
          ...item,
          items: item.items.filter((bannerItem) => bannerItem.id !== itemId)
        }))
      );
      setMessage("Đã xóa item banner.");
    } catch (error) {
      setMessage(extractError(error, "Không xóa được item banner"));
    } finally {
      setSaving(null);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách banner</h2>
            <p className="panel-copy">Quản lý banner hiển thị ngoài storefront.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>Tạo banner</button>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Tên</th>
              <th>Code</th>
              <th>Placement</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => void choose(item)}>
                <td>{item.name}</td>
                <td>{item.code}</td>
                <td>{item.placement}</td>
                <td>{loadingId === item.id ? "Đang tải..." : item.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <div className="admin-stack">
        <section className="card panel">
          <div className="panel-header">
            <h2>{selectedId ? "Cập nhật banner" : "Tạo banner mới"}</h2>
          </div>
          {message ? <p className="action-message">{message}</p> : null}
          <div className="admin-form-grid">
            <input className="admin-input" placeholder="Tên banner" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
            <input className="admin-input" placeholder="Code" value={form.code} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} />
            <select className="select" value={form.placement} onChange={(event) => setForm((current) => ({ ...current, placement: event.target.value }))}>
              {placements.map((item) => <option value={item} key={item}>{item}</option>)}
            </select>
            <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>
              {statuses.map((item) => <option value={item} key={item}>{item}</option>)}
            </select>
            <input className="admin-input" type="datetime-local" value={form.startsAt} onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))} />
            <input className="admin-input" type="datetime-local" value={form.endsAt} onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))} />
          </div>
          <div className="page-actions">
            <button className="admin-btn" type="button" onClick={() => void handleSaveBanner()} disabled={saving === "banner"}>
              {saving === "banner" ? "Đang lưu..." : selectedId ? "Cập nhật banner" : "Tạo banner"}
            </button>
          </div>
        </section>

        <section className="card panel">
          <div className="panel-header">
            <h2>Item banner</h2>
          </div>
          <div className="admin-form-grid">
            <input className="admin-input" placeholder="Tiêu đề" value={newItemForm.title} onChange={(event) => setNewItemForm((current) => ({ ...current, title: event.target.value }))} />
            <input className="admin-input" placeholder="Phụ đề" value={newItemForm.subtitle} onChange={(event) => setNewItemForm((current) => ({ ...current, subtitle: event.target.value }))} />
            <div className="admin-form-full">
              <input className="admin-input" placeholder="Image URL" value={newItemForm.imageUrl} onChange={(event) => setNewItemForm((current) => ({ ...current, imageUrl: event.target.value }))} />
            </div>
            <input className="admin-input" placeholder="Target URL" value={newItemForm.targetUrl} onChange={(event) => setNewItemForm((current) => ({ ...current, targetUrl: event.target.value }))} />
            <input className="admin-input" placeholder="Product ID (nếu có)" value={newItemForm.productId} onChange={(event) => setNewItemForm((current) => ({ ...current, productId: event.target.value }))} />
            <input className="admin-input" type="number" min={0} placeholder="Sort order" value={newItemForm.sortOrder} onChange={(event) => setNewItemForm((current) => ({ ...current, sortOrder: event.target.value }))} />
            <label className="admin-check">
              <input type="checkbox" checked={newItemForm.isActive} onChange={(event) => setNewItemForm((current) => ({ ...current, isActive: event.target.checked }))} />
              Item đang hoạt động
            </label>
          </div>
          <div className="page-actions">
            <button className="admin-btn secondary" type="button" onClick={() => void handleAddItem()} disabled={saving === "banner-item-create"}>
              {saving === "banner-item-create" ? "Đang thêm..." : "Thêm item"}
            </button>
          </div>

          {selected?.items.length ? (
            <div className="admin-stack">
              {selected.items.map((item) => {
                const draft = itemForms[item.id] ?? createItemForm();
                return (
                  <div className="admin-subcard" key={item.id}>
                    <div className="admin-image-card">
                      <img src={draft.imageUrl || item.imageUrl} alt={draft.title || item.title || selected.name} />
                      <div>
                        <strong>{item.title || "Item banner"}</strong>
                        <div className="table-subtle">{item.targetUrl || "Không có target URL"}</div>
                      </div>
                    </div>
                    <div className="admin-form-grid">
                      <input className="admin-input" value={draft.title} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, title: event.target.value } }))} />
                      <input className="admin-input" value={draft.subtitle} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, subtitle: event.target.value } }))} />
                      <div className="admin-form-full">
                        <input className="admin-input" value={draft.imageUrl} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, imageUrl: event.target.value } }))} />
                      </div>
                      <input className="admin-input" value={draft.targetUrl} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, targetUrl: event.target.value } }))} />
                      <input className="admin-input" value={draft.productId} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, productId: event.target.value } }))} />
                      <input className="admin-input" type="number" min={0} value={draft.sortOrder} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, sortOrder: event.target.value } }))} />
                      <label className="admin-check">
                        <input type="checkbox" checked={draft.isActive} onChange={(event) => setItemForms((current) => ({ ...current, [item.id]: { ...draft, isActive: event.target.checked } }))} />
                        Hoạt động
                      </label>
                    </div>
                    <div className="page-actions">
                      <button className="admin-btn secondary" type="button" onClick={() => void handleUpdateItem(item.id)} disabled={saving === `banner-item:${item.id}`}>{saving === `banner-item:${item.id}` ? "Đang lưu..." : "Lưu item"}</button>
                      <button className="admin-btn secondary" type="button" onClick={() => void handleDeleteItem(item.id)} disabled={saving === `banner-item-delete:${item.id}`}>{saving === `banner-item-delete:${item.id}` ? "Đang xóa..." : "Xóa item"}</button>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="empty-state">Banner này chưa có item nào.</div>
          )}
        </section>
      </div>
    </div>
  );
}
