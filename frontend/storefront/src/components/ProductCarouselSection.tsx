"use client";

import { useRef, useState } from "react";
import { cn } from "@/lib/utils";
import { ProductCard } from "@/components/ProductCard";
import { LeftArrowIcon, RightArrowIcon } from "@/components/icons";
import type { Product } from "@/types/ivy";

interface ProductCarouselSectionProps {
  title: string;
  tabs?: { id: string; label: string; products: Product[] }[];
  products?: Product[];
}

export function ProductCarouselSection({
  title,
  tabs,
  products,
}: ProductCarouselSectionProps) {
  const [activeTabId, setActiveTabId] = useState(tabs?.[0]?.id);
  const trackRef = useRef<HTMLDivElement>(null);

  const activeProducts = tabs
    ? (tabs.find((tab) => tab.id === activeTabId) ?? tabs[0])?.products
    : products;

  const scrollByAmount = (direction: 1 | -1) => {
    const el = trackRef.current;
    if (!el) return;
    el.scrollBy({ left: el.clientWidth * 0.9 * direction, behavior: "smooth" });
  };

  return (
    <section className="mb-10">
      <div className="title-section">
        <h2 className="mb-5 font-[Montserrat,sans-serif] text-[30px] leading-[32px] font-semibold tracking-[2px] text-ivy-dark uppercase">
          {title}
        </h2>
      </div>

      <div className="exclusive-tabs">
        {tabs ? (
          <div className="mb-7 flex justify-center">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTabId(tab.id)}
                className={cn(
                  "mr-14 inline-block cursor-pointer pb-0.5 text-xl leading-[30px] text-ivy-text-muted last:mr-0",
                  activeTabId === tab.id &&
                    "border-b-2 border-ivy-dark text-ivy-dark"
                )}
              >
                {tab.label}
              </button>
            ))}
          </div>
        ) : null}

        <div className="relative min-h-[100px]">
          {activeProducts ? (
            <div className="relative">
              <div
                ref={trackRef}
                className="flex gap-[30px] overflow-x-auto scroll-smooth [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
                style={{ scrollbarWidth: "none" }}
              >
                {activeProducts.map((product) => (
                  <div key={product.id} className="w-[200px] shrink-0 md:w-[246px]">
                    <ProductCard product={product} />
                  </div>
                ))}
              </div>

              <button
                type="button"
                aria-label="Sản phẩm trước"
                onClick={() => scrollByAmount(-1)}
                className="absolute top-1/2 left-[39px] -mt-[65px] flex h-[30px] w-[30px] -translate-y-1/2 cursor-pointer items-center justify-center rounded-sm text-[#BCBDC0] transition-all duration-300 ease-in-out hover:text-ivy-dark"
              >
                <LeftArrowIcon className="h-8 w-8" />
              </button>
              <button
                type="button"
                aria-label="Sản phẩm tiếp theo"
                onClick={() => scrollByAmount(1)}
                className="absolute top-1/2 right-[39px] -mt-[65px] flex h-[30px] w-[30px] -translate-y-1/2 cursor-pointer items-center justify-center rounded-sm text-[#BCBDC0] transition-all duration-300 ease-in-out hover:text-ivy-dark"
              >
                <RightArrowIcon className="h-8 w-8" />
              </button>
            </div>
          ) : null}
        </div>
      </div>
    </section>
  );
}
