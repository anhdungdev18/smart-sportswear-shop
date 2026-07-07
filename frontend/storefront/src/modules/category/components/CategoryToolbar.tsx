"use client";

import { startTransition, useMemo, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { cn } from "@/lib/utils";
import { ChevronDownIcon } from "@/components/shared/icons";

type SortOption = {
  label: string;
  value?: string;
};

const SORT_OPTIONS: SortOption[] = [
  { label: "Mặc định" },
  { label: "Mới nhất", value: "newest" },
  { label: "Bán chạy nhất", value: "bestselling" },
  { label: "Giá: cao đến thấp", value: "price_desc" },
  { label: "Giá: thấp đến cao", value: "price_asc" },
];

export function CategoryToolbar({
  title,
  resultCount,
  currentSort,
}: {
  title: string;
  resultCount?: number;
  currentSort?: string;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [isPending, setIsPending] = useState(false);
  const pathname = usePathname();
  const router = useRouter();
  const searchParams = useSearchParams();

  const selectedSort = useMemo(
    () => SORT_OPTIONS.find((option) => option.value === currentSort)?.label ?? SORT_OPTIONS[0].label,
    [currentSort],
  );

  function applySort(sortValue?: string) {
    const params = new URLSearchParams(searchParams.toString());
    params.delete("page");
    params.delete("sort");
    params.delete("sortBy");
    params.delete("sortOrder");

    if (sortValue) {
      params.set("sort", sortValue);
    }

    const query = params.toString();
    setIsPending(true);
    startTransition(() => {
      router.push(query ? `${pathname}?${query}` : pathname, { scroll: false });
    });
    setIsOpen(false);
  }

  return (
    <div className="relative mb-8 border-b border-ivy-hairline pb-5">
      <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-[28px] leading-[1.1] font-semibold uppercase tracking-[0.05em] text-ivy-dark md:text-[34px]">
            {title}
          </h1>
          {typeof resultCount === "number" ? (
            <p className="mt-2 text-[14px] text-ivy-text">{resultCount.toLocaleString("vi-VN")} sản phẩm</p>
          ) : null}
        </div>

        <div className="relative w-fit">
          <button
            type="button"
            onClick={() => setIsOpen((prev) => !prev)}
            onBlur={() => setTimeout(() => setIsOpen(false), 120)}
            disabled={isPending}
            className="flex h-10 min-w-[220px] items-center justify-between gap-4 border border-ivy-hairline px-4 text-[14px] text-ivy-dark disabled:cursor-wait disabled:opacity-70"
          >
            <span>{isPending ? "Đang tải..." : `Sắp xếp: ${selectedSort}`}</span>
            <ChevronDownIcon className={cn("size-4 transition-transform", isOpen && "rotate-180")} />
          </button>

          {isOpen ? (
            <div className="absolute right-0 z-20 mt-1 min-w-[220px] border border-ivy-hairline bg-white shadow-sm">
              {SORT_OPTIONS.map((option) => (
                <button
                  key={option.label}
                  type="button"
                  onMouseDown={(e) => e.preventDefault()}
                  onClick={() => applySort(option.value)}
                  className={cn(
                    "block w-full px-4 py-2.5 text-left text-[14px] text-ivy-text hover:bg-gray-50",
                    selectedSort === option.label && "font-semibold text-ivy-dark",
                  )}
                >
                  {option.label}
                </button>
              ))}
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}

