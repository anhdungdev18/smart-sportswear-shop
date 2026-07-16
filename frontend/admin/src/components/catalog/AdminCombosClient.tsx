"use client";

import { useEffect, useMemo, useState } from "react";
import { extractAdminError } from "@/modules/api/admin-errors";
import {
  createCombo,
  deleteCombo,
  listCombos,
  listProductsForPicker,
  updateCombo
} from "@/modules/catalog-admin/browser-api";
import type { ComboResponse } from "@/modules/catalog-admin/types";

type PickItem = { id: string; name: string };

function createEmptyForm() {
  return {
    name: "",
    description: "",
    discount: "",
    status: "ACTIVE" as "ACTIVE" | "INACTIVE",
    productIds: [] as string[]
  };
}

function money(value: number) {
  return `${value.toLocaleString("vi-VN")}đ`;
}

export function AdminCombosClient() {
  const [combos, setCombos] = useState<ComboResponse[]>([]);
  const [products, setProducts] = useState<PickItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedId, setSelectedId] = useState("");
  const [form, setForm] = useState(createEmptyForm());
  const [query, setQuery] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  async function load() {
    try {
      const [comboList, productList] = await Promise.all([listCombos(), listProductsForPicker()]);
      setCombos(comboList);
      setProducts(productList.map((item) => ({ id: item.id, name: item.name })));
    } catch (error) {
      setMessage(extractAdminError(error, "Không tải được danh sách combo."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const filteredProducts = useMemo(() => {
    const q = query.trim().toLowerCase();
    return q ? products.filter((item) => item.name.toLowerCase().includes(q)) : products;
  }, [products, query]);

  function productName(id: string) {
    return products.find((item) => item.id === id)?.name ?? id;
  }

  function handleSelect(combo: ComboResponse) {
    setSelectedId(combo.id);
    setForm({
      name: combo.name,
      description: combo.description ?? "",
      discount: String(combo.discountAmount),
      status: combo.status,
      productIds: combo.products.map((item) => item.productId)
    });
    setQuery("");
    setMessage(null);
  }

  function startCreate() {
    setSelectedId("");
    setForm(createEmptyForm());
    setQuery("");
    setMessage("Bạn đang tạo combo mới.");
  }

  function toggleProduct(id: string) {
    setForm((current) => ({
      ...current,
      productIds: current.productIds.includes(id)
        ? current.productIds.filter((x) => x !== id)
        : [...current.productIds, id]
    }));
  }

  async function handleSubmit() {
    if (!form.name.trim() || form.productIds.length === 0) {
      setMessage("Nhập tên combo và chọn ít nhất 1 sản phẩm.");
      return;
    }
    const payload = {
      name: form.name.trim(),
      description: form.description.trim() || null,
      discountAmount: Number(form.discount) || 0,
      status: form.status,
      productIds: form.productIds
    };
    try {
      setSaving(true);
      setMessage(null);
      if (selectedId) {
        const updated = await updateCombo(selectedId, payload);
        setCombos((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        setMessage(`Đã cập nhật combo ${updated.name}.`);
      } else {
        const created = await createCombo(payload);
        setCombos((current) => [created, ...current]);
        setSelectedId(created.id);
        setMessage(`Đã tạo combo ${created.name}.`);
      }
    } catch (error) {
      setMessage(extractAdminError(error, "Không lưu được combo."));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!selectedId) return;
    if (!window.confirm("Xóa combo này? Hành động không thể hoàn tác.")) return;
    try {
      setSaving(true);
      await deleteCombo(selectedId);
      setCombos((current) => current.filter((item) => item.id !== selectedId));
      setSelectedId("");
      setForm(createEmptyForm());
      setMessage("Đã xóa combo.");
    } catch (error) {
      setMessage(extractAdminError(error, "Không xóa được combo."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Danh sách combo</h2>
            <p className="panel-copy">Combo áp dụng khi giỏ hàng có đủ toàn bộ sản phẩm trong bộ.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreate}>
            Tạo combo
          </button>
        </div>

        {loading ? (
          <div className="empty-state">Đang tải...</div>
        ) : combos.length === 0 ? (
          <div className="empty-state">Chưa có combo nào. Hãy tạo combo đầu tiên ở bên phải.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Tên</th>
                <th>Sản phẩm</th>
                <th>Giảm</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {combos.map((combo) => (
                <tr
                  key={combo.id}
                  className={selectedId === combo.id ? "row-selected" : ""}
                  onClick={() => handleSelect(combo)}
                >
                  <td>
                    <strong>{combo.name}</strong>
                    <div className="table-subtle">
                      {combo.products.map((item) => productName(item.productId)).join(", ")}
                    </div>
                  </td>
                  <td>{combo.products.length}</td>
                  <td>{money(combo.discountAmount)}</td>
                  <td>{combo.status === "ACTIVE" ? "Hoạt động" : "Tạm dừng"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>{selectedId ? "Cập nhật combo" : "Tạo combo mới"}</h2>
            <p className="panel-copy">Chọn bộ sản phẩm cố định và mức giảm tiền khi khách mua đủ bộ.</p>
          </div>
        </div>
        {message ? <p className="action-message">{message}</p> : null}

        <div className="admin-form-grid">
          <input
            className="admin-input"
            placeholder="Tên combo"
            value={form.name}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
          />
          <input
            className="admin-input"
            type="number"
            min={0}
            placeholder="Giảm (đ)"
            value={form.discount}
            onChange={(event) => setForm((current) => ({ ...current, discount: event.target.value }))}
          />
          <select
            className="select"
            value={form.status}
            onChange={(event) =>
              setForm((current) => ({ ...current, status: event.target.value as "ACTIVE" | "INACTIVE" }))
            }
          >
            <option value="ACTIVE">Hoạt động</option>
            <option value="INACTIVE">Tạm dừng</option>
          </select>

          <div className="admin-form-full">
            <textarea
              className="admin-textarea"
              placeholder="Mô tả (tùy chọn)"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </div>

          <div className="admin-form-full">
            <div className="admin-subcard">
              <div className="side-nav-title">Sản phẩm trong combo ({form.productIds.length})</div>
              <input
                className="admin-input"
                placeholder="Tìm sản phẩm..."
                value={query}
                onChange={(event) => setQuery(event.target.value)}
              />
              <div className="combo-picker-list">
                {filteredProducts.length === 0 ? (
                  <p className="table-subtle">Không có sản phẩm.</p>
                ) : (
                  filteredProducts.map((item) => (
                    <label key={item.id} className="combo-picker-row">
                      <input
                        type="checkbox"
                        checked={form.productIds.includes(item.id)}
                        onChange={() => toggleProduct(item.id)}
                      />
                      <span>{item.name}</span>
                    </label>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="page-actions">
          {selectedId && (
            <button className="admin-btn secondary" type="button" onClick={() => void handleDelete()} disabled={saving}>
              Xóa combo
            </button>
          )}
          <button className="admin-btn" type="button" onClick={() => void handleSubmit()} disabled={saving}>
            {saving ? "Đang lưu..." : selectedId ? "Cập nhật combo" : "Tạo combo"}
          </button>
        </div>
      </section>
    </div>
  );
}
