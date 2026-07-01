"use client";

import { useRef } from "react";
import Image from "next/image";
import { LeftArrowIcon, RightArrowIcon } from "@/components/icons";

interface PromoItem {
  src: string;
  alt: string;
  href: string;
}

const PROMO_ITEMS: PromoItem[] = [
  {
    src: "/images/ivymoda/brand/59eeeabf630f72988274fb1a3840a980.webp",
    alt: "Hè sang - Sale rộn ràng, chỉ từ 200K",
    href: "/danh-muc/deal-gia-cuoi-mtg-0526",
  },
  {
    src: "/images/ivymoda/brand/3a41dbc144753c0b810e8eecb1104835.webp",
    alt: "Mua 2 tặng 1 - Chin chu từ sự quan tâm",
    href: "/danh-muc/mua-2-tang-1-mtg-0526",
  },
];

export function BrandPromoCarousel() {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scrollByAmount = (direction: 1 | -1) => {
    const el = scrollRef.current;
    if (!el) return;
    const amount = el.clientWidth * 0.9 * direction;
    el.scrollBy({ left: amount, behavior: "smooth" });
  };

  return (
    <section className="mb-[107px]">
      <h2 className="mb-5 text-center font-[Montserrat,sans-serif] text-[30px] font-semibold uppercase leading-[32px] tracking-[2px] text-[#221F20]">
        Hè sang rộn ràng - Tặng ưu đãi đặc biệt
      </h2>

      <div className="relative mx-auto max-w-[1380px] px-4">
        <div
          ref={scrollRef}
          className="flex snap-x snap-mandatory gap-[30px] overflow-x-auto scroll-smooth [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        >
          {PROMO_ITEMS.map((item) => (
            <a
              key={item.src}
              href={item.href}
              className="relative aspect-[660/380] w-[85%] shrink-0 snap-center overflow-hidden md:w-[calc(50%-15px)]"
            >
              <Image
                src={item.src}
                alt={item.alt}
                fill
                sizes="(max-width: 768px) 85vw, 660px"
                className="object-cover"
              />
            </a>
          ))}
        </div>

        <button
          type="button"
          aria-label="Xem ưu đãi trước"
          onClick={() => scrollByAmount(-1)}
          className="absolute left-0 top-1/2 hidden -translate-y-1/2 items-center justify-center rounded-full bg-white/80 p-2 shadow-md transition hover:bg-white md:flex"
        >
          <LeftArrowIcon className="h-6 w-6 text-[#221F20]" />
        </button>
        <button
          type="button"
          aria-label="Xem ưu đãi tiếp theo"
          onClick={() => scrollByAmount(1)}
          className="absolute right-0 top-1/2 hidden -translate-y-1/2 items-center justify-center rounded-full bg-white/80 p-2 shadow-md transition hover:bg-white md:flex"
        >
          <RightArrowIcon className="h-6 w-6 text-[#221F20]" />
        </button>
      </div>
    </section>
  );
}
