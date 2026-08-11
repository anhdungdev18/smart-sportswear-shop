"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { LeftArrowIcon, RightArrowIcon } from "@/components/shared/icons";
import { ProductCard } from "@/modules/product/components/ProductCard";
import type { ActivePromotion, Product } from "@/modules/product/types";

function millisecondsUntil(value: string | null | undefined) {
  if (!value) return null;
  const end = new Date(value).getTime();
  if (Number.isNaN(end)) return null;
  return Math.max(0, end - Date.now());
}

function formatTime(value: number) {
  return String(value).padStart(2, "0");
}

export function HomeFlashSale({ products, promotion }: { products: Product[]; promotion: ActivePromotion | null }) {
  const trackRef = useRef<HTMLDivElement>(null);
  const [remaining, setRemaining] = useState<number | null>(() => millisecondsUntil(promotion?.endsAt));

  useEffect(() => {
    const update = () => setRemaining(millisecondsUntil(promotion?.endsAt));
    update();
    const timer = window.setInterval(update, 1000);
    return () => window.clearInterval(timer);
  }, [promotion?.endsAt]);

  if (products.length === 0) return null;

  const hours = remaining == null ? 0 : Math.floor(remaining / 3_600_000);
  const minutes = remaining == null ? 0 : Math.floor((remaining % 3_600_000) / 60_000);
  const seconds = remaining == null ? 0 : Math.floor((remaining % 60_000) / 1000);
  const scroll = (direction: 1 | -1) => {
    const track = trackRef.current;
    if (track) track.scrollBy({ left: track.clientWidth * 0.9 * direction, behavior: "smooth" });
  };

  return (
    <section className="relative left-1/2 right-1/2 mb-16 w-screen -translate-x-1/2 bg-[#f8f2f0] py-9 md:py-12">
      <div className="mx-auto px-4 md:px-8 xl:px-[68px]">
        <div className="mb-8 flex flex-col items-center justify-between gap-5 md:flex-row">
          <div className="text-center md:text-left">
            <p className="mb-2 text-[11px] font-semibold uppercase tracking-[0.28em] text-[#ac2f33]">
              {promotion ? promotion.name : "Ưu đãi đang chạy"}
            </p>
            <h2 className="text-[24px] font-semibold uppercase tracking-[0.08em] text-ivy-dark md:text-[30px]">Flash Sale</h2>
          </div>
          <div
            className="flex items-center gap-3"
            aria-label={remaining == null ? "Khuyến mãi đang chạy" : `Kết thúc sau ${hours} giờ ${minutes} phút ${seconds} giây`}
          >
            <span className="text-xs uppercase tracking-[0.12em] text-ivy-text-muted">
              {remaining == null ? "Đang áp dụng" : "Kết thúc sau"}
            </span>
            {remaining == null ? (
              <span className="rounded-sm bg-ivy-dark px-3 py-2 text-xs font-semibold uppercase tracking-[0.12em] text-white">
                Không giới hạn
              </span>
            ) : (
              [hours, minutes, seconds].map((value, index) => (
                <div key={index} className="flex items-center gap-2">
                  <span className="min-w-11 rounded-sm bg-ivy-dark px-2 py-2 text-center text-base font-semibold tabular-nums text-white">
                    {formatTime(value)}
                  </span>
                  {index < 2 ? <span className="font-semibold text-ivy-dark">:</span> : null}
                </div>
              ))
            )}
          </div>
        </div>

        <div className="relative">
          <div ref={trackRef} className="flex gap-5 overflow-x-auto scroll-smooth md:gap-7 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
            {products.map((product) => (
              <div key={product.id} className="w-[calc((100%-1.25rem)/2)] shrink-0 sm:w-[calc((100%-2.5rem)/3)] md:w-[calc((100%-5.25rem)/4)] lg:w-[calc((100%-7rem)/5)]">
                <ProductCard product={product} />
              </div>
            ))}
          </div>
          <button type="button" aria-label="Sản phẩm trước" onClick={() => scroll(-1)} className="absolute left-1 top-[38%] hidden -translate-y-1/2 text-[#8c8d91] hover:text-ivy-dark md:block">
            <LeftArrowIcon className="h-8 w-8" />
          </button>
          <button type="button" aria-label="Sản phẩm tiếp theo" onClick={() => scroll(1)} className="absolute right-1 top-[38%] hidden -translate-y-1/2 text-[#8c8d91] hover:text-ivy-dark md:block">
            <RightArrowIcon className="h-8 w-8" />
          </button>
        </div>

        <div className="mt-8 text-center">
          <Link href="/tim-kiem?discount=any" className="inline-flex rounded-tl-[18px] rounded-br-[18px] border border-ivy-dark px-7 py-3 text-xs font-semibold uppercase tracking-[0.12em] transition-colors hover:bg-ivy-dark hover:text-white">
            Xem tất cả ưu đãi
          </Link>
        </div>
      </div>
    </section>
  );
}
