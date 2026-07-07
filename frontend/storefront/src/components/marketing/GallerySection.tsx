"use client";

import { useRef } from "react";
import Image from "next/image";
import { LeftArrowIcon, RightArrowIcon } from "@/components/shared/icons";
import type { GalleryItem } from "@/modules/content/types";

interface GallerySectionProps {
  items?: GalleryItem[];
}

export function GallerySection({ items }: GallerySectionProps) {
  const trackRef = useRef<HTMLDivElement>(null);

  const scrollByAmount = (direction: "prev" | "next") => {
    const track = trackRef.current;
    if (!track) return;
    const itemWidth = track.firstElementChild?.clientWidth ?? 246;
    const gap = 28;
    const amount = (itemWidth + gap) * (direction === "next" ? 1 : -1);
    track.scrollBy({ left: amount, behavior: "smooth" });
  };

  if (!items || items.length === 0) return null;

  return (
    <section className="mb-16 mt-4">
      <h3 className="mb-8 text-center text-[20px] font-semibold uppercase tracking-[3px] text-[#221F20] md:text-[46px] md:leading-[1.05]">
        GALLERY
      </h3>

      <div className="relative">
        <button
          type="button"
          aria-label="Ảnh trước"
          onClick={() => scrollByAmount("prev")}
          className="absolute left-5 top-1/2 z-10 hidden -translate-y-1/2 items-center justify-center text-[#BCBDC0] transition hover:text-[#221F20] md:flex"
        >
          <LeftArrowIcon className="size-8" />
        </button>

        <div
          ref={trackRef}
          className="flex snap-x snap-mandatory gap-[28px] overflow-x-auto scroll-smooth px-0 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        >
          {items.map((item) => (
            <a
              key={item.image}
              href={item.href}
              className="relative aspect-square w-[180px] shrink-0 snap-start sm:w-[220px] md:w-[248px]"
            >
              <Image
                src={item.image}
                alt="Gallery"
                fill
                sizes="(max-width: 768px) 200px, 248px"
                className="block object-cover"
              />
            </a>
          ))}
        </div>

        <button
          type="button"
          aria-label="Ảnh tiếp theo"
          onClick={() => scrollByAmount("next")}
          className="absolute right-5 top-1/2 z-10 hidden -translate-y-1/2 items-center justify-center text-[#BCBDC0] transition hover:text-[#221F20] md:flex"
        >
          <RightArrowIcon className="size-8" />
        </button>
      </div>
    </section>
  );
}
