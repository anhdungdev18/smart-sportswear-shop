"use client";

import { useMemo, useState } from "react";
import { addBannerItem, createBanner, deleteBannerItem, updateBanner, updateBannerItem } from "@/modules/banners/browser-api";
import type { BannerItemResponse, BannerResponse } from "@/modules/banners/types";
import { extractAdminError } from "@/modules/api/admin-errors";

const placements = ["HOME_HERO", "HOME_GRID", "COLLECTION_TOP", "SIDEBAR", "POPUP", "FOOTER"] as const;
const statuses = ["DRAFT", "ACTIVE", "INACTIVE"] as const;

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

function toBannerForm(banner: BannerResponse) {
  return {
    name: banner.name,
    code: banner.code,
    placement: banner.placement,
    status: banner.status,
    startsAt: toLocalDateTime(banner.startsAt),
    endsAt: toLocalDateTime(banner.endsAt)
  };
}

function toBannerItemForm(item: BannerItemResponse) {
  return {
    title: item.title ?? "",
    subtitle: item.subtitle ?? "",
    imageUrl: item.imageUrl,
    targetUrl: item.targetUrl ?? "",
    productId: item.productId ?? "",
    sortOrder: String(item.sortOrder),
    isActive: item.isActive
  };
}

function toBannerItemFormMap(items: BannerItemResponse[]) {
  return Object.fromEntries(items.map((item) => [item.id, toBannerItemForm(item)]));
}

const errorLabels = {
  code: "Mã banner",
  placement: "Vị trí hiển thị",
  startsAt: "Bắt đầu",
  endsAt: "Kết thúc",
  imageUrl: "Ảnh",
  targetUrl: "Liên kết đích",
  productId: "Sản phẩm"
};

export function AdminBannersClient({ initialItems }: { initialItems: BannerResponse[] }) {
  const initialSelected = initialItems[0] ?? null;
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialSelected?.id ?? "");
  const [form, setForm] = useState(() => (initialSelected ? toBannerForm(initialSelected) : createBannerForm()));
  const [itemForms, setItemForms] = useState<Record<string, ReturnType<typeof createItemForm>>>(() =>
    initialSelected ? toBannerItemFormMap(initialSelected.items) : {}
  );
  const [newItemForm, setNewItemForm] = useState(createItemForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState<string | null>(null);

  const selected = useMemo(() => items.find((item) => item.id === selectedId) ?? null, [items, selectedId]);

  function applySelectedBanner(banner: BannerResponse) {
    setSelectedId(banner.id);
    setForm(toBannerForm(banner));
    setItemForms(toBannerItemFormMap(banner.items));
    setNewItemForm(createItemForm());
  }

  function choose(banner: BannerResponse) {
    applySelectedBanner(banner);
    setMessage(null);
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

      const payload = {
        name: form.name,
        code: form.code,
        placement: form.placement,
        status: form.status,
        startsAt: toInstant(form.startsAt),
        endsAt: toInstant(form.endsAt)
      };

      if (selectedId) {
        const updated = await updateBanner(selectedId, payload);
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        applySelectedBanner(updated);
        setMessage(`Đã cập nhật banner ${updated.name}.`);
        return;
      }

      const created = await createBanner(payload);
      setItems((current) => [created, ...current]);
      applySelectedBanner(created);
      setMessage(`Đã tạo banner ${created.name}.`);
    } catch (error) {
      setMessage(extractAdminError(error, "Không lưu được banner", errorLabels));
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

      setItems((current) =>
        current.map((item) =>
          item.id === selectedId
            ? { ...item, items: [...item.items, created].sort((a, b) => a.sortOrder - b.sortOrder) }
            : item
        )
      );
      setItemForms((current) => ({ ...current, [created.id]: toBannerItemForm(created) }));
      setNewItemForm(createItemForm());
      setMessage("Đã thêm item banner.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không thêm được item banner", errorLabels));
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
            ? {
                ...item,
                items: item.items
                  .map((bannerItem) => (bannerItem.id === updated.id ? updated : bannerItem))
                  .sort((a, b) => a.sortOrder - b.sortOrder)
              }
            : item
        )
      );
      setItemForms((current) => ({ ...current, [updated.id]: toBannerItemForm(updated) }));
      setMessage("Đã cập nhật item banner.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không cập nhật được item banner", errorLabels));
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
      setItemForms((current) => {
        const next = { ...current };
        delete next[itemId];
        return next;
      });
      setMessage("Đã xóa item banner.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không xóa được item banner", errorLabels));
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
            <p className="panel-copy">Mỗi banner có thể chứa nhiều item hình ảnh để hiển thị ngoài storefront.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo banner
          </button>
        </div>

        {message ? <p className="action-message">{message}</p> : null}

        {items.length === 0 ? (
          <div className="empty-state">
            <h3>Chưa có banner nào</h3>
            <p>Hãy tạo banner đầu tiên để quản lý hero, grid hoặc popup cho storefront.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Tên</th>
                <th>Mã</th>
                <th>Vị trí</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => choose(item)}>
                  <td>{item.name}</td>
                  <td>{item.code}</td>
                  <td>{item.placement}</td>
                  <td>{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <h2>{selectedId ? "Cập nhật banner" : "Tạo banner mới"}</h2>
        </div>

        <div className="admin-form-grid">
          <input
            className="admin-input"
            placeholder="Tên banner"
            value={form.name}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
          />
          <input
            className="admin-input"
            placeholder="Mã banner"
            value={form.code}
            onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))}
          />
          <select
            className="select"
            value={form.placement}
            onChange={(event) => setForm((current) => ({ ...current, placement: event.target.value }))}
          >
            {placements.map((placement) => (
              <option key={placement} value={placement}>
                {placement}
              </option>
            ))}
          </select>
          <select
            className="select"
            value={form.status}
            onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
          >
            {statuses.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
          <input
            className="admin-input"
            type="datetime-local"
            value={form.startsAt}
            onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))}
          />
          <input
            className="admin-input"
            type="datetime-local"
            value={form.endsAt}
            onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))}
          />
        </div>

        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSaveBanner()} disabled={saving === "banner"}>
            {saving === "banner" ? "Đang lưu..." : selectedId ? "Cập nhật banner" : "Tạo banner"}
          </button>
        </div>

        <div className="panel-header" style={{ marginTop: 24 }}>
          <div>
            <h2>Item của banner</h2>
            <p className="panel-copy">Sắp xếp theo thứ tự hiển thị, mỗi item có thể trỏ tới link hoặc sản phẩm.</p>
          </div>
        </div>

        {!selected ? (
          <div className="empty-state compact">
            <p>Tạo hoặc chọn banner trước khi thêm item.</p>
          </div>
        ) : (
          <div className="stack-gap">
            {selected.items.length === 0 ? (
              <div className="empty-state compact">
                <p>Banner này chưa có item nào.</p>
              </div>
            ) : (
              selected.items.map((item) => {
                const draft = itemForms[item.id] ?? toBannerItemForm(item);

                return (
                  <div className="card panel" key={item.id}>
                    <div className="admin-form-grid">
                      <input
                        className="admin-input"
                        placeholder="Tiêu đề"
                        value={draft.title}
                        onChange={(event) =>
                          setItemForms((current) => ({
                            ...current,
                            [item.id]: { ...draft, title: event.target.value }
                          }))
                        }
                      />
                      <input
                        className="admin-input"
                        placeholder="Phụ đề"
                        value={draft.subtitle}
                        onChange={(event) =>
                          setItemForms((current) => ({
                            ...current,
                            [item.id]: { ...draft, subtitle: event.target.value }
                          }))
                        }
                      />
                      <input
                        className="admin-input"
                        placeholder="URL ảnh"
                        value={draft.imageUrl}
                        onChange={(event) =>
                          setItemForms((current) => ({
                            ...current,
                            [item.id]: { ...draft, imageUrl: event.target.value }
                          }))
                        }
                      />
                      <input
                        className="admin-input"
                        placeholder="URL đích"
                        value={draft.targetUrl}
                        onChange={(event) =>
                          setItemForms((current) => ({
                            ...current,
                            [item.id]: { ...draft, targetUrl: event.target.value }
                          }))
                        }
                      />
                      <input
                        className="admin-input"
                        placeholder="Product ID"
                        value={draft.productId}
                        onChange={(event) =>
                          setItemForms((current) => ({
                            ...current,
                            [item.id]: { ...draft, productId: event.target.value }
                          }))
                        }
                      />
                      <input
                        className="admin-input"
                        placeholder="Thứ tự"
                        value={draft.sortOrder}
                        onChange={(event) =>
                          setItemForms((current) => ({
                            ...current,
                            [item.id]: { ...draft, sortOrder: event.target.value }
                          }))
                        }
                      />
                      <label className="inline-check admin-form-full">
                        <input
                          type="checkbox"
                          checked={draft.isActive}
                          onChange={(event) =>
                            setItemForms((current) => ({
                              ...current,
                              [item.id]: { ...draft, isActive: event.target.checked }
                            }))
                          }
                        />
                        Item đang hiển thị
                      </label>
                    </div>
                    <div className="page-actions">
                      <button
                        className="admin-btn"
                        type="button"
                        onClick={() => void handleUpdateItem(item.id)}
                        disabled={saving === `banner-item:${item.id}`}
                      >
                        {saving === `banner-item:${item.id}` ? "Đang lưu..." : "Lưu item"}
                      </button>
                      <button
                        className="ghost-btn"
                        type="button"
                        onClick={() => void handleDeleteItem(item.id)}
                        disabled={saving === `banner-item-delete:${item.id}`}
                      >
                        {saving === `banner-item-delete:${item.id}` ? "Đang xóa..." : "Xóa item"}
                      </button>
                    </div>
                  </div>
                );
              })
            )}

            <div className="card panel">
              <div className="panel-header">
                <h2>Thêm item mới</h2>
              </div>
              <div className="admin-form-grid">
                <input
                  className="admin-input"
                  placeholder="Tiêu đề"
                  value={newItemForm.title}
                  onChange={(event) => setNewItemForm((current) => ({ ...current, title: event.target.value }))}
                />
                <input
                  className="admin-input"
                  placeholder="Phụ đề"
                  value={newItemForm.subtitle}
                  onChange={(event) => setNewItemForm((current) => ({ ...current, subtitle: event.target.value }))}
                />
                <input
                  className="admin-input"
                  placeholder="URL ảnh"
                  value={newItemForm.imageUrl}
                  onChange={(event) => setNewItemForm((current) => ({ ...current, imageUrl: event.target.value }))}
                />
                <input
                  className="admin-input"
                  placeholder="URL đích"
                  value={newItemForm.targetUrl}
                  onChange={(event) => setNewItemForm((current) => ({ ...current, targetUrl: event.target.value }))}
                />
                <input
                  className="admin-input"
                  placeholder="Product ID"
                  value={newItemForm.productId}
                  onChange={(event) => setNewItemForm((current) => ({ ...current, productId: event.target.value }))}
                />
                <input
                  className="admin-input"
                  placeholder="Thứ tự"
                  value={newItemForm.sortOrder}
                  onChange={(event) => setNewItemForm((current) => ({ ...current, sortOrder: event.target.value }))}
                />
                <label className="inline-check admin-form-full">
                  <input
                    type="checkbox"
                    checked={newItemForm.isActive}
                    onChange={(event) => setNewItemForm((current) => ({ ...current, isActive: event.target.checked }))}
                  />
                  Hiển thị ngay sau khi tạo
                </label>
              </div>
              <div className="page-actions">
                <button
                  className="admin-btn"
                  type="button"
                  onClick={() => void handleAddItem()}
                  disabled={saving === "banner-item-create"}
                >
                  {saving === "banner-item-create" ? "Đang thêm..." : "Thêm item"}
                </button>
              </div>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
