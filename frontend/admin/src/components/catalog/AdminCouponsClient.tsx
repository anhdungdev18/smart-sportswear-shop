"use client";

import { useMemo, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { createCoupon, updateCoupon } from "@/modules/catalog-admin/browser-api";
import type { CouponResponse, PromotionResponse } from "@/modules/catalog-admin/types";

const couponStatuses = ["ACTIVE", "INACTIVE", "EXPIRED"] as const;

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

function createEmptyForm(promotionId = "") {
  return {
    code: "",
    promotionId,
    description: "",
    status: "ACTIVE",
    startsAt: "",
    endsAt: "",
    usageLimit: "",
    perUserLimit: ""
  };
}

export function AdminCouponsClient({
  initialItems,
  promotions
}: {
  initialItems: CouponResponse[];
  promotions: PromotionResponse[];
}) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm(promotions[0]?.id ?? ""));
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const promotionMap = useMemo(() => new Map(promotions.map((item) => [item.id, item.name])), [promotions]);

  function handleSelect(item: CouponResponse) {
    setSelectedId(item.id);
    setForm({
      code: item.code,
      promotionId: item.promotionId,
      description: item.description ?? "",
      status: item.status,
      startsAt: toLocalDateTime(item.startsAt),
      endsAt: toLocalDateTime(item.endsAt),
      usageLimit: item.usageLimit != null ? String(item.usageLimit) : "",
      perUserLimit: item.perUserLimit != null ? String(item.perUserLimit) : ""
    });
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setForm(createEmptyForm(promotions[0]?.id ?? ""));
    setMessage("Bạn đang tạo coupon mới.");
  }

  async function handleSubmit() {
    try {
      setSaving(true);
      setMessage(null);

      if (selectedId) {
        const updated = await updateCoupon(selectedId, {
          description: form.description || null,
          status: form.status,
          startsAt: toInstant(form.startsAt),
          endsAt: toInstant(form.endsAt),
          usageLimit: form.usageLimit ? Number(form.usageLimit) : null,
          perUserLimit: form.perUserLimit ? Number(form.perUserLimit) : null
        });
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        setMessage(`Đã cập nhật coupon ${updated.code}.`);
        return;
      }

      const created = await createCoupon({
        code: form.code,
        promotionId: form.promotionId,
        description: form.description || null,
        status: form.status,
        startsAt: toInstant(form.startsAt),
        endsAt: toInstant(form.endsAt),
        usageLimit: form.usageLimit ? Number(form.usageLimit) : null,
        perUserLimit: form.perUserLimit ? Number(form.perUserLimit) : null
      });
      setItems((current) => [created, ...current]);
      setSelectedId(created.id);
      setMessage(`Đã tạo coupon ${created.code}.`);
    } catch (error) {
      setMessage(extractError(error, "Không lưu được coupon"));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách coupon</h2>
            <p className="panel-copy">Theo dõi mã giảm giá gắn với promotion cụ thể.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo coupon
          </button>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Khuyến mãi</th>
              <th>Trạng thái</th>
              <th>Đã dùng</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => handleSelect(item)}>
                <td>{item.code}</td>
                <td>{promotionMap.get(item.promotionId) ?? item.promotionId}</td>
                <td>{item.status}</td>
                <td>{item.usageCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedId ? "Cập nhật coupon" : "Tạo coupon mới"}</h2>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Mã coupon" value={form.code} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} disabled={Boolean(selectedId)} />
          <select className="select" value={form.promotionId} onChange={(event) => setForm((current) => ({ ...current, promotionId: event.target.value }))} disabled={Boolean(selectedId)}>
            {promotions.map((item) => (
              <option value={item.id} key={item.id}>{item.name}</option>
            ))}
          </select>
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>
            {couponStatuses.map((item) => (
              <option value={item} key={item}>{item}</option>
            ))}
          </select>
          <input className="admin-input" type="datetime-local" value={form.startsAt} onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.endsAt} onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giới hạn lượt dùng" value={form.usageLimit} onChange={(event) => setForm((current) => ({ ...current, usageLimit: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giới hạn mỗi user" value={form.perUserLimit} onChange={(event) => setForm((current) => ({ ...current, perUserLimit: event.target.value }))} />
          <div className="admin-form-full">
            <textarea className="admin-textarea" placeholder="Mô tả" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} />
          </div>
        </div>
        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật coupon" : "Tạo coupon"}
          </button>
        </div>
      </section>
    </div>
  );
}
