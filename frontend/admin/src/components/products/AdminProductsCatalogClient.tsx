
"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { memo, useDeferredValue, useEffect, useMemo, useRef, useState } from "react";
import { ApiRequestError } from "@/modules/api/common";
import { toSlug } from "@/modules/utils/slug";
import {
  addProductImage,
  addProductToCollection,
  createAdminProduct,
  createVariant,
  deleteProduct,
  deleteProductImage,
  fetchAdminProductDetail,
  listCollectionsForPicker,
  listProductCollections,
  removeProductFromCollection,
  updateAdminProduct,
  updateVariant,
  uploadProductImage
} from "@/modules/catalog-admin/browser-api";
import type {
  BrandResponse,
  CategoryResponse,
  CollectionResponse,
  ProductDetailResponse,
  ProductImageResponse,
  ProductVariantResponse
} from "@/modules/catalog-admin/types";
import type { AdminProduct } from "@/modules/product-management/products";

const productStatusOptions = ["DRAFT", "ACTIVE", "INACTIVE"] as const;
const genderOptions = ["MEN", "WOMEN", "UNISEX", "KIDS"] as const;
const variantStatusOptions = ["ACTIVE", "OUT_OF_STOCK", "INACTIVE"] as const;

type ApiFieldErrorPayload = {
  field?: string;
  message?: string;
};

type ApiEnvelopePayload = {
  message?: string;
  data?: ApiFieldErrorPayload[] | null;
  errors?: ApiFieldErrorPayload[] | null;
};

type VariantDraft = ReturnType<typeof createEmptyVariantForm>;

const fieldLabelMap: Record<string, string> = {
  name: "Tên sản phẩm",
  slug: "Slug",
  categoryId: "Danh mục",
  brandId: "Thương hiệu",
  shortDescription: "Mô tả ngắn",
  description: "Mô tả chi tiết",
  gender: "Giới tính",
  sportType: "Môn thể thao",
  status: "Trạng thái",
  sku: "SKU",
  size: "Kích cỡ",
  color: "Màu sắc",
  price: "Giá bán",
  compareAtPrice: "Giá so sánh",
  stockQuantity: "Tồn kho"
};

function formatFieldErrors(items: ApiFieldErrorPayload[]) {
  const lines = items
    .map((item) => {
      const label = item.field ? fieldLabelMap[item.field] ?? item.field : null;
      const message = item.message?.trim();

      if (!message) {
        return null;
      }

      return label ? `${label}: ${message}` : message;
    })
    .filter((value): value is string => Boolean(value));

  return lines.length > 0 ? lines.join(" | ") : null;
}

function extractError(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError) {
    const payload = error.payload as ApiEnvelopePayload | null;
    const fieldErrors = [
      ...(Array.isArray(payload?.errors) ? payload.errors : []),
      ...(Array.isArray(payload?.data) ? payload.data : [])
    ];
    const fieldMessage = formatFieldErrors(fieldErrors);

    if (fieldMessage) {
      return fieldMessage;
    }

    return payload?.message ?? fallback;
  }

  return fallback;
}

function formatPrice(value: string) {
  const amount = Number(value);
  if (!Number.isFinite(amount) || amount <= 0) {
    return "Liên hệ";
  }

  return `${Math.round(amount).toLocaleString("vi-VN")} ₫`;
}

function mapAdminStatus(status: ProductDetailResponse["status"], stock: number): AdminProduct["status"] {
  if (status !== "ACTIVE") {
    return "draft";
  }

  if (stock <= 0) {
    return "out";
  }

  if (stock <= 10) {
    return "low";
  }

  return "active";
}

function collectionStatusTone(status: string) {
  if (status === "ACTIVE") return "success";
  if (status === "ARCHIVED") return "muted";
  return "warning";
}

function collectionStatusLabel(status: string) {
  if (status === "ACTIVE") return "Hoạt động";
  if (status === "ARCHIVED") return "Lưu trữ";
  return "Nháp";
}

function createEmptyProductForm(categoryId = "", brandId = "") {
  return {
    name: "",
    slug: "",
    shortDescription: "",
    description: "",
    categoryId,
    brandId,
    gender: "",
    sportType: "",
    status: "DRAFT",
    isFeatured: false
  };
}

function createEmptyVariantForm() {
  return {
    sku: "",
    size: "",
    color: "",
    price: "",
    compareAtPrice: "",
    stockQuantity: "0",
    status: "ACTIVE"
  };
}

function createEmptyImageForm() {
  return {
    imageUrl: "",
    publicId: "",
    altText: "",
    isPrimary: false,
    sortOrder: "0"
  };
}

function createEmptyUploadForm() {
  return {
    file: null as File | null,
    altText: "",
    isPrimary: false,
    sortOrder: "0"
  };
}

function toAdminProduct(detail: ProductDetailResponse, categories: CategoryResponse[], brands: BrandResponse[]): AdminProduct {
  const stock = detail.variants.reduce((sum, item) => sum + item.availableQuantity, 0);
  const prices = detail.variants.map((item) => item.price).filter((value): value is number => value != null);
  const minPrice = prices.length ? Math.min(...prices) : 0;
  const maxPrice = prices.length ? Math.max(...prices) : 0;
  const firstVariant = detail.variants[0];

  return {
    id: detail.id,
    sku: firstVariant?.sku ?? detail.slug,
    name: detail.name,
    category: categories.find((item) => item.id === detail.category?.id)?.name ?? detail.category?.name ?? "Chưa phân loại",
    brand: brands.find((item) => item.id === detail.brand?.id)?.name ?? detail.brand?.name ?? "Chưa có thương hiệu",
    price: minPrice === maxPrice ? formatPrice(String(minPrice)) : `${formatPrice(String(minPrice))} - ${formatPrice(String(maxPrice))}`,
    stock,
    sold: 0,
    status: mapAdminStatus(detail.status, stock),
    isFeatured: detail.isFeatured,
    image: detail.images[0]?.imageUrl ?? "https://placehold.co/96x96/f5f5f5/202020?text=SP"
  };
}

function buildVariantDrafts(variants: ProductVariantResponse[]) {
  return Object.fromEntries(
    variants.map((item) => [
      item.id,
      {
        sku: item.sku,
        size: item.size ?? "",
        color: item.color ?? "",
        price: item.price != null ? String(item.price) : "",
        compareAtPrice: item.compareAtPrice != null ? String(item.compareAtPrice) : "",
        stockQuantity: String(item.availableQuantity),
        status: item.status
      }
    ])
  );
}

function buildProductForm(detail: ProductDetailResponse, categories: CategoryResponse[], brands: BrandResponse[]) {
  return {
    name: detail.name,
    slug: detail.slug,
    shortDescription: detail.shortDescription ?? "",
    description: detail.description ?? "",
    categoryId: detail.category?.id ?? categories[0]?.id ?? "",
    brandId: detail.brand?.id ?? brands[0]?.id ?? "",
    gender: detail.gender ?? "",
    sportType: detail.sportType ?? "",
    status: detail.status,
    isFeatured: detail.isFeatured
  };
}
const VariantEditorCard = memo(function VariantEditorCard({
  variant,
  draft,
  saving,
  onDraftChange,
  onSave
}: {
  variant: ProductVariantResponse;
  draft: VariantDraft;
  saving: boolean;
  onDraftChange: (patch: Partial<VariantDraft>) => void;
  onSave: () => void;
}) {
  return (
    <div className="admin-subcard">
      <strong>{variant.sku}</strong>
      <div className="table-subtle">Khả dụng hiện tại: {variant.availableQuantity}</div>
      <div className="admin-form-grid">
        <input className="admin-input" value={draft.size} onChange={(event) => onDraftChange({ size: event.target.value })} />
        <input className="admin-input" value={draft.color} onChange={(event) => onDraftChange({ color: event.target.value })} />
        <input className="admin-input" type="number" min={0} value={draft.price} onChange={(event) => onDraftChange({ price: event.target.value })} />
        <input className="admin-input" type="number" min={0} value={draft.compareAtPrice} onChange={(event) => onDraftChange({ compareAtPrice: event.target.value })} />
        <select className="select" value={draft.status} onChange={(event) => onDraftChange({ status: event.target.value })}>
          {variantStatusOptions.map((item) => (
            <option value={item} key={item}>
              {item}
            </option>
          ))}
        </select>
      </div>
      <button className="admin-btn secondary" type="button" onClick={onSave} disabled={saving}>
        {saving ? "Đang lưu..." : "Lưu biến thể"}
      </button>
    </div>
  );
});

const ProductImageCard = memo(function ProductImageCard({
  image,
  productName,
  saving,
  onDelete
}: {
  image: ProductImageResponse;
  productName: string;
  saving: boolean;
  onDelete: () => void;
}) {
  return (
    <article className="admin-image-card">
      <Image
        src={image.imageUrl}
        alt={image.altText ?? productName}
        width={320}
        height={320}
        sizes="(max-width: 768px) 100vw, 320px"
        style={{ width: "100%", height: "auto" }}
      />
      <div>
        <strong>{image.altText ?? "Không có alt text"}</strong>
        <div className="table-subtle">
          {image.isPrimary ? "Ảnh chính" : "Ảnh phụ"} · Thứ tự {image.sortOrder}
        </div>
      </div>
      <button className="admin-btn secondary" type="button" onClick={onDelete} disabled={saving}>
        {saving ? "Đang xóa..." : "Xóa ảnh"}
      </button>
    </article>
  );
});

export function AdminProductsCatalogClient({
  initialProducts,
  categories,
  brands
}: {
  initialProducts: AdminProduct[];
  categories: CategoryResponse[];
  brands: BrandResponse[];
}) {
  const router = useRouter();
  const [products, setProducts] = useState(initialProducts);
  const [selectedProductId, setSelectedProductId] = useState(initialProducts.find((item) => item.id)?.id ?? "");
  const [detail, setDetail] = useState<ProductDetailResponse | null>(null);
  const detailCacheRef = useRef<Record<string, ProductDetailResponse>>({});
  const [detailLoading, setDetailLoading] = useState(false);
  const [productForm, setProductForm] = useState(createEmptyProductForm(categories[0]?.id ?? "", brands[0]?.id ?? ""));
  const [slugDirty, setSlugDirty] = useState(false);
  const [variantForm, setVariantForm] = useState(createEmptyVariantForm());
  const [variantDrafts, setVariantDrafts] = useState<Record<string, VariantDraft>>({});
  const [imageForm, setImageForm] = useState(createEmptyImageForm());
  const [uploadForm, setUploadForm] = useState(createEmptyUploadForm());
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | AdminProduct["status"] | "featured">("all");
  const deferredSearchTerm = useDeferredValue(searchTerm);

  const [productCollections, setProductCollections] = useState<CollectionResponse[]>([]);
  const [allCollections, setAllCollections] = useState<CollectionResponse[]>([]);
  const [collectionSearch, setCollectionSearch] = useState("");
  const [collectionsLoading, setCollectionsLoading] = useState(false);
  const [collectionsPickerLoading, setCollectionsPickerLoading] = useState(false);
  const [collectionMessage, setCollectionMessage] = useState<string | null>(null);
  const allCollectionsFetchedRef = useRef(false);
  const productCollectionsCacheRef = useRef<Record<string, CollectionResponse[]>>({});

  const selectedProduct = useMemo(() => products.find((item) => item.id === selectedProductId) ?? null, [products, selectedProductId]);
  const missingCategorySetup = categories.length === 0;
  const missingBrandSetup = brands.length === 0;
  const canSubmitProduct = !missingCategorySetup && !missingBrandSetup && saving !== "product";
  const filteredProducts = useMemo(() => {
    const keyword = deferredSearchTerm.trim().toLowerCase();

    return products.filter((product) => {
      const matchesStatus =
        statusFilter === "all"
          ? true
          : statusFilter === "featured"
            ? Boolean(product.isFeatured)
            : product.status === statusFilter;
      if (!matchesStatus) {
        return false;
      }

      if (!keyword) {
        return true;
      }

      const haystack = [product.name, product.sku, product.category, product.brand].join(" ").toLowerCase();
      return haystack.includes(keyword);
    });
  }, [deferredSearchTerm, products, statusFilter]);

  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => setMessage(null), 4000);
    return () => clearTimeout(timer);
  }, [message]);

  useEffect(() => {
    if (!collectionMessage) return;
    const timer = setTimeout(() => setCollectionMessage(null), 4000);
    return () => clearTimeout(timer);
  }, [collectionMessage]);

  useEffect(() => {
    if (!selectedProductId) {
      setProductCollections([]);
      return;
    }

    const cached = productCollectionsCacheRef.current[selectedProductId];
    if (cached) {
      setProductCollections(cached);
      return;
    }

    let active = true;
    setCollectionsLoading(true);

    listProductCollections(selectedProductId)
      .then((data) => {
        if (!active) return;
        productCollectionsCacheRef.current[selectedProductId] = data;
        setProductCollections(data);
      })
      .catch(() => {
        if (!active) return;
        setProductCollections([]);
      })
      .finally(() => {
        if (active) setCollectionsLoading(false);
      });

    return () => { active = false; };
  }, [selectedProductId]);

  // Load all collections once on mount — not tied to selectedProductId so the active
  // cleanup from switching products doesn't cancel it.
  useEffect(() => {
    if (allCollectionsFetchedRef.current) return;
    allCollectionsFetchedRef.current = true;
    setCollectionsPickerLoading(true);
    listCollectionsForPicker()
      .then((data) => setAllCollections(data))
      .catch(() => { allCollectionsFetchedRef.current = false; })
      .finally(() => setCollectionsPickerLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedProductId) {
      setDetail(null);
      return;
    }

    let active = true;

    async function loadDetail() {
      try {
        setDetailLoading(true);
        const cached = detailCacheRef.current[selectedProductId];
        if (cached) {
          setDetail(cached);
          setSlugDirty(true);
          setProductForm(buildProductForm(cached, categories, brands));
          setVariantDrafts(buildVariantDrafts(cached.variants));
          return;
        }

        const response = await fetchAdminProductDetail(selectedProductId);
        if (!active) {
          return;
        }

        detailCacheRef.current[response.id] = response;
        setDetail(response);
        setSlugDirty(true);
        setProductForm(buildProductForm(response, categories, brands));
        setVariantDrafts(buildVariantDrafts(response.variants));
      } catch (error) {
        if (active) {
          setMessage(extractError(error, "Không tải được chi tiết sản phẩm"));
        }
      } finally {
        if (active) {
          setDetailLoading(false);
        }
      }
    }

    void loadDetail();

    return () => {
      active = false;
    };
  }, [brands, categories, selectedProductId]);

  const filteredAvailableCollections = useMemo(() => {
    const assignedIds = new Set(productCollections.map((c) => c.id));
    const keyword = collectionSearch.trim().toLowerCase();
    return allCollections
      .filter((c) => !assignedIds.has(c.id))
      .filter((c) => !keyword || [c.name, c.slug, c.collectionType].join(" ").toLowerCase().includes(keyword))
      .slice(0, 20);
  }, [allCollections, productCollections, collectionSearch]);

  async function ensureAllCollectionsLoaded() {
    if (allCollectionsFetchedRef.current && allCollections.length > 0) return;
    if (collectionsPickerLoading) return;
    allCollectionsFetchedRef.current = true;
    try {
      setCollectionsPickerLoading(true);
      const data = await listCollectionsForPicker();
      setAllCollections(data);
    } catch {
      allCollectionsFetchedRef.current = false;
    } finally {
      setCollectionsPickerLoading(false);
    }
  }

  async function handleAddToCollection(collection: CollectionResponse) {
    if (!selectedProductId) return;
    try {
      setCollectionMessage(null);
      await addProductToCollection(selectedProductId, collection.id);
      const next = [...productCollections, collection];
      setProductCollections(next);
      productCollectionsCacheRef.current[selectedProductId] = next;
      setCollectionMessage(`Đã thêm vào bộ sưu tập ${collection.name}.`);
    } catch (error) {
      setCollectionMessage(extractError(error, "Không thêm được vào bộ sưu tập"));
    }
  }

  async function handleRemoveFromCollection(collectionId: string) {
    if (!selectedProductId) return;
    try {
      setCollectionMessage(null);
      await removeProductFromCollection(selectedProductId, collectionId);
      const next = productCollections.filter((c) => c.id !== collectionId);
      setProductCollections(next);
      productCollectionsCacheRef.current[selectedProductId] = next;
      setCollectionMessage("Đã gỡ khỏi bộ sưu tập.");
    } catch (error) {
      setCollectionMessage(extractError(error, "Không gỡ được khỏi bộ sưu tập"));
    }
  }

  function upsertProductCard(nextDetail: ProductDetailResponse) {
    const nextProduct = toAdminProduct(nextDetail, categories, brands);
    setProducts((current) => {
      const existingIndex = current.findIndex((item) => item.id === nextProduct.id);
      if (existingIndex === -1) {
        return [nextProduct, ...current];
      }

      const clone = current.slice();
      clone[existingIndex] = { ...clone[existingIndex], ...nextProduct, sold: clone[existingIndex].sold };
      return clone;
    });
  }

  async function refreshDetail(productId: string) {
    const response = await fetchAdminProductDetail(productId);
    detailCacheRef.current[response.id] = response;
    setDetail(response);
    upsertProductCard(response);
    setVariantDrafts(buildVariantDrafts(response.variants));
    return response;
  }
  async function handleCreateOrUpdateProduct() {
    if (missingCategorySetup) {
      setMessage("Chưa có danh mục nào trong hệ thống. Hãy tạo danh mục trước khi tạo sản phẩm.");
      return;
    }

    if (missingBrandSetup) {
      setMessage("Chưa có thương hiệu nào trong hệ thống. Hãy tạo thương hiệu trước khi tạo sản phẩm.");
      return;
    }

    try {
      setSaving("product");
      setMessage(null);

      const payload = {
        name: productForm.name,
        slug: productForm.slug,
        shortDescription: productForm.shortDescription || null,
        description: productForm.description || null,
        categoryId: productForm.categoryId,
        brandId: productForm.brandId,
        gender: productForm.gender || null,
        sportType: productForm.sportType || null,
        status: productForm.status,
        isFeatured: productForm.isFeatured
      };

      if (selectedProductId) {
        const updated = await updateAdminProduct(selectedProductId, payload);
        detailCacheRef.current[updated.id] = updated;
        setDetail(updated);
        upsertProductCard(updated);
        setMessage(`Đã cập nhật sản phẩm ${updated.name}.`);
        router.refresh();
        return;
      }

      const created = await createAdminProduct(payload);
      detailCacheRef.current[created.id] = created;
      setSelectedProductId(created.id);
      setDetail(created);
      upsertProductCard(created);
      setMessage(`Đã tạo sản phẩm ${created.name}.`);
      router.refresh();
    } catch (error) {
      setMessage(extractError(error, "Không lưu được sản phẩm"));
    } finally {
      setSaving(null);
    }
  }

  async function handleDeleteProduct() {
    if (!selectedProductId) return;
    if (!window.confirm("Xóa sản phẩm này? Hành động không thể hoàn tác.")) return;
    const idToDelete = selectedProductId;
    const nextId = products.find((item) => item.id && item.id !== idToDelete)?.id ?? "";
    try {
      setSaving("product-delete");
      await deleteProduct(idToDelete);
      delete detailCacheRef.current[idToDelete];
      setProducts((prev) => prev.filter((item) => item.id !== idToDelete));
      setSelectedProductId(nextId);
      setDetail(null);
      setMessage("Đã xóa sản phẩm.");
      router.refresh();
    } catch (error) {
      setMessage(extractError(error, "Không xóa được sản phẩm"));
    } finally {
      setSaving(null);
    }
  }

  async function handleCreateVariant() {
    if (!selectedProductId) {
      setMessage("Hãy tạo sản phẩm trước khi thêm biến thể.");
      return;
    }

    try {
      setSaving("variant-create");
      setMessage(null);
      await createVariant(selectedProductId, {
        sku: variantForm.sku,
        size: variantForm.size,
        color: variantForm.color,
        price: Number(variantForm.price),
        compareAtPrice: variantForm.compareAtPrice ? Number(variantForm.compareAtPrice) : null,
        stockQuantity: Number(variantForm.stockQuantity || 0),
        status: variantForm.status
      });
      await refreshDetail(selectedProductId);
      setVariantForm(createEmptyVariantForm());
      setMessage("Đã thêm biến thể mới.");
    } catch (error) {
      setMessage(extractError(error, "Không thêm được biến thể"));
    } finally {
      setSaving(null);
    }
  }

  async function handleUpdateVariant(variantId: string) {
    const draft = variantDrafts[variantId];
    if (!draft) {
      return;
    }

    try {
      setSaving(`variant-${variantId}`);
      setMessage(null);
      const updated = await updateVariant(variantId, {
        size: draft.size,
        color: draft.color,
        price: Number(draft.price),
        compareAtPrice: draft.compareAtPrice ? Number(draft.compareAtPrice) : null,
        status: draft.status
      });

      setVariantDrafts((current) => ({
        ...current,
        [variantId]: {
          ...current[variantId],
          size: updated.size ?? "",
          color: updated.color ?? "",
          price: updated.price != null ? String(updated.price) : "",
          compareAtPrice: updated.compareAtPrice != null ? String(updated.compareAtPrice) : "",
          status: updated.status,
          stockQuantity: String(updated.availableQuantity),
          sku: updated.sku
        }
      }));

      if (selectedProductId) {
        await refreshDetail(selectedProductId);
      }
      setMessage(`Đã cập nhật biến thể ${updated.sku}.`);
    } catch (error) {
      setMessage(extractError(error, "Không cập nhật được biến thể"));
    } finally {
      setSaving(null);
    }
  }

  async function handleAddImageByUrl() {
    if (!selectedProductId) {
      setMessage("Hãy tạo sản phẩm trước khi thêm ảnh.");
      return;
    }

    try {
      setSaving("image-url");
      setMessage(null);
      await addProductImage(selectedProductId, {
        imageUrl: imageForm.imageUrl,
        publicId: imageForm.publicId || null,
        altText: imageForm.altText || null,
        sortOrder: Number(imageForm.sortOrder || 0),
        isPrimary: imageForm.isPrimary
      });
      await refreshDetail(selectedProductId);
      setImageForm(createEmptyImageForm());
      setMessage("Đã thêm ảnh bằng URL.");
    } catch (error) {
      setMessage(extractError(error, "Không thêm được ảnh"));
    } finally {
      setSaving(null);
    }
  }

  async function handleUploadImage() {
    if (!selectedProductId) {
      setMessage("Hãy tạo sản phẩm trước khi upload ảnh.");
      return;
    }

    if (!uploadForm.file) {
      setMessage("Bạn chưa chọn tệp ảnh.");
      return;
    }

    try {
      setSaving("image-upload");
      setMessage(null);
      const formData = new FormData();
      formData.append("file", uploadForm.file);
      if (uploadForm.altText.trim()) {
        formData.append("altText", uploadForm.altText.trim());
      }
      formData.append("isPrimary", String(uploadForm.isPrimary));
      formData.append("sortOrder", String(Number(uploadForm.sortOrder || 0)));
      await uploadProductImage(selectedProductId, formData);
      await refreshDetail(selectedProductId);
      setUploadForm(createEmptyUploadForm());
      setMessage("Đã upload ảnh lên Cloudinary.");
    } catch (error) {
      setMessage(extractError(error, "Không upload được ảnh"));
    } finally {
      setSaving(null);
    }
  }

  async function handleDeleteImage(image: ProductImageResponse) {
    if (!selectedProductId) {
      return;
    }

    try {
      setSaving(`image-delete-${image.id}`);
      setMessage(null);
      await deleteProductImage(selectedProductId, image.id);
      await refreshDetail(selectedProductId);
      setMessage("Đã xóa ảnh khỏi sản phẩm.");
    } catch (error) {
      setMessage(extractError(error, "Không xóa được ảnh"));
    } finally {
      setSaving(null);
    }
  }

  function startCreateProduct() {
    setSelectedProductId("");
    setDetail(null);
    setSlugDirty(false);
    setProductForm(createEmptyProductForm(categories[0]?.id ?? "", brands[0]?.id ?? ""));
    setVariantForm(createEmptyVariantForm());
    setVariantDrafts({});
    setImageForm(createEmptyImageForm());
    setUploadForm(createEmptyUploadForm());
    setMessage("Bạn đang ở chế độ tạo sản phẩm mới.");
  }
  return (
    <div className="admin-grid admin-grid-2">
      <section className="card panel">
        <div className="panel-header">
          <div>
            <h2>Catalog sản phẩm</h2>
            <p className="panel-copy">Danh sách sản phẩm đang được đồng bộ từ API quản trị.</p>
          </div>
          <button className="admin-btn" type="button" onClick={startCreateProduct}>
            Tạo sản phẩm mới
          </button>
        </div>
        <div className="admin-form-grid" style={{ marginBottom: 16 }}>
          <input
            className="admin-input"
            placeholder="Tìm theo tên, SKU, danh mục hoặc thương hiệu"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />
          <select className="select" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as "all" | AdminProduct["status"] | "featured")}>
            <option value="all">Tất cả trạng thái</option>
            <option value="active">Đang bán ổn định</option>
            <option value="low">Sắp hết hàng</option>
            <option value="out">Hết hàng</option>
            <option value="draft">Bản nháp / ẩn</option>
            <option value="featured">Sản phẩm nổi bật</option>
          </select>
        </div>

        {products.length === 0 ? (
          <div className="empty-state">Hiện chưa có sản phẩm nào trong database.</div>
        ) : filteredProducts.length === 0 ? (
          <div className="empty-state">Không có sản phẩm nào khớp bộ lọc hiện tại.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Tồn</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map((product) => (
                <tr
                  key={product.id ?? product.sku}
                  className={selectedProductId && product.id === selectedProductId ? "row-selected" : ""}
                  onClick={() => product.id && setSelectedProductId(product.id)}
                >
                  <td>
                    <strong>{product.name}</strong>
                    <div className="table-subtle">
                      {product.category} · {product.brand}
                      {product.isFeatured ? " · Nổi bật" : ""}
                    </div>
                  </td>
                  <td>{product.sku}</td>
                  <td>{product.stock}</td>
                  <td>
                    <span className={`status ${product.status}`}>{product.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <div className="admin-stack">
        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>{selectedProduct ? `Chỉnh sửa: ${selectedProduct.name}` : "Tạo sản phẩm mới"}</h2>
              <p className="panel-copy">Form này gọi trực tiếp API admin product create/update.</p>
            </div>
          </div>
          {message ? <p className="action-message">{message}</p> : null}
          {detailLoading ? (
            <div className="loading-state">
              <div className="skeleton" style={{ width: "42%", marginBottom: 10 }} />
              <div className="skeleton" style={{ width: "100%", marginBottom: 8 }} />
              <div className="skeleton" style={{ width: "86%" }} />
            </div>
          ) : null}
          {missingCategorySetup || missingBrandSetup ? (
            <div className="empty-state" style={{ marginBottom: 16 }}>
              {[
                missingCategorySetup ? "Danh mục đang trống" : null,
                missingBrandSetup ? "Thương hiệu đang trống" : null
              ]
                .filter(Boolean)
                .join(" · ")}
              . Hãy tạo dữ liệu nền ở trang quản trị tương ứng rồi quay lại đây.
            </div>
          ) : null}
          <div className="admin-form-grid">
            <input className="admin-input" placeholder="Tên sản phẩm" value={productForm.name} onChange={(event) => {
              const name = event.target.value;
              setProductForm((current) => ({ ...current, name, ...(!slugDirty && { slug: toSlug(name) }) }));
            }} />
            <input className="admin-input" placeholder="Slug" value={productForm.slug} onChange={(event) => {
              setSlugDirty(true);
              setProductForm((current) => ({ ...current, slug: event.target.value }));
            }} />
            <select className="select" value={productForm.categoryId} onChange={(event) => setProductForm((current) => ({ ...current, categoryId: event.target.value }))} disabled={missingCategorySetup}>
              {missingCategorySetup ? <option value="">Chưa có danh mục</option> : null}
              {categories.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}
            </select>
            <select className="select" value={productForm.brandId} onChange={(event) => setProductForm((current) => ({ ...current, brandId: event.target.value }))} disabled={missingBrandSetup}>
              {missingBrandSetup ? <option value="">Chưa có thương hiệu</option> : null}
              {brands.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}
            </select>
            <select className="select" value={productForm.gender} onChange={(event) => setProductForm((current) => ({ ...current, gender: event.target.value }))}>
              <option value="">Không chọn giới tính</option>
              {genderOptions.map((item) => <option value={item} key={item}>{item}</option>)}
            </select>
            <select className="select" value={productForm.status} onChange={(event) => setProductForm((current) => ({ ...current, status: event.target.value }))}>
              {productStatusOptions.map((item) => <option value={item} key={item}>{item}</option>)}
            </select>
            <input className="admin-input" placeholder="Môn thể thao" value={productForm.sportType} onChange={(event) => setProductForm((current) => ({ ...current, sportType: event.target.value }))} />
            <label className="admin-check">
              <input type="checkbox" checked={productForm.isFeatured} onChange={(event) => setProductForm((current) => ({ ...current, isFeatured: event.target.checked }))} />
              Sản phẩm nổi bật
            </label>
            <div className="admin-form-full">
              <input className="admin-input" placeholder="Mô tả ngắn" value={productForm.shortDescription} onChange={(event) => setProductForm((current) => ({ ...current, shortDescription: event.target.value }))} />
            </div>
            <div className="admin-form-full">
              <textarea className="admin-textarea" placeholder="Mô tả chi tiết" value={productForm.description} onChange={(event) => setProductForm((current) => ({ ...current, description: event.target.value }))} />
            </div>
          </div>
          <div className="page-actions">
            {selectedProductId && (
              <button className="admin-btn secondary" type="button" onClick={() => void handleDeleteProduct()} disabled={saving !== null}>
                {saving === "product-delete" ? "Đang xóa..." : "Xóa sản phẩm"}
              </button>
            )}
            <button className="admin-btn" type="button" onClick={() => void handleCreateOrUpdateProduct()} disabled={!canSubmitProduct}>
              {saving === "product" ? "Đang lưu..." : selectedProductId ? "Cập nhật sản phẩm" : "Tạo sản phẩm"}
            </button>
          </div>
        </section>

        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Biến thể</h2>
              <p className="panel-copy">Thêm biến thể mới tại đây. Chỉnh tồn kho sau tạo dùng module tồn kho.</p>
            </div>
          </div>
          <div className="admin-form-grid">
            <input className="admin-input" placeholder="SKU" value={variantForm.sku} onChange={(event) => setVariantForm((current) => ({ ...current, sku: event.target.value }))} />
            <input className="admin-input" placeholder="Size" value={variantForm.size} onChange={(event) => setVariantForm((current) => ({ ...current, size: event.target.value }))} />
            <input className="admin-input" placeholder="Màu sắc" value={variantForm.color} onChange={(event) => setVariantForm((current) => ({ ...current, color: event.target.value }))} />
            <input className="admin-input" type="number" min={0} placeholder="Giá bán" value={variantForm.price} onChange={(event) => setVariantForm((current) => ({ ...current, price: event.target.value }))} />
            <input className="admin-input" type="number" min={0} placeholder="Giá so sánh" value={variantForm.compareAtPrice} onChange={(event) => setVariantForm((current) => ({ ...current, compareAtPrice: event.target.value }))} />
            <input className="admin-input" type="number" min={0} placeholder="Tồn ban đầu" value={variantForm.stockQuantity} onChange={(event) => setVariantForm((current) => ({ ...current, stockQuantity: event.target.value }))} />
            <select className="select" value={variantForm.status} onChange={(event) => setVariantForm((current) => ({ ...current, status: event.target.value }))}>
              {variantStatusOptions.map((item) => <option value={item} key={item}>{item}</option>)}
            </select>
          </div>
          <div className="page-actions">
            <button className="admin-btn" type="button" onClick={() => void handleCreateVariant()} disabled={saving === "variant-create"}>
              {saving === "variant-create" ? "Đang thêm..." : "Thêm biến thể"}
            </button>
          </div>
          {detail?.variants.length ? (
            <div className="admin-stack">
              {detail.variants.map((variant) => {
                const draft = variantDrafts[variant.id];
                if (!draft) return null;
                return <VariantEditorCard key={variant.id} variant={variant} draft={draft} saving={saving === `variant-${variant.id}`} onDraftChange={(patch) => setVariantDrafts((current) => ({ ...current, [variant.id]: { ...current[variant.id], ...patch } }))} onSave={() => void handleUpdateVariant(variant.id)} />;
              })}
            </div>
          ) : <div className="empty-state">Sản phẩm này chưa có biến thể.</div>}
        </section>

        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Ảnh sản phẩm</h2>
              <p className="panel-copy">Hỗ trợ cả thêm URL có sẵn và upload ảnh thật.</p>
            </div>
          </div>
          <div className="admin-form-grid">
            <div className="admin-form-full">
              <input className="admin-input" placeholder="Image URL" value={imageForm.imageUrl} onChange={(event) => setImageForm((current) => ({ ...current, imageUrl: event.target.value }))} />
            </div>
            <input className="admin-input" placeholder="Cloudinary publicId (nếu có)" value={imageForm.publicId} onChange={(event) => setImageForm((current) => ({ ...current, publicId: event.target.value }))} />
            <input className="admin-input" placeholder="Alt text" value={imageForm.altText} onChange={(event) => setImageForm((current) => ({ ...current, altText: event.target.value }))} />
            <input className="admin-input" type="number" min={0} placeholder="Sort order" value={imageForm.sortOrder} onChange={(event) => setImageForm((current) => ({ ...current, sortOrder: event.target.value }))} />
            <label className="admin-check">
              <input type="checkbox" checked={imageForm.isPrimary} onChange={(event) => setImageForm((current) => ({ ...current, isPrimary: event.target.checked }))} />
              Đặt làm ảnh chính
            </label>
          </div>
          <div className="page-actions">
            <button className="admin-btn secondary" type="button" onClick={() => void handleAddImageByUrl()} disabled={saving === "image-url"}>
              {saving === "image-url" ? "Đang thêm..." : "Thêm ảnh bằng URL"}
            </button>
          </div>
          <div className="admin-form-grid">
            <input className="admin-input admin-form-full" type="file" accept="image/*" onChange={(event) => setUploadForm((current) => ({ ...current, file: event.target.files?.[0] ?? null }))} />
            <input className="admin-input" placeholder="Alt text upload" value={uploadForm.altText} onChange={(event) => setUploadForm((current) => ({ ...current, altText: event.target.value }))} />
            <input className="admin-input" type="number" min={0} placeholder="Sort order" value={uploadForm.sortOrder} onChange={(event) => setUploadForm((current) => ({ ...current, sortOrder: event.target.value }))} />
            <label className="admin-check">
              <input type="checkbox" checked={uploadForm.isPrimary} onChange={(event) => setUploadForm((current) => ({ ...current, isPrimary: event.target.checked }))} />
              Ảnh chính khi upload
            </label>
          </div>
          <div className="page-actions">
            <button className="admin-btn" type="button" onClick={() => void handleUploadImage()} disabled={saving === "image-upload"}>
              {saving === "image-upload" ? "Đang upload..." : "Upload ảnh"}
            </button>
          </div>
          {detail?.images.length ? (
            <div className="admin-gallery">
              {detail.images.map((image) => <ProductImageCard key={image.id} image={image} productName={detail.name} saving={saving === `image-delete-${image.id}`} onDelete={() => void handleDeleteImage(image)} />)}
            </div>
          ) : <div className="empty-state">Sản phẩm này chưa có ảnh.</div>}
        </section>
      {selectedProductId ? (
        <section className="card panel">
          <div className="panel-header">
            <div>
              <h2>Bộ sưu tập</h2>
              <p className="panel-copy">Gắn sản phẩm vào các bộ sưu tập để hiển thị trên storefront.</p>
            </div>
          </div>

          {collectionMessage ? <p className="action-message">{collectionMessage}</p> : null}

          <div style={{ marginBottom: 20 }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>
              Đang thuộc bộ sưu tập ({productCollections.length})
            </h3>
            {collectionsLoading && productCollections.length === 0 ? (
              <div className="loading-state">
                <div className="skeleton" style={{ width: "60%", marginBottom: 8 }} />
                <div className="skeleton" style={{ width: "80%" }} />
              </div>
            ) : productCollections.length === 0 ? (
              <div className="empty-state">Sản phẩm này chưa thuộc bộ sưu tập nào.</div>
            ) : (
              <div className="admin-gallery">
                {productCollections.map((c) => (
                  <article key={c.id} className="admin-image-card">
                    {c.coverImageUrl ? (
                      <Image
                        src={c.coverImageUrl}
                        alt={c.name}
                        width={96}
                        height={96}
                        style={{ width: 96, height: 96, borderRadius: 12, objectFit: "cover" }}
                        unoptimized
                      />
                    ) : (
                      <div style={{ width: 96, height: 96, borderRadius: 12, background: "var(--admin-line)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 32, flexShrink: 0 }}>
                        🗂
                      </div>
                    )}
                    <div>
                      <strong style={{ display: "block", marginBottom: 2 }}>{c.name}</strong>
                      <div className="table-subtle">
                        {[c.collectionType, c.season, c.year].filter(Boolean).join(" · ")}
                      </div>
                      <div className="table-subtle">{c.slug}</div>
                      <span className={`status-pill status-pill-${collectionStatusTone(c.status)}`} style={{ marginTop: 4, display: "inline-block" }}>
                        {collectionStatusLabel(c.status)}
                      </span>
                    </div>
                    <button
                      className="admin-btn secondary"
                      type="button"
                      onClick={() => void handleRemoveFromCollection(c.id)}
                    >
                      Gỡ khỏi bộ sưu tập
                    </button>
                  </article>
                ))}
              </div>
            )}
          </div>

          <div>
            <h3 style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>Thêm vào bộ sưu tập</h3>
            <input
              className="admin-input"
              placeholder="Tìm bộ sưu tập để thêm..."
              value={collectionSearch}
              style={{ width: "100%", marginBottom: 12 }}
              onFocus={() => void ensureAllCollectionsLoaded()}
              onChange={(event) => {
                void ensureAllCollectionsLoaded();
                setCollectionSearch(event.target.value);
              }}
            />
            {collectionsPickerLoading ? (
              <div className="loading-state">
                <div className="skeleton" style={{ width: "70%", marginBottom: 8 }} />
                <div className="skeleton" style={{ width: "85%", marginBottom: 8 }} />
                <div className="skeleton" style={{ width: "60%" }} />
              </div>
            ) : filteredAvailableCollections.length === 0 ? (
              <div className="empty-state">
                {allCollections.length === 0
                  ? "Bấm vào ô tìm kiếm để tải danh sách bộ sưu tập."
                  : "Không có bộ sưu tập nào khả dụng để thêm."}
              </div>
            ) : (
              <div className="admin-gallery">
                {filteredAvailableCollections.map((c) => (
                  <article key={c.id} className="admin-image-card">
                    {c.coverImageUrl ? (
                      <Image
                        src={c.coverImageUrl}
                        alt={c.name}
                        width={96}
                        height={96}
                        style={{ width: 96, height: 96, borderRadius: 12, objectFit: "cover" }}
                        unoptimized
                      />
                    ) : (
                      <div style={{ width: 96, height: 96, borderRadius: 12, background: "var(--admin-line)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 32, flexShrink: 0 }}>
                        🗂
                      </div>
                    )}
                    <div>
                      <strong style={{ display: "block", marginBottom: 2 }}>{c.name}</strong>
                      <div className="table-subtle">
                        {[c.collectionType, c.season, c.year].filter(Boolean).join(" · ")}
                      </div>
                      <div className="table-subtle">{c.slug}</div>
                      <span className={`status-pill status-pill-${collectionStatusTone(c.status)}`} style={{ marginTop: 4, display: "inline-block" }}>
                        {collectionStatusLabel(c.status)}
                      </span>
                    </div>
                    <button
                      className="admin-btn"
                      type="button"
                      onClick={() => void handleAddToCollection(c)}
                    >
                      Thêm vào
                    </button>
                  </article>
                ))}
              </div>
            )}
          </div>
        </section>
      ) : null}
      </div>
    </div>
  );
}
