"use client";

import { useRef } from "react";
import Image from "next/image";
import { LeftArrowIcon, RightArrowIcon } from "@/components/shared/icons";
import { BRAND_PROMO_ITEMS } from "@/modules/content/data/marketing";

export function BrandPromoCarousel() {
  const scrollRef = useRef<HTMLDivElement>(null);

  function scrollByAmount(direction: 1 | -1) {
    const el = scrollRef.current;
    if (!el) return;
    el.scrollBy({ left: el.clientWidth * 0.9 * direction, behavior: "smooth" });
  }

  return (
    <section className="mb-24 mt-4">
      <div className="mb-8 text-center">
        <h2 className="inline-block border-b border-ivy-dark pb-2 font-[Montserrat,sans-serif] text-[18px] font-light uppercase tracking-wide text-ivy-dark md:text-[28px]">
          Ưu đãi đặc biệt
        </h2>
      </div>

      <div className="relative mx-auto max-w-342 px-4 md:px-0">
        <div
          ref={scrollRef}
          className="flex snap-x snap-mandatory gap-7 overflow-x-auto scroll-smooth [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        >
          {BRAND_PROMO_ITEMS.map((item) => (
            <a
              key={item.alt}
              href={item.href}
              className="relative aspect-660/380 w-[85%] shrink-0 snap-center overflow-hidden bg-gray-100 md:w-[calc(50%-14px)]"
            >
              {item.src ? (
                <Image
                  src={item.src}
                  alt={item.alt}
                  fill
                  sizes="(max-width: 768px) 85vw, 660px"
                  className="object-cover"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center text-[13px] text-gray-400">
                  Chưa có ảnh
                </div>
              )}
            </a>
          ))}
        </div>

        <button
          type="button"
          aria-label="Xem trước"
          onClick={() => scrollByAmount(-1)}
          className="absolute left-0 top-1/2 hidden -translate-y-1/2 items-center justify-center text-gray-300 transition hover:text-ivy-dark md:flex"
        >
          <LeftArrowIcon className="h-8 w-8" />
        </button>
        <button
          type="button"
          aria-label="Xem tiếp"
          onClick={() => scrollByAmount(1)}
          className="absolute right-0 top-1/2 hidden -translate-y-1/2 items-center justify-center text-gray-300 transition hover:text-ivy-dark md:flex"
        >
          <RightArrowIcon className="h-8 w-8" />
        </button>
      </div>
    </section>
  );
}
