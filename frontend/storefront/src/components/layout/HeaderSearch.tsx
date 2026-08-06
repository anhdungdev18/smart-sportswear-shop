"use client";

import Image from "next/image";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { SearchIcon } from "@/components/shared/icons";
import { VisualSearchDialog } from "@/modules/visual-search/VisualSearchDialog";
import { API_BASE } from "@/lib/api";

type Suggestion = {
  type: "CATEGORY" | "BRAND" | "PRODUCT";
  label: string;
  value: string;
  slug: string;
  thumbnail?: string;
  minPrice?: number;
  subtitle?: string;
};

export function HeaderSearch() {
  const router = useRouter();
  const rootRef = useRef<HTMLDivElement>(null);
  const [query, setQuery] = useState("");
  const [items, setItems] = useState<Suggestion[]>([]);
  const [active, setActive] = useState(-1);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (query.trim().length < 2) {
      setItems([]);
      return;
    }
    const controller = new AbortController();
    const timer = window.setTimeout(async () => {
      setLoading(true);
      try {
        const response = await fetch(
          `${API_BASE}/api/v1/products/search-suggestions?q=${encodeURIComponent(query.trim())}`,
          { signal: controller.signal },
        );
        const payload = await response.json();
        setItems(response.ok ? (payload.data ?? []).slice(0, 10) : []);
        setOpen(response.ok);
        setActive(-1);
      } catch {
        if (!controller.signal.aborted) setItems([]);
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }, 275);
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [query]);

  useEffect(() => {
    const close = (event: MouseEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  function submit() {
    const value = query.trim();
    if (active >= 0 && items[active]) {
      navigateSuggestion(items[active]);
    } else if (value) {
      router.push(`/tim-kiem?q=${encodeURIComponent(value)}`);
    }
    setOpen(false);
  }

  function navigateSuggestion(item: Suggestion) {
    if (item.type === "PRODUCT") router.push(`/sanpham/${item.slug}`);
    else if (item.type === "CATEGORY") router.push(`/danh-muc/${item.slug}`);
    else router.push(`/tim-kiem?q=${encodeURIComponent(item.label)}&brandSlug=${encodeURIComponent(item.slug)}`);
    setOpen(false);
  }

  return (
    <div ref={rootRef} className="relative">
      <form
        action="/tim-kiem"
        method="get"
        onSubmit={(event) => {
          event.preventDefault();
          submit();
        }}
        className="flex h-9.5 w-full items-center gap-2 rounded-lg border border-ivy-hairline bg-white px-4 text-[12px] text-ivy-text md:w-120 lg:w-150"
      >
        <input
          name="q"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onFocus={() => items.length && setOpen(true)}
          onKeyDown={(event) => {
            if (event.key === "ArrowDown") {
              event.preventDefault();
              setActive((value) => Math.min(value + 1, items.length - 1));
            } else if (event.key === "ArrowUp") {
              event.preventDefault();
              setActive((value) => Math.max(value - 1, -1));
            } else if (event.key === "Escape") {
              setOpen(false);
            }
          }}
          autoComplete="off"
          role="combobox"
          aria-label="Tìm kiếm sản phẩm"
          aria-expanded={open}
          aria-controls="header-search-suggestions"
          placeholder="TÌM KIẾM SẢN PHẨM"
          className="min-w-0 flex-1 bg-transparent text-[12px] tracking-[0.01em] text-ivy-text placeholder:text-[#8b8c91] outline-none"
        />
        <button type="submit" className="flex items-center justify-center text-ivy-dark" aria-label="Tìm kiếm">
          <SearchIcon className="size-3.75" />
        </button>
        <span className="h-5 w-px bg-ivy-hairline" aria-hidden="true" />
        <VisualSearchDialog />
      </form>
      {open ? (
        <div
          id="header-search-suggestions"
          role="listbox"
          className="absolute inset-x-0 top-full z-50 mt-1 overflow-hidden rounded-lg border border-ivy-hairline bg-white shadow-lg"
        >
          <p className="px-4 py-2 text-[11px] font-semibold uppercase tracking-wider text-gray-400">Gợi ý</p>
          {loading ? <p className="px-4 py-3 text-sm text-gray-500">Đang tìm…</p> : null}
          {!loading && items.length === 0 ? (
            <p className="px-4 py-3 text-sm text-gray-500">Không có gợi ý phù hợp.</p>
          ) : null}
          {items.map((item, index) => (
            <button
              key={`${item.type}:${item.value}`}
              type="button"
              role="option"
              aria-selected={index === active}
              onMouseEnter={() => setActive(index)}
              onClick={() => navigateSuggestion(item)}
              className={`flex w-full items-center gap-3 px-4 py-2 text-left text-sm ${
                index === active ? "bg-gray-100" : "hover:bg-gray-50"
              }`}
            >
              {item.thumbnail ? (
                <Image src={item.thumbnail} alt="" width={40} height={40} className="size-10 object-cover" />
              ) : (
                <span className="size-10 bg-gray-100" />
              )}
              <span className="min-w-0 flex-1">
                <span className="block line-clamp-2">{item.label}</span>
                <span className="block truncate text-xs text-gray-500">
                  {item.subtitle}
                  {item.minPrice ? ` · từ ${item.minPrice.toLocaleString("vi-VN")}đ` : ""}
                </span>
              </span>
            </button>
          ))}
          {query.trim() ? (
            <button type="button" onClick={submit} className="w-full border-t px-4 py-3 text-left text-sm font-semibold">
              Xem tất cả kết quả cho “{query.trim()}”
            </button>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
