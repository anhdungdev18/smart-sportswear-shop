"use client";

import Image from "next/image";
import { useDeferredValue, useEffect, useMemo, useRef, useState } from "react";
import { extractAdminError } from "@/modules/api/admin-errors";
import {
  addProductToCollection,
  createCollection,
  deleteCollection,
  listCollectionProducts,
  listProductsForPicker,
  removeProductFromCollection,
  updateCollection
} from "@/modules/catalog-admin/browser-api";
import type { ProductPickItem } from "@/modules/catalog-admin/browser-api";
import type { CollectionResponse } from "@/modules/catalog-admin/types";
import { toSlug } from "@/modules/utils/slug";

const PLACEHOLDER_IMG = "https://placehold.co/96x96/f5f5f5/202020?text=SP";

function getProductBrandName(product: ProductPickItem) {
  return product.brandName ?? product.brand?.name ?? null;
}

function getProductCategoryName(product: ProductPickItem) {
  return product.categoryName ?? product.category?.name ?? null;
}

function productStatusClass(status: string) {
  switch (status) {
    case "ACTIVE": return "active";
    case "INACTIVE": return "draft";
    default: return "draft";
  }
}

const STATUS_OPTIONS = ["DRAFT", "ACTIVE", "ARCHIVED"] as const;
const COLLECTION_TYPES = ["CAMPAIGN", "SEASONAL", "LOOKBOOK", "SPORT", "EDITORIAL"] as const;

function createEmptyForm() {
  return {
    name: "",
    slug: "",
    description: "",
    shortDescription: "",
    collectionType: "LOOKBOOK",
    season: "",
    year: "",
    bannerImageUrl: "",
    coverImageUrl: "",
    status: "DRAFT",
    startsAt: "",
    endsAt: "",
    sortOrder: "0",
    isFeatured: false
  };
}

function toLocalDateTime(value: string | null) {
  if (!value) return "";
  return new Date(value).toISOString().slice(0, 16);
}

function toInstant(value: string) {
  return value ? new Date(value).toISOString() : null;
}

function toStatusLabel(status: string) {
  switch (status) {
    case "ACTIVE":
      return "Hoạt động";
    case "ARCHIVED":
      return "Lưu trữ";
    default:
      return "Nháp";
  }
}

function toStatusTone(status: string) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "ARCHIVED":
      return "muted";
    default:
      return "warning";
  }
}

function createAssignError(error: unknown) {
  return extractAdminError(error, "Không thể cập nhật danh sách sản phẩm của bộ sưu tập");
}

export function AdminCollectionsClient({ initialItems }: { initialItems: CollectionResponse[] }) {
  const [items, setItems] = useState(initialItems);
  const [selectedId, setSelectedId] = useState(initialItems[0]?.id ?? "");
  const [form, setForm] = useState(createEmptyForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [slugDirty, setSlugDirty] = useState(false);
  const [allProducts, setAllProducts] = useState<ProductPickItem[]>([]);
  const [assigned, setAssigned] = useState<ProductPickItem[]>([]);
  const [productSearch, setProductSearch] = useState("");
  const [collectionSearch, setCollectionSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | string>("all");
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [assignMessage, setAssignMessage] = useState<string | null>(null);
  const deferredProductSearch = useDeferredValue(productSearch);
  const deferredCollectionSearch = useDeferredValue(collectionSearch);
  const assignedCacheRef = useRef<Record<string, ProductPickItem[]>>({});

  const selectedCollection = items.find((item) => item.id === selectedId) ?? null;

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
      shortDescription: first.shortDescription ?? "",
      collectionType: first.collectionType,
      season: first.season ?? "",
      year: first.year != null ? String(first.year) : "",
      bannerImageUrl: first.bannerImageUrl ?? "",
      coverImageUrl: first.coverImageUrl ?? "",
      status: first.status,
      startsAt: toLocalDateTime(first.startsAt),
      endsAt: toLocalDateTime(first.endsAt),
      sortOrder: String(first.sortOrder ?? 0),
      isFeatured: Boolean(first.isFeatured)
    });
  }, [initialItems]);

  useEffect(() => {
    if (!selectedId) {
      setAssigned([]);
      return;
    }

    const cached = assignedCacheRef.current[selectedId];
    if (cached) {
      setAssigned(cached);
      return;
    }

    let active = true;
    setLoadingProducts(true);
    setAssignMessage(null);

    listCollectionProducts(selectedId)
      .then((data) => {
        if (!active) return;
        assignedCacheRef.current[selectedId] = data;
        setAssigned(data);
      })
      .catch((error) => {
        if (!active) return;
        setAssigned([]);
        setAssignMessage(createAssignError(error));
      })
      .finally(() => {
        if (active) {
          setLoadingProducts(false);
        }
      });

    return () => {
      active = false;
    };
  }, [selectedId]);

  const filteredCollections = useMemo(() => {
    const query = deferredCollectionSearch.trim().toLowerCase();
    return items.filter((item) => {
      const matchesStatus = statusFilter === "all" ? true : item.status === statusFilter;
      if (!matchesStatus) return false;
      if (!query) return true;
      return [item.name, item.slug, item.season ?? "", String(item.year ?? ""), item.collectionType]
        .join(" ")
        .toLowerCase()
        .includes(query);
    });
  }, [deferredCollectionSearch, items, statusFilter]);

  const filteredProducts = useMemo(() => {
    const query = deferredProductSearch.trim().toLowerCase();
    const assignedIds = new Set(assigned.map((item) => item.id));
    const candidates = allProducts.filter((item) => !assignedIds.has(item.id));
    if (!query) {
      return candidates.slice(0, 30);
    }

    return candidates
      .filter((item) => [item.name, item.slug, item.status].join(" ").toLowerCase().includes(query))
      .slice(0, 30);
  }, [allProducts, assigned, deferredProductSearch]);

  async function ensureProductPickerLoaded() {
    if (allProducts.length > 0 || loadingProducts) {
      return;
    }

    try {
      setLoadingProducts(true);
      const data = await listProductsForPicker();
      setAllProducts(data);
    } catch (error) {
      setAssignMessage(createAssignError(error));
    } finally {
      setLoadingProducts(false);
    }
  }

  function handleSelect(item: CollectionResponse) {
    setSelectedId(item.id);
    setSlugDirty(true);
    setMessage(null);
    setAssignMessage(null);
    setForm({
      name: item.name,
      slug: item.slug,
      description: item.description ?? "",
      shortDescription: item.shortDescription ?? "",
      collectionType: item.collectionType,
      season: item.season ?? "",
      year: item.year != null ? String(item.year) : "",
      bannerImageUrl: item.bannerImageUrl ?? "",
      coverImageUrl: item.coverImageUrl ?? "",
      status: item.status,
      startsAt: toLocalDateTime(item.startsAt),
      endsAt: toLocalDateTime(item.endsAt),
      sortOrder: String(item.sortOrder ?? 0),
      isFeatured: Boolean(item.isFeatured)
    });
  }

  function startCreate() {
    setSelectedId("");
    setSlugDirty(false);
    setAssignMessage(null);
    setProductSearch("");
    setAssigned([]);
    setForm(createEmptyForm());
    setMessage("Bạn đang tạo bộ sưu tập mới.");
  }

  async function handleSubmit() {
    if (!form.name.trim()) {
      setMessage("Vui lòng nhập tên bộ sưu tập.");
      return;
    }

    if (!form.slug.trim()) {
      setMessage("Slug không được để trống.");
      return;
    }

    try {
      setSaving(true);
      setMessage(null);

      const payload = {
        name: form.name.trim(),
        slug: form.slug.trim(),
        description: form.description.trim() || null,
        shortDescription: form.shortDescription.trim() || null,
        collectionType: form.collectionType,
        season: form.season.trim() || null,
        year: form.year.trim() ? Number(form.year) : null,
        bannerImageUrl: form.bannerImageUrl.trim() || null,
        coverImageUrl: form.coverImageUrl.trim() || null,
        status: form.status,
        startsAt: toInstant(form.startsAt),
        endsAt: toInstant(form.endsAt),
        sortOrder: form.sortOrder.trim() ? Number(form.sortOrder) : 0,
        isFeatured: form.isFeatured
      };

      if (selectedId) {
        const updated = await updateCollection(selectedId, payload);
        setItems((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        setMessage(`Đã cập nhật bộ sưu tập ${updated.name}.`);
        return;
      }

      const created = await createCollection(payload);
      setItems((current) => [created, ...current]);
      setSelectedId(created.id);
      setSlugDirty(true);
      setForm({
        name: created.name,
        slug: created.slug,
        description: created.description ?? "",
        shortDescription: created.shortDescription ?? "",
        collectionType: created.collectionType,
        season: created.season ?? "",
        year: created.year != null ? String(created.year) : "",
        bannerImageUrl: created.bannerImageUrl ?? "",
        coverImageUrl: created.coverImageUrl ?? "",
        status: created.status,
        startsAt: toLocalDateTime(created.startsAt),
        endsAt: toLocalDateTime(created.endsAt),
        sortOrder: String(created.sortOrder ?? 0),
        isFeatured: Boolean(created.isFeatured)
      });
      assignedCacheRef.current[created.id] = [];
      setAssigned([]);
      setMessage(`Đã tạo bộ sưu tập ${created.name}.`);
    } catch (error) {
      setMessage(extractAdminError(error, "Không thể lưu bộ sưu tập"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!selectedId) return;
    if (!window.confirm("Xóa bộ sưu tập này? Hành động không thể hoàn tác.")) return;
    try {
      setSaving(true);
      await deleteCollection(selectedId);
      const remaining = items.filter((item) => item.id !== selectedId);
      setItems(remaining);
      setSelectedId(remaining[0]?.id ?? "");
      setMessage("Đã xóa bộ sưu tập.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không xóa được bộ sưu tập"));
    } finally {
      setSaving(false);
    }
  }

  async function handleAddProduct(product: ProductPickItem) {
    if (!selectedId) return;

    try {
      setAssignMessage(null);
      await addProductToCollection(product.id, selectedId);
      const nextAssigned = [product, ...assigned];
      setAssigned(nextAssigned);
      assignedCacheRef.current[selectedId] = nextAssigned;
      setAssignMessage(`Đã thêm ${product.name} vào bộ sưu tập.`);
    } catch (error) {
      setAssignMessage(createAssignError(error));
    }
  }

  async function handleRemoveProduct(productId: string) {
    if (!selectedId) return;

    try {
      setAssignMessage(null);
      await removeProductFromCollection(productId, selectedId);
      const nextAssigned = assigned.filter((item) => item.id !== productId);
      setAssigned(nextAssigned);
      assignedCacheRef.current[selectedId] = nextAssigned;
      setAssignMessage("Đã gỡ sản phẩm khỏi bộ sưu tập.");
    } catch (error) {
      setAssignMessage(createAssignError(error));
    }
  }

  return (
    <div className={`admin-grid ${selectedId ? "admin-grid-3" : "admin-grid-2"}`}>
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách bộ sưu tập</h2>
            <p className="panel-copy">Quản lý lookbook, mùa vụ và các chiến dịch trưng bày của storefront.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo bộ sưu tập
          </button>
        </div>

        <div className="admin-form-grid" style={{ marginBottom: 16 }}>
          <input
            className="admin-input"
            placeholder="Tìm theo tên, slug, mùa hoặc năm..."
            value={collectionSearch}
            onChange={(event) => setCollectionSearch(event.target.value)}
          />
          <select className="select" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
            <option value="all">Tất cả trạng thái</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {toStatusLabel(status)}
              </option>
            ))}
          </select>
        </div>

        {filteredCollections.length === 0 ? (
          <div className="empty-state">Không có bộ sưu tập nào khớp bộ lọc hiện tại.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Bộ sưu tập</th>
                <th>Mùa</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {filteredCollections.map((item) => (
                <tr key={item.id} className={selectedId === item.id ? "row-selected" : ""} onClick={() => handleSelect(item)}>
                  <td>
                    <strong>{item.name}</strong>
                    <div className="table-subtle">{item.slug}</div>
                  </td>
                  <td>{[item.season, item.year].filter(Boolean).join(" ") || "-"}</td>
                  <td>
                    <span className={`status-pill status-pill-${toStatusTone(item.status)}`}>{toStatusLabel(item.status)}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedId ? "Cập nhật bộ sưu tập" : "Tạo bộ sưu tập mới"}</h2>
            <p className="panel-copy">Slug có thể nhập tay hoặc tự sinh theo tên nếu bạn chưa chỉnh thủ công.</p>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}

        <div className="admin-form-grid">
          <input
            className="admin-input"
            placeholder="Tên bộ sưu tập"
            value={form.name}
            onChange={(event) => {
              const name = event.target.value;
              setForm((current) => ({ ...current, name, ...(!slugDirty && { slug: toSlug(name) }) }));
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
          <select className="select" value={form.collectionType} onChange={(event) => setForm((current) => ({ ...current, collectionType: event.target.value }))}>
            {COLLECTION_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
          <select className="select" value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
          <input
            className="admin-input"
            placeholder="Mùa"
            value={form.season}
            onChange={(event) => setForm((current) => ({ ...current, season: event.target.value }))}
          />
          <input
            className="admin-input"
            placeholder="Năm"
            value={form.year}
            onChange={(event) => setForm((current) => ({ ...current, year: event.target.value }))}
          />
          <input
            className="admin-input"
            placeholder="URL ảnh bìa"
            value={form.coverImageUrl}
            onChange={(event) => setForm((current) => ({ ...current, coverImageUrl: event.target.value }))}
          />
          <input
            className="admin-input"
            placeholder="URL ảnh banner"
            value={form.bannerImageUrl}
            onChange={(event) => setForm((current) => ({ ...current, bannerImageUrl: event.target.value }))}
          />
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
          <input
            className="admin-input"
            placeholder="Thứ tự hiển thị"
            value={form.sortOrder}
            onChange={(event) => setForm((current) => ({ ...current, sortOrder: event.target.value }))}
          />
          <label className="admin-toggle">
            <input
              type="checkbox"
              checked={form.isFeatured}
              onChange={(event) => setForm((current) => ({ ...current, isFeatured: event.target.checked }))}
            />
            <span>Đánh dấu nổi bật</span>
          </label>
          <div className="admin-form-full">
            <input
              className="admin-input"
              placeholder="Mô tả ngắn"
              value={form.shortDescription}
              onChange={(event) => setForm((current) => ({ ...current, shortDescription: event.target.value }))}
            />
          </div>
          <div className="admin-form-full">
            <textarea
              className="admin-textarea"
              placeholder="Mô tả đầy đủ"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </div>
        </div>

        <div className="page-actions">
          {selectedId && (
            <button className="admin-btn secondary" type="button" onClick={() => void handleDelete()} disabled={saving}>
              Xóa bộ sưu tập
            </button>
          )}
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật bộ sưu tập" : "Tạo bộ sưu tập"}
          </button>
        </div>
      </section>

      {selectedId ? (
        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Gắn sản phẩm</h2>
              <p className="panel-copy">
                {selectedCollection ? `Sản phẩm đang thuộc bộ sưu tập ${selectedCollection.name}.` : "Quản lý sản phẩm trong bộ sưu tập."}
              </p>
            </div>
          </div>

          {assignMessage ? <p className="action-message">{assignMessage}</p> : null}

          <div className="admin-form-grid" style={{ marginBottom: 16 }}>
            <input
              className="admin-input"
              placeholder="Tìm sản phẩm để thêm..."
              value={productSearch}
              onFocus={() => void ensureProductPickerLoaded()}
              onChange={(event) => {
                void ensureProductPickerLoaded();
                setProductSearch(event.target.value);
              }}
            />
          </div>

          <div style={{ marginBottom: 8 }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>
              Sản phẩm đang thuộc bộ sưu tập ({assigned.length})
            </h3>
            {loadingProducts && assigned.length === 0 ? (
              <div className="empty-state">Đang tải dữ liệu sản phẩm...</div>
            ) : assigned.length === 0 ? (
              <div className="empty-state">Bộ sưu tập này chưa có sản phẩm nào.</div>
            ) : (
              <div className="admin-gallery">
                {assigned.map((product) => {
                  const brandName = getProductBrandName(product);
                  const categoryName = getProductCategoryName(product);
                  const subtitle = [brandName, categoryName].filter(Boolean).join(" · ");
                  return (
                    <article key={product.id} className="admin-image-card">
                      <Image
                        src={product.thumbnail ?? PLACEHOLDER_IMG}
                        alt={product.name}
                        width={96}
                        height={96}
                        style={{ width: 96, height: 96, borderRadius: 12, objectFit: "cover" }}
                        unoptimized
                      />
                      <div>
                        <strong style={{ display: "block", marginBottom: 2 }}>{product.name}</strong>
                        {subtitle ? <div className="table-subtle">{subtitle}</div> : null}
                        <div className="table-subtle">{product.slug}</div>
                        <span className={`status ${productStatusClass(product.status)}`} style={{ marginTop: 4, display: "inline-block" }}>
                          {product.status}
                        </span>
                      </div>
                      <button
                        className="admin-btn secondary"
                        type="button"
                        onClick={() => void handleRemoveProduct(product.id)}
                      >
                        Gỡ khỏi bộ sưu tập
                      </button>
                    </article>
                  );
                })}
              </div>
            )}
          </div>

          <div style={{ marginTop: 24 }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>Sản phẩm khả dụng để thêm</h3>
            {loadingProducts && allProducts.length === 0 ? (
              <div className="empty-state">Đang tải danh mục sản phẩm...</div>
            ) : filteredProducts.length === 0 ? (
              <div className="empty-state">
                {allProducts.length === 0
                  ? "Chưa có sản phẩm nào sẵn sàng để thêm hoặc bạn đã thêm hết trong bộ sưu tập này."
                  : "Không tìm thấy sản phẩm phù hợp."}
              </div>
            ) : (
              <div className="admin-gallery">
                {filteredProducts.map((product) => {
                  const brandName = getProductBrandName(product);
                  const categoryName = getProductCategoryName(product);
                  const subtitle = [brandName, categoryName].filter(Boolean).join(" · ");
                  return (
                    <article key={product.id} className="admin-image-card">
                      <Image
                        src={product.thumbnail ?? PLACEHOLDER_IMG}
                        alt={product.name}
                        width={96}
                        height={96}
                        style={{ width: 96, height: 96, borderRadius: 12, objectFit: "cover" }}
                        unoptimized
                      />
                      <div>
                        <strong style={{ display: "block", marginBottom: 2 }}>{product.name}</strong>
                        {subtitle ? <div className="table-subtle">{subtitle}</div> : null}
                        <div className="table-subtle">{product.slug}</div>
                        <span className={`status ${productStatusClass(product.status)}`} style={{ marginTop: 4, display: "inline-block" }}>
                          {product.status}
                        </span>
                      </div>
                      <button
                        className="admin-btn"
                        type="button"
                        onClick={() => void handleAddProduct(product)}
                      >
                        Thêm vào
                      </button>
                    </article>
                  );
                })}
              </div>
            )}
          </div>
        </section>
      ) : null}
    </div>
  );
}
﻿
