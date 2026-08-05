import Link from "next/link";

const LABELS: Record<string, string> = {
  gender: "Giới tính",
  surface: "Mặt sân",
  color: "Màu",
  size: "Size",
  minPrice: "Từ",
  maxPrice: "Dưới",
  brandSlug: "Thương hiệu",
  categorySlug: "Danh mục",
  brand: "Thương hiệu",
  category: "Danh mục",
  productType: "Loại sản phẩm",
  sportType: "Môn thể thao",
  colorFamily: "Màu",
};

export function SearchFilterChips({
  params,
  parsedQuery,
}: {
  params: Record<string, string | undefined>;
  parsedQuery?: Record<string, unknown>;
}) {
  const chips = Object.entries(params).filter(([key, value]) => LABELS[key] && value);
  const inferred = Object.entries(parsedQuery ?? {}).filter(
    ([key, value]) =>
      LABELS[key] &&
      value != null &&
      value !== "" &&
      !params[key] &&
      !["normalized", "semanticText", "featureHints"].includes(key),
  );
  if (!chips.length && !inferred.length) return null;
  return (
    <div className="mb-6 flex flex-wrap gap-2" aria-label="Bộ lọc đang áp dụng">
      {chips.map(([key, value]) => {
        const next = new URLSearchParams();
        Object.entries(params).forEach(([name, item]) => {
          if (item && name !== key && name !== "page") next.set(name, item);
        });
        return (
          <Link
            key={key}
            href={`/tim-kiem?${next.toString()}`}
            className="rounded-full border border-gray-300 px-3 py-1.5 text-xs text-gray-700 hover:border-gray-600"
          >
            {LABELS[key]}: {key.toLowerCase().includes("price") ? Number(value).toLocaleString("vi-VN") + "đ" : value} ×
          </Link>
        );
      })}
      {inferred.map(([key, value]) => (
        <span
          key={`parsed:${key}`}
          title="Bộ lọc được hiểu từ câu tìm kiếm"
          className="rounded-full border border-blue-200 bg-blue-50 px-3 py-1.5 text-xs text-blue-800"
        >
          {LABELS[key]}: {String(value)}
        </span>
      ))}
    </div>
  );
}
