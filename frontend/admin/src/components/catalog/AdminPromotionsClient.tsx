"use client";

import { useMemo, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { createPromotion, updatePromotion } from "@/modules/catalog-admin/browser-api";
import type { PromotionResponse } from "@/modules/catalog-admin/types";

type ProductRef = {
  id: string;
  name: string;
};

const promotionTypes = ["PERCENTAGE", "FIXED_AMOUNT"] as const;
const promotionScopes = ["ORDER", "PRODUCT", "CATEGORY"] as const;
const promotionStatuses = ["DRAFT", "ACTIVE", "INACTIVE", "EXPIRED"] as const;

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as { message?: string } | null;
    return payload?.message ?? fallback;
  }

  return fallback;
}

function toLocalDateTime(value: string | null) {
  if (!value) {
    return "";
  }

  return new Date(value).toISOString().slice(0, 16);
}

function toInstant(value: string) {
  return value ? new Date(value).toISOString() : null;
}

function createEmptyForm() {
  return {
    name: "",
    slug: "",
    description: "",
    type: "PERCENTAGE",
    scope: "ORDER",
    status: "DRAFT",
    discountPercent: "",
    discountAmount: "",
    minOrderAmount: "",
    maxDiscountAmount: "",
    startsAt: "",
    endsAt: "",
    usageLimit: "",
    productIds: [] as string[]
  };
}

export function AdminPromotionsClient({
  initialItems,
  products
}: {
  initialItems: PromotionResponse[];
  products: ProductRef[];
}) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const productMap = useMemo(() => new Map(products.map((item) => [item.id, item.name])), [products]);

  function handleSelect(item: PromotionResponse) {
    setSelectedId(item.id);
    setForm({
      name: item.name,
      slug: item.slug,
      description: item.description ?? "",
      type: item.type,
      scope: item.scope,
      status: item.status,
      discountPercent: item.discountPercent != null ? String(item.discountPercent) : "",
      discountAmount: item.discountAmount != null ? String(item.discountAmount) : "",
      minOrderAmount: item.minOrderAmount != null ? String(item.minOrderAmount) : "",
      maxDiscountAmount: item.maxDiscountAmount != null ? String(item.maxDiscountAmount) : "",
      startsAt: toLocalDateTime(item.startsAt),
      endsAt: toLocalDateTime(item.endsAt),
      usageLimit: item.usageLimit != null ? String(item.usageLimit) : "",
      productIds: item.productIds
    });
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo khuyến mãi mới.");
  }

  async function handleSubmit() {
    try {
      setSaving(true);
      setMessage(null);

      if (selectedId) {
        const updated = await updatePromotion(selectedId, {
          name: form.name,
          description: form.description || null,
          status: form.status,
          discountPercent: form.discountPercent ? Number(form.discountPercent) : null,
          discountAmount: form.discountAmount ? Number(form.discountAmount) : null,
          minOrderAmount: form.minOrderAmount ? Number(form.minOrderAmount) : null,
          maxDiscountAmount: form.maxDiscountAmount ? Number(form.maxDiscountAmount) : null,
          startsAt: toInstant(form.startsAt),
          endsAt: toInstant(form.endsAt),
          usageLimit: form.usageLimit ? Number(form.usageLimit) : null,
          productIds: form.scope === "PRODUCT" ? form.productIds : []
        });
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        setMessage(`Đã cập nhật khuyến mãi ${updated.name}.`);
        return;
      }

      const created = await createPromotion({
        name: form.name,
        slug: form.slug,
        description: form.description || null,
        type: form.type,
        scope: form.scope,
        status: form.status,
        discountPercent: form.discountPercent ? Number(form.discountPercent) : null,
        discountAmount: form.discountAmount ? Number(form.discountAmount) : null,
        minOrderAmount: form.minOrderAmount ? Number(form.minOrderAmount) : null,
        maxDiscountAmount: form.maxDiscountAmount ? Number(form.maxDiscountAmount) : null,
        startsAt: toInstant(form.startsAt),
        endsAt: toInstant(form.endsAt),
        usageLimit: form.usageLimit ? Number(form.usageLimit) : null,
        productIds: form.scope === "PRODUCT" ? form.productIds : []
      });
      setItems((current) => [created, ...current]);
      setSelectedId(created.id);
      setMessage(`Đã tạo khuyến mãi ${created.name}.`);
    } catch (error) {
      setMessage(extractError(error, "Không lưu được khuyến mãi"));
    } finally {
      setSaving(false);
    }
  }

  function toggleProduct(productId: string) {
    setForm((current) => ({
      ...current,
      productIds: current.productIds.includes(productId)
        ? current.productIds.filter((item) => item !== productId)
        : [...current.productIds, productId]
    }));
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách khuyến mãi</h2>
            <p className="panel-copy">API admin promotions đã được nối trực tiếp.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo khuyến mãi
          </button>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Tên</th>
              <th>Loại</th>
              <th>Scope</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => handleSelect(item)}>
                <td>
                  <strong>{item.name}</strong>
                  <div className="table-subtle">{item.slug}</div>
                </td>
                <td>{item.type}</td>
                <td>{item.scope}</td>
                <td>{item.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedId ? "Cập nhật khuyến mãi" : "Tạo khuyến mãi mới"}</h2>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Tên khuyến mãi" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
          <input className="admin-input" placeholder="Slug" value={form.slug} onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))} disabled={Boolean(selectedId)} />
          <select className="select" value={form.type} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value }))} disabled={Boolean(selectedId)}>
            {promotionTypes.map((item) => (
              <option value={item} key={item}>{item}</option>
            ))}
          </select>
          <select className="select" value={form.scope} onChange={(event) => setForm((current) => ({ ...current, scope: event.target.value }))} disabled={Boolean(selectedId)}>
            {promotionScopes.map((item) => (
              <option value={item} key={item}>{item}</option>
            ))}
          </select>
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>
            {promotionStatuses.map((item) => (
              <option value={item} key={item}>{item}</option>
            ))}
          </select>
          <input className="admin-input" type="number" min={0} placeholder="Giảm %" value={form.discountPercent} onChange={(event) => setForm((current) => ({ ...current, discountPercent: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giảm cố định" value={form.discountAmount} onChange={(event) => setForm((current) => ({ ...current, discountAmount: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Đơn tối thiểu" value={form.minOrderAmount} onChange={(event) => setForm((current) => ({ ...current, minOrderAmount: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giảm tối đa" value={form.maxDiscountAmount} onChange={(event) => setForm((current) => ({ ...current, maxDiscountAmount: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.startsAt} onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.endsAt} onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giới hạn lượt dùng" value={form.usageLimit} onChange={(event) => setForm((current) => ({ ...current, usageLimit: event.target.value }))} />
          <div className="admin-form-full">
            <textarea className="admin-textarea" placeholder="Mô tả" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} />
          </div>
        </div>

        {form.scope === "PRODUCT" ? (
          <div className="admin-selection-grid">
            {products.map((item) => (
              <label className="admin-check card-select" key={item.id}>
                <input type="checkbox" checked={form.productIds.includes(item.id)} onChange={() => toggleProduct(item.id)} />
                <span>{productMap.get(item.id) ?? item.name}</span>
              </label>
            ))}
          </div>
        ) : null}

        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật khuyến mãi" : "Tạo khuyến mãi"}
          </button>
        </div>
      </section>
    </div>
  );
}
