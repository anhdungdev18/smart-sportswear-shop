"use client";

import { useEffect, useMemo, useState } from "react";
import { extractAdminError } from "@/modules/api/admin-errors";
import { listAllProductsForPicker } from "@/modules/catalog-admin/browser-api";
import type { ProductPickItem } from "@/modules/catalog-admin/browser-api";
import {
  createPromotion,
  deletePromotion,
  listPromotions,
  updatePromotion,
  updatePromotionStatus
} from "@/modules/promotions/browser-api";
import type { PromotionResponse, PromotionStatus } from "@/modules/promotions/types";

type ProductOption = {
  id: string;
  name: string;
  meta: string;
};

function emptyForm() {
  return {
    name: "",
    description: "",
    discountPercent: "10",
    startsAt: "",
    endsAt: "",
    productIds: [] as string[]
  };
}

function toLocalInput(value: string | null) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const offsetMs = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16);
}

function toInstant(value: string) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

function formatDateTime(value: string | null) {
  if (!value) return "Không giới hạn";
  return new Date(value).toLocaleString("vi-VN", { dateStyle: "short", timeStyle: "short" });
}

function statusLabel(status: PromotionStatus) {
  if (status === "ACTIVE") return "Đang bật";
  if (status === "INACTIVE") return "Tạm dừng";
  if (status === "EXPIRED") return "Hết hạn";
  return "Nháp";
}

function statusTone(item: PromotionResponse) {
  if (item.live) return "success";
  if (item.status === "ACTIVE") return "warning";
  return "muted";
}

function productMeta(item: ProductPickItem) {
  const category = item.category?.name ?? item.categoryName ?? "Chưa phân loại";
  const brand = item.brand?.name ?? item.brandName ?? "Chưa có thương hiệu";
  return `${category} · ${brand}`;
}

function toPromotionForm(item: PromotionResponse) {
  return {
    name: item.name,
    description: item.description ?? "",
    discountPercent: String(item.discountPercent),
    startsAt: toLocalInput(item.startsAt),
    endsAt: toLocalInput(item.endsAt),
    productIds: item.productIds
  };
}

export function AdminPromotionsClient({ initialItems }: { initialItems: PromotionResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [products, setProducts] = useState<ProductOption[]>([]);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(() => {
    const selected = initialItems[0];
    return selected ? toPromotionForm(selected) : emptyForm();
  });
  const [query, setQuery] = useState("");
  const [productQuery, setProductQuery] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    async function loadInitialData() {
      try {
        const [promotionList, productList] = await Promise.all([listPromotions(), listAllProductsForPicker()]);
        if (ignore) return;
        setItems(promotionList);
        setSelectedId((current) => {
          if (current || !promotionList[0]) return current;
          setForm(toPromotionForm(promotionList[0]));
          return promotionList[0].id;
        });
        setProducts(productList.map((item) => ({ id: item.id, name: item.name, meta: productMeta(item) })));
      } catch (error) {
        if (!ignore) setMessage(extractAdminError(error, "Không tải được dữ liệu khuyến mãi."));
      }
    }
    void loadInitialData();
    return () => {
      ignore = true;
    };
  }, []);

  const selected = useMemo(() => items.find((item) => item.id === selectedId) ?? null, [items, selectedId]);

  const filteredItems = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return items;
    return items.filter((item) => [item.name, item.slug, item.description ?? ""].some((value) => value.toLowerCase().includes(q)));
  }, [items, query]);

  const filteredProducts = useMemo(() => {
    const q = productQuery.trim().toLowerCase();
    const source = q ? products.filter((item) => `${item.name} ${item.meta}`.toLowerCase().includes(q)) : products;
    const picked = new Set(form.productIds);
    return [...source].sort((a, b) => Number(picked.has(b.id)) - Number(picked.has(a.id))).slice(0, 80);
  }, [products, productQuery, form.productIds]);

  function applySelected(item: PromotionResponse) {
    setSelectedId(item.id);
    setForm(toPromotionForm(item));
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setForm(emptyForm());
    setProductQuery("");
    setMessage("Bạn đang tạo khuyến mãi mới.");
  }

  function toggleProduct(productId: string) {
    setForm((current) => ({
      ...current,
      productIds: current.productIds.includes(productId)
        ? current.productIds.filter((id) => id !== productId)
        : [...current.productIds, productId]
    }));
  }

  async function reload() {
    const next = await listPromotions();
    setItems(next);
    return next;
  }

  async function handleSubmit() {
    const discountPercent = Number(form.discountPercent);
    if (!form.name.trim() || !Number.isFinite(discountPercent) || discountPercent <= 0 || discountPercent > 100 || form.productIds.length === 0) {
      setMessage("Nhập tên, phần trăm giảm từ 0.01 đến 100 và chọn ít nhất 1 sản phẩm.");
      return;
    }

    const payload = {
      name: form.name.trim(),
      description: form.description.trim() || null,
      discountPercent,
      startsAt: toInstant(form.startsAt),
      endsAt: toInstant(form.endsAt),
      productIds: form.productIds
    };

    try {
      setSaving(true);
      setMessage(null);
      const saved = selectedId ? await updatePromotion(selectedId, payload) : await createPromotion(payload);
      const next = await reload();
      setSelectedId(saved.id);
      setItems(next.map((item) => (item.id === saved.id ? saved : item)));
      applySelected(saved);
      setMessage(selectedId ? "Đã cập nhật khuyến mãi." : "Đã tạo khuyến mãi.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không lưu được khuyến mãi."));
    } finally {
      setSaving(false);
    }
  }

  async function handleStatus(status: PromotionStatus) {
    if (!selectedId) return;
    try {
      setSaving(true);
      const updated = await updatePromotionStatus(selectedId, status);
      setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      applySelected(updated);
      setMessage("Đã đổi trạng thái khuyến mãi.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không đổi được trạng thái khuyến mãi."));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!selectedId) return;
    if (!window.confirm("Xóa khuyến mãi này? Hành động không thể hoàn tác.")) return;
    try {
      setSaving(true);
      await deletePromotion(selectedId);
      const remaining = items.filter((item) => item.id !== selectedId);
      setItems(remaining);
      setSelectedId(remaining[0]?.id ?? "");
      if (remaining[0]) applySelected(remaining[0]);
      else setForm(emptyForm());
      setMessage("Đã xóa khuyến mãi.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không xóa được khuyến mãi."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách khuyến mãi</h2>
            <p className="panel-copy">Các chương trình giảm giá theo sản phẩm, dùng cho Flash Sale và giá sale trên storefront.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>Tạo khuyến mãi</button>
        </div>

        <div className="table-toolbar">
          <input className="admin-input" placeholder="Tìm theo tên, slug hoặc mô tả..." value={query} onChange={(event) => setQuery(event.target.value)} />
        </div>

        {filteredItems.length === 0 ? (
          <div className="empty-state">Chưa có khuyến mãi phù hợp.</div>
        ) : (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Tên</th>
                  <th>Giảm</th>
                  <th>Thời gian</th>
                  <th>SP</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {filteredItems.map((item) => (
                  <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => applySelected(item)}>
                    <td>
                      <strong>{item.name}</strong>
                      <div className="table-subtle">{item.slug}</div>
                    </td>
                    <td>{Number(item.discountPercent).toLocaleString("vi-VN")}%</td>
                    <td>
                      <div className="table-subtle">{formatDateTime(item.startsAt)}</div>
                      <div className="table-subtle">đến {formatDateTime(item.endsAt)}</div>
                    </td>
                    <td>{item.productCount}</td>
                    <td><span className={`status-pill status-pill-${statusTone(item)}`}>{item.live ? "Đang chạy" : statusLabel(item.status)}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selected ? "Sửa khuyến mãi" : "Tạo khuyến mãi mới"}</h2>
            <p className="panel-copy">Chọn sản phẩm, đặt phần trăm giảm và khung thời gian hiệu lực.</p>
          </div>
        </div>

        {message ? <p className="action-message">{message}</p> : null}

        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Tên khuyến mãi" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
          <input className="admin-input" type="number" min="0.01" max="100" step="0.01" placeholder="% giảm" value={form.discountPercent} onChange={(event) => setForm((current) => ({ ...current, discountPercent: event.target.value }))} />
          <label className="admin-field">
            <span>Bắt đầu</span>
            <input className="admin-input" type="datetime-local" value={form.startsAt} onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))} />
          </label>
          <label className="admin-field">
            <span>Kết thúc</span>
            <input className="admin-input" type="datetime-local" value={form.endsAt} onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))} />
          </label>
          <textarea className="admin-textarea admin-form-full" placeholder="Mô tả khuyến mãi" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} />
          <div className="admin-form-full admin-subcard">
            <div className="editor-subcard-title">Sản phẩm áp dụng ({form.productIds.length})</div>
            <input className="admin-input" placeholder="Tìm sản phẩm để chọn..." value={productQuery} onChange={(event) => setProductQuery(event.target.value)} />
            <div className="combo-picker-list">
              {filteredProducts.length === 0 ? (
                <p className="table-subtle">Không có sản phẩm phù hợp.</p>
              ) : (
                filteredProducts.map((product) => (
                  <label key={product.id} className="combo-picker-row">
                    <input type="checkbox" checked={form.productIds.includes(product.id)} onChange={() => toggleProduct(product.id)} />
                    <span>
                      <strong>{product.name}</strong>
                      <span className="table-subtle" style={{ display: "block" }}>{product.meta}</span>
                    </span>
                  </label>
                ))
              )}
            </div>
          </div>
        </div>

        <div className="page-actions">
          {selectedId ? (
            <>
              <button className="admin-btn secondary" type="button" onClick={() => void handleStatus(selected?.status === "ACTIVE" ? "INACTIVE" : "ACTIVE")} disabled={saving}>
                {selected?.status === "ACTIVE" ? "Tạm dừng" : "Bật lại"}
              </button>
              <button className="admin-btn secondary" type="button" onClick={() => void handleDelete()} disabled={saving}>Xóa</button>
            </>
          ) : null}
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Lưu khuyến mãi" : "Tạo khuyến mãi"}
          </button>
        </div>
      </section>
    </div>
  );
}
