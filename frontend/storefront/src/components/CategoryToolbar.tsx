"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import { ChevronDownIcon } from "@/components/icons";

const SORT_OPTIONS = [
  "Mặc định",
  "Mới nhất",
  "Được mua nhiều nhất",
  "Được yêu thích nhất",
  "Giá: cao đến thấp",
  "Giá: thấp đến cao",
] as const;

export function CategoryToolbar({
  title,
  resultCount,
}: {
  title: string;
  resultCount?: number;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedSort, setSelectedSort] = useState<string>(SORT_OPTIONS[0]);

  return (
    <div className="relative mb-[26px]">
      <h1 className="text-2xl leading-8 font-semibold uppercase text-ivy-dark">
        {title}
      </h1>
      {typeof resultCount === "number" ? (
        <p className="mt-1 text-sm text-ivy-text">
          {resultCount.toLocaleString("vi-VN")} sản phẩm
        </p>
      ) : null}

      <div className="static mt-2 md:absolute md:top-0 md:right-0 md:mt-0">
        <button
          type="button"
          onClick={() => setIsOpen((prev) => !prev)}
          onBlur={() => setIsOpen(false)}
          className="flex cursor-pointer items-center gap-1 text-sm text-ivy-dark"
        >
          Sắp xếp theo
          <ChevronDownIcon
            className={cn(
              "size-4 transition-transform",
              isOpen && "rotate-180"
            )}
          />
        </button>

        {isOpen ? (
          <div className="absolute right-0 z-20 mt-1 min-w-[220px] rounded border border-ivy-hairline bg-white shadow-md">
            {SORT_OPTIONS.map((option) => (
              <button
                key={option}
                type="button"
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => {
                  setSelectedSort(option);
                  setIsOpen(false);
                }}
                className={cn(
                  "block w-full cursor-pointer px-4 py-2 text-left text-sm text-ivy-text hover:bg-gray-50",
                  selectedSort === option && "font-semibold text-ivy-dark"
                )}
              >
                {selectedSort === option ? "✓ " : null}
                {option}
              </button>
            ))}
          </div>
        ) : null}
      </div>
    </div>
  );
}
