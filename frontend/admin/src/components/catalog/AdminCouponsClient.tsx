"use client";

import { useDeferredValue, useEffect, useMemo, useState } from "react";
import { extractAdminError } from "@/modules/api/admin-errors";
import { createCoupon, listPromotionsForPicker, updateCoupon } from "@/modules/catalog-admin/browser-api";
import type { CouponResponse, PromotionResponse } from "@/modules/catalog-admin/types";

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: "Đang chạy",
  INACTIVE: "Tạm dừng",
  EXPIRED: "Hết hạn"
};

function toLocalDateTime(value: string | null) {
  if (!value) return "";
  return new Date(value).toISOString().slice(0, 16);
}

function toInstant(value: string) {
  return value ? new Date(value).toISOString() : null;
}

function createEmptyForm() {
  return {
    code: "",
    promotionId: "",
    description: "",
    status: "ACTIVE",
    startsAt: "",
    endsAt: "",
    usageLimit: "",
    perUserLimit: ""
  };
}

export function AdminCouponsClient({ initialItems }: { initialItems: CouponResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [promotionsLoading, setPromotionsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | keyof typeof STATUS_LABELS>("all");
  const deferredSearchTerm = useDeferredValue(searchTerm);

  useEffect(() => {
    listPromotionsForPicker()
      .then((data) => setPromotions(data))
      .catch(() => setPromotions([]))
      .finally(() => setPromotionsLoading(false));
  }, []);

  useEffect(() => {
    if (initialItems.length === 0) {
      return;
    }

    const first = initialItems[0];
    setSelectedId(first.id);
    setForm({
      code: first.code,
      promotionId: first.promotionId,
      description: first.description ?? "",
      status: first.status,
      startsAt: toLocalDateTime(first.startsAt),
      endsAt: toLocalDateTime(first.endsAt),
      usageLimit: first.usageLimit != null ? String(first.usageLimit) : "",
      perUserLimit: first.perUserLimit != null ? String(first.perUserLimit) : ""
    });
  }, [initialItems]);

  const promotionMap = useMemo(() => new Map(promotions.map((item) => [item.id, item])), [promotions]);
  const selectedItem = items.find((item) => item.id === selectedId) ?? null;
  const canCreate = !selectedId && !!form.promotionId && promotions.length > 0 && !saving;
  const canUpdate = !!selectedId && !saving;
  const filteredItems = useMemo(() => {
    const keyword = deferredSearchTerm.trim().toLowerCase();
    return items.filter((item) => {
      const matchesStatus = statusFilter === "all" ? true : item.status === statusFilter;
      if (!matchesStatus) return false;
      if (!keyword) return true;
      const promotionName = promotionMap.get(item.promotionId)?.name ?? "";
      return [item.code, item.description ?? "", promotionName].join(" ").toLowerCase().includes(keyword);
    });
  }, [deferredSearchTerm, items, promotionMap, statusFilter]);

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
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo coupon mới.");
  }

  async function handleSubmit() {
    if (!selectedId && !form.code.trim()) {
      setMessage("Vui lòng nhập mã coupon.");
      return;
    }

    if (!selectedId && !form.promotionId) {
      setMessage("Vui lòng chọn promotion cho coupon này.");
      return;
    }

    if (!selectedId && promotions.length === 0) {
      setMessage("Chưa có promotion nào để gắn coupon. Hãy tạo promotion trước.");
      return;
    }

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
        setForm({
          code: updated.code,
          promotionId: updated.promotionId,
          description: updated.description ?? "",
          status: updated.status,
          startsAt: toLocalDateTime(updated.startsAt),
          endsAt: toLocalDateTime(updated.endsAt),
          usageLimit: updated.usageLimit != null ? String(updated.usageLimit) : "",
          perUserLimit: updated.perUserLimit != null ? String(updated.perUserLimit) : ""
        });
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
      setForm({
        code: created.code,
        promotionId: created.promotionId,
        description: created.description ?? "",
        status: created.status,
        startsAt: toLocalDateTime(created.startsAt),
        endsAt: toLocalDateTime(created.endsAt),
        usageLimit: created.usageLimit != null ? String(created.usageLimit) : "",
        perUserLimit: created.perUserLimit != null ? String(created.perUserLimit) : ""
      });
      setMessage(`Đã tạo coupon ${created.code}.`);
    } catch (error) {
      setMessage(extractAdminError(error, "Không lưu được coupon."));
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
            <p className="panel-copy">Mỗi coupon gắn với một promotion nền, phục vụ checkout và validate coupon.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>Tạo coupon</button>
        </div>
        <div className="admin-form-grid" style={{ marginBottom: 16 }}>
          <input className="admin-input" placeholder="Tìm theo mã, mô tả hoặc promotion" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} />
          <select className="select" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as "all" | keyof typeof STATUS_LABELS)}>
            <option value="all">Tất cả trạng thái</option>
            {Object.keys(STATUS_LABELS).map((key) => <option key={key} value={key}>{STATUS_LABELS[key]}</option>)}
          </select>
        </div>
        {filteredItems.length === 0 ? (
          <div className="empty-state">{items.length === 0 ? "Hiện chưa có coupon nào. Hãy tạo coupon đầu tiên để bắt đầu." : "Không có coupon nào khớp bộ lọc hiện tại."}</div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Mã</th><th>Promotion</th><th>Lượt dùng</th><th>Trạng thái</th></tr></thead>
            <tbody>
              {filteredItems.map((item) => {
                const promotion = promotionMap.get(item.promotionId);
                return (
                  <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => handleSelect(item)}>
                    <td><strong>{item.code}</strong>{item.description ? <div className="table-subtle">{item.description}</div> : null}</td>
                    <td>{promotion?.name ?? item.promotionId}</td>
                    <td>{item.usageCount}{item.usageLimit != null ? ` / ${item.usageLimit}` : ""}</td>
                    <td>{STATUS_LABELS[item.status] ?? item.status}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedItem ? `Cập nhật: ${selectedItem.code}` : "Tạo coupon mới"}</h2>
            <p className="panel-copy">Khi đã tạo coupon, mã và promotion gắn kèm sẽ được khóa để tránh đổi nghĩa lịch sử sử dụng.</p>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Mã coupon" value={form.code} disabled={Boolean(selectedId)} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value.toUpperCase() }))} />
          <select className="select" value={form.promotionId} disabled={Boolean(selectedId) || promotionsLoading} onChange={(event) => setForm((current) => ({ ...current, promotionId: event.target.value }))}>
            <option value="">{promotionsLoading ? "Đang tải promotion..." : "Chọn promotion"}</option>
            {promotions.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
          </select>
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>{Object.keys(STATUS_LABELS).map((key) => <option key={key} value={key}>{STATUS_LABELS[key]}</option>)}</select>
          <input className="admin-input" type="number" min={1} placeholder="Giới hạn lượt dùng" value={form.usageLimit} onChange={(event) => setForm((current) => ({ ...current, usageLimit: event.target.value }))} />
          <input className="admin-input" type="number" min={1} placeholder="Giới hạn mỗi user" value={form.perUserLimit} onChange={(event) => setForm((current) => ({ ...current, perUserLimit: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.startsAt} onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.endsAt} onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))} />
          <div className="admin-form-full"><textarea className="admin-textarea" placeholder="Mô tả nội bộ" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} /></div>
        </div>
        <div className="page-actions">
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={selectedId ? !canUpdate : !canCreate}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật coupon" : "Tạo coupon"}
          </button>
        </div>
      </section>
    </div>
  );
}
