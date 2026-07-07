"use client";

import { useDeferredValue, useEffect, useMemo, useState } from "react";
import { extractAdminError } from "@/modules/api/admin-errors";
import { createPromotion, listProductsForPicker, updatePromotion } from "@/modules/catalog-admin/browser-api";
import type { PromotionResponse } from "@/modules/catalog-admin/types";
import { toSlug } from "@/modules/utils/slug";

type ProductPickItem = { id: string; name: string; slug: string; status: string };

const PROMOTION_TYPES = ["PERCENTAGE", "FIXED_AMOUNT"] as const;
const PROMOTION_SCOPES = ["ORDER", "PRODUCT", "CATEGORY"] as const;
const PROMOTION_STATUSES = ["DRAFT", "ACTIVE", "INACTIVE", "EXPIRED"] as const;

const TYPE_LABELS: Record<string, string> = {
  PERCENTAGE: "Giảm theo %",
  FIXED_AMOUNT: "Giảm tiền cố định"
};

const SCOPE_LABELS: Record<string, string> = {
  ORDER: "Toàn đơn",
  PRODUCT: "Theo sản phẩm",
  CATEGORY: "Theo danh mục"
};

const STATUS_LABELS: Record<string, string> = {
  DRAFT: "Nháp",
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

export function AdminPromotionsClient({ initialItems }: { initialItems: PromotionResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [slugDirty, setSlugDirty] = useState(false);
  const [productSearch, setProductSearch] = useState("");
  const [listSearch, setListSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | (typeof PROMOTION_STATUSES)[number]>("all");
  const deferredProductSearch = useDeferredValue(productSearch);
  const deferredListSearch = useDeferredValue(listSearch);
  const [products, setProducts] = useState<ProductPickItem[]>([]);
  const [productsLoading, setProductsLoading] = useState(false);

  const selectedItem = items.find((item) => item.id === selectedId) ?? null;
  const needsProductPicker = form.scope === "PRODUCT";

  useEffect(() => {
    if (initialItems.length === 0) {
      return;
    }

    const first = initialItems[0];
    setSelectedId(first.id);
    setSlugDirty(true);
    setForm({
      name: first.name,
      slug: first.slug,
      description: first.description ?? "",
      type: first.type,
      scope: first.scope,
      status: first.status,
      discountPercent: first.discountPercent != null ? String(first.discountPercent) : "",
      discountAmount: first.discountAmount != null ? String(first.discountAmount) : "",
      minOrderAmount: first.minOrderAmount != null ? String(first.minOrderAmount) : "",
      maxDiscountAmount: first.maxDiscountAmount != null ? String(first.maxDiscountAmount) : "",
      startsAt: toLocalDateTime(first.startsAt),
      endsAt: toLocalDateTime(first.endsAt),
      usageLimit: first.usageLimit != null ? String(first.usageLimit) : "",
      productIds: first.productIds.map(String)
    });
  }, [initialItems]);

  useEffect(() => {
    if (!needsProductPicker || products.length > 0 || productsLoading) {
      return;
    }

    setProductsLoading(true);
    listProductsForPicker()
      .then((data) => setProducts(data))
      .catch(() => setProducts([]))
      .finally(() => setProductsLoading(false));
  }, [needsProductPicker, products.length, productsLoading]);

  const filteredProducts = useMemo(() => {
    const query = deferredProductSearch.trim().toLowerCase();
    if (!query) {
      return products.slice(0, 40);
    }

    return products.filter((item) => item.name.toLowerCase().includes(query)).slice(0, 40);
  }, [deferredProductSearch, products]);

  const filteredItems = useMemo(() => {
    const query = deferredListSearch.trim().toLowerCase();
    return items.filter((item) => {
      const matchesStatus = statusFilter === "all" ? true : item.status === statusFilter;
      if (!matchesStatus) return false;
      if (!query) return true;
      return [item.name, item.slug, TYPE_LABELS[item.type] ?? item.type, SCOPE_LABELS[item.scope] ?? item.scope].join(" ").toLowerCase().includes(query);
    });
  }, [deferredListSearch, items, statusFilter]);

  function handleSelect(item: PromotionResponse) {
    setSelectedId(item.id);
    setSlugDirty(true);
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
      productIds: item.productIds.map(String)
    });
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setSlugDirty(false);
    setProductSearch("");
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo khuyến mãi mới.");
  }

  function toggleProduct(productId: string) {
    setForm((current) => ({
      ...current,
      productIds: current.productIds.includes(productId)
        ? current.productIds.filter((item) => item !== productId)
        : [...current.productIds, productId]
    }));
  }

  async function handleSubmit() {
    if (!form.name.trim()) {
      setMessage("Vui lòng nhập tên khuyến mãi.");
      return;
    }

    if (!selectedId && !form.slug.trim()) {
      setMessage("Slug không được để trống.");
      return;
    }

    if (form.type === "PERCENTAGE" && !form.discountPercent) {
      setMessage("Khuyến mãi theo phần trăm cần có giá trị giảm %.");
      return;
    }

    if (form.type === "FIXED_AMOUNT" && !form.discountAmount) {
      setMessage("Khuyến mãi giảm tiền cố định cần có số tiền giảm.");
      return;
    }

    if (form.scope === "PRODUCT" && form.productIds.length === 0) {
      setMessage("Hãy chọn ít nhất một sản phẩm cho khuyến mãi theo sản phẩm.");
      return;
    }

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
        setForm({
          name: updated.name,
          slug: updated.slug,
          description: updated.description ?? "",
          type: updated.type,
          scope: updated.scope,
          status: updated.status,
          discountPercent: updated.discountPercent != null ? String(updated.discountPercent) : "",
          discountAmount: updated.discountAmount != null ? String(updated.discountAmount) : "",
          minOrderAmount: updated.minOrderAmount != null ? String(updated.minOrderAmount) : "",
          maxDiscountAmount: updated.maxDiscountAmount != null ? String(updated.maxDiscountAmount) : "",
          startsAt: toLocalDateTime(updated.startsAt),
          endsAt: toLocalDateTime(updated.endsAt),
          usageLimit: updated.usageLimit != null ? String(updated.usageLimit) : "",
          productIds: updated.productIds.map(String)
        });
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
      setSlugDirty(true);
      setForm({
        name: created.name,
        slug: created.slug,
        description: created.description ?? "",
        type: created.type,
        scope: created.scope,
        status: created.status,
        discountPercent: created.discountPercent != null ? String(created.discountPercent) : "",
        discountAmount: created.discountAmount != null ? String(created.discountAmount) : "",
        minOrderAmount: created.minOrderAmount != null ? String(created.minOrderAmount) : "",
        maxDiscountAmount: created.maxDiscountAmount != null ? String(created.maxDiscountAmount) : "",
        startsAt: toLocalDateTime(created.startsAt),
        endsAt: toLocalDateTime(created.endsAt),
        usageLimit: created.usageLimit != null ? String(created.usageLimit) : "",
        productIds: created.productIds.map(String)
      });
      setMessage(`Đã tạo khuyến mãi ${created.name}.`);
    } catch (error) {
      setMessage(extractAdminError(error, "Không lưu được khuyến mãi."));
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
            <p className="panel-copy">Quản lý promotion theo đơn hàng, theo sản phẩm hoặc theo danh mục.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>Tạo khuyến mãi</button>
        </div>
        <div className="admin-form-grid" style={{ marginBottom: 16 }}>
          <input className="admin-input" placeholder="Tìm theo tên, slug, loại hoặc phạm vi" value={listSearch} onChange={(event) => setListSearch(event.target.value)} />
          <select className="select" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as "all" | (typeof PROMOTION_STATUSES)[number])}>
            <option value="all">Tất cả trạng thái</option>
            {PROMOTION_STATUSES.map((item) => <option value={item} key={item}>{STATUS_LABELS[item]}</option>)}
          </select>
        </div>
        {filteredItems.length === 0 ? (
          <div className="empty-state">{items.length === 0 ? "Hiện chưa có khuyến mãi nào. Hãy tạo khuyến mãi đầu tiên để bắt đầu." : "Không có khuyến mãi nào khớp bộ lọc hiện tại."}</div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Tên</th><th>Loại</th><th>Phạm vi</th><th>Trạng thái</th></tr></thead>
            <tbody>
              {filteredItems.map((item) => (
                <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => handleSelect(item)}>
                  <td><strong>{item.name}</strong><div className="table-subtle">{item.slug}</div></td>
                  <td>{TYPE_LABELS[item.type] ?? item.type}</td>
                  <td>{SCOPE_LABELS[item.scope] ?? item.scope}</td>
                  <td>{STATUS_LABELS[item.status] ?? item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedItem ? `Cập nhật: ${selectedItem.name}` : "Tạo khuyến mãi mới"}</h2>
            <p className="panel-copy">Picker sản phẩm chỉ tải khi thật sự cần, để trang admin mở nhanh hơn khi số lượng sản phẩm lớn.</p>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}
        <div className="admin-form-grid">
          <input className="admin-input" placeholder="Tên khuyến mãi" value={form.name} onChange={(event) => {
            const name = event.target.value;
            setForm((current) => ({ ...current, name, ...(!slugDirty && { slug: toSlug(name) }) }));
          }} />
          <input className="admin-input" placeholder="Slug" value={form.slug} disabled={Boolean(selectedId)} onChange={(event) => { setSlugDirty(true); setForm((current) => ({ ...current, slug: event.target.value })); }} />
          <select className="select" value={form.type} disabled={Boolean(selectedId)} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value }))}>{PROMOTION_TYPES.map((item) => <option value={item} key={item}>{TYPE_LABELS[item]}</option>)}</select>
          <select className="select" value={form.scope} disabled={Boolean(selectedId)} onChange={(event) => setForm((current) => ({ ...current, scope: event.target.value, ...(event.target.value !== "PRODUCT" ? { productIds: [] } : {}) }))}>{PROMOTION_SCOPES.map((item) => <option value={item} key={item}>{SCOPE_LABELS[item]}</option>)}</select>
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>{PROMOTION_STATUSES.map((item) => <option value={item} key={item}>{STATUS_LABELS[item]}</option>)}</select>
          <input className="admin-input" type="number" min={0} placeholder="Giảm %" value={form.discountPercent} onChange={(event) => setForm((current) => ({ ...current, discountPercent: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giảm tiền cố định" value={form.discountAmount} onChange={(event) => setForm((current) => ({ ...current, discountAmount: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Đơn tối thiểu" value={form.minOrderAmount} onChange={(event) => setForm((current) => ({ ...current, minOrderAmount: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giảm tối đa" value={form.maxDiscountAmount} onChange={(event) => setForm((current) => ({ ...current, maxDiscountAmount: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.startsAt} onChange={(event) => setForm((current) => ({ ...current, startsAt: event.target.value }))} />
          <input className="admin-input" type="datetime-local" value={form.endsAt} onChange={(event) => setForm((current) => ({ ...current, endsAt: event.target.value }))} />
          <input className="admin-input" type="number" min={0} placeholder="Giới hạn lượt dùng" value={form.usageLimit} onChange={(event) => setForm((current) => ({ ...current, usageLimit: event.target.value }))} />
          <div className="admin-form-full"><textarea className="admin-textarea" placeholder="Mô tả" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} /></div>
        </div>

        {needsProductPicker ? (
          <div style={{ marginTop: 16 }}>
            <label className="admin-label">Áp dụng cho sản phẩm ({form.productIds.length} đã chọn)</label>
            <input className="admin-input" placeholder="Tìm sản phẩm..." value={productSearch} onChange={(event) => setProductSearch(event.target.value)} style={{ marginBottom: 8 }} />
            <div style={{ maxHeight: 240, overflowY: "auto", border: "1px solid var(--admin-line)", borderRadius: 10, padding: 6 }}>
              {productsLoading ? <div className="loading-state">Đang tải sản phẩm...</div> : null}
              {!productsLoading && filteredProducts.length === 0 ? <div className="empty-state" style={{ padding: 14 }}>{products.length === 0 ? "Chưa có sản phẩm nào để gán." : "Không tìm thấy sản phẩm phù hợp."}</div> : null}
              {filteredProducts.map((item) => (
                <label key={item.id} className="admin-check card-select" style={{ display: "flex", marginBottom: 6 }}>
                  <input type="checkbox" checked={form.productIds.includes(item.id)} onChange={() => toggleProduct(item.id)} />
                  <span>{item.name}</span>
                </label>
              ))}
            </div>
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
