"use client";

import { useRef, useState } from "react";
import { LeftArrowIcon, RightArrowIcon } from "@/components/shared/icons";
import { ProductCard } from "@/modules/product/components/ProductCard";
import { cn } from "@/lib/utils";
import type { Product } from "@/modules/product/types";

interface ProductRailProps {
  title: string;
  tabs?: { id: string; label: string; products: Product[] }[];
  products?: Product[];
}

export function ProductRail({ title, tabs, products }: ProductRailProps) {
  const [activeTabId, setActiveTabId] = useState(tabs?.[0]?.id);
  const trackRef = useRef<HTMLDivElement>(null);

  const activeProducts = tabs ? (tabs.find((tab) => tab.id === activeTabId) ?? tabs[0])?.products : products;
  const visibleProducts = activeProducts ?? [];
  const hasProducts = visibleProducts.length > 0;

  const scrollByAmount = (direction: 1 | -1) => {
    const el = trackRef.current;
    if (!el) return;
    el.scrollBy({ left: el.clientWidth * 0.9 * direction, behavior: "smooth" });
  };

  return (
    <section className="relative left-1/2 right-1/2 mb-16 w-screen -translate-x-1/2 px-4 md:px-8 xl:px-[68px]">
      <div className="title-section mb-8 text-center">
        <h2 className="inline-block border-b border-ivy-dark pb-2 font-[Montserrat,sans-serif] text-[18px] font-light uppercase tracking-wide text-ivy-dark md:text-[28px]">
          {title}
        </h2>
      </div>

      {tabs ? (
        <div className="mb-10 flex justify-center">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTabId(tab.id)}
              className={cn(
                "mr-12 inline-block cursor-pointer pb-1 text-[18px] leading-[30px] text-ivy-text-muted last:mr-0 md:text-[20px]",
                activeTabId === tab.id && "border-b-2 border-ivy-dark text-ivy-dark",
              )}
            >
              {tab.label}
            </button>
          ))}
        </div>
      ) : null}

      {hasProducts ? (
        <div className="relative min-h-[100px]">
          <div
            ref={trackRef}
            className="flex gap-5 overflow-x-auto scroll-smooth md:gap-7 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
            style={{ scrollbarWidth: "none" }}
          >
            {visibleProducts.map((product) => (
              <div
                key={product.id}
                className="w-[calc((100%-1.25rem)/2)] shrink-0 sm:w-[calc((100%-2.5rem)/3)] md:w-[calc((100%-5.25rem)/4)] lg:w-[calc((100%-7rem)/5)]"
              >
                <ProductCard product={product} />
              </div>
            ))}
          </div>

          <button
            type="button"
            aria-label="Sản phẩm trước"
            onClick={() => scrollByAmount(-1)}
            className="absolute left-3 top-[38%] flex h-[30px] w-[30px] -translate-y-1/2 items-center justify-center rounded-sm text-[#BCBDC0] transition-all duration-300 ease-in-out hover:text-ivy-dark md:left-[8px] xl:left-[12px]"
          >
            <LeftArrowIcon className="h-8 w-8" />
          </button>
          <button
            type="button"
            aria-label="Sản phẩm tiếp theo"
            onClick={() => scrollByAmount(1)}
            className="absolute right-3 top-[38%] flex h-[30px] w-[30px] -translate-y-1/2 items-center justify-center rounded-sm text-[#BCBDC0] transition-all duration-300 ease-in-out hover:text-ivy-dark md:right-[8px] xl:right-[12px]"
          >
            <RightArrowIcon className="h-8 w-8" />
          </button>
        </div>
      ) : (
        <div className="py-10 text-center text-sm text-ivy-text-muted">
          Chưa có sản phẩm để hiển thị.
        </div>
      )}
    </section>
  );
}

