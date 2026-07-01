"use client";

import { useRef } from "react";
import Image from "next/image";
import { LeftArrowIcon, RightArrowIcon } from "@/components/icons";
import type { GalleryItem } from "@/types/ivy";

const galleryItems: GalleryItem[] = [
  {
    image: "/images/ivymoda/gallery/7b06c32a834e8032b0139df98ff1e2ce.webp",
    href: "/sanpham/chan-vay-but-chi-xanh-coban-ms-31b0608-44353",
  },
  {
    image: "/images/ivymoda/gallery/719650f8a4399ebad50b32b42f4e2098.webp",
    href: "/sanpham/ao-canh-polo-phoi-ren-ms-16m9204-43337",
  },
  {
    image: "/images/ivymoda/gallery/892245aeb1635dc06c48acb0dfb130f6.webp",
    href: "/sanpham/ao-kieu-petal-light-ms-16b0646-44338",
  },
  {
    image: "/images/ivymoda/gallery/6351c0d504bed1fc5ecb737e700d81cd.webp",
    href: "/sanpham/ao-kieu-blooming-touch-ms-16m9353-44349",
  },
  {
    image: "/images/ivymoda/gallery/114db62022947c3cf9997a9f4dca5095.webp",
    href: "/sanpham/ao-gile-elegant-layer-ms-76b0551-44337",
  },
  {
    image: "/images/ivymoda/gallery/52b32974abb653aa0b54ee95d8d77cc8.webp",
    href: "/sanpham/ao-blazer-dahlia-set-ms-67m8817-40193",
  },
  {
    image: "/images/ivymoda/gallery/7f4c9433ea83a0ea92619d1ac9469aad.webp",
    href: "/sanpham/ao-khoac-tweed-ruby-classic-ms-67h9916-43703",
  },
];

export function GallerySection() {
  const trackRef = useRef<HTMLDivElement>(null);

  const scrollByAmount = (direction: "prev" | "next") => {
    const track = trackRef.current;
    if (!track) return;
    const itemWidth = track.firstElementChild?.clientWidth ?? 246;
    const gap = 30;
    const amount = (itemWidth + gap) * (direction === "next" ? 1 : -1);
    track.scrollBy({ left: amount, behavior: "smooth" });
  };

  return (
    <section className="mb-10">
      <h3 className="mb-6 text-center text-2xl font-semibold leading-[46px] tracking-[2px] text-[#221F20] md:text-[38px]">
        GALLERY
      </h3>

      <div className="relative">
        <button
          type="button"
          aria-label="Previous gallery items"
          onClick={() => scrollByAmount("prev")}
          className="absolute left-0 top-1/2 z-10 hidden -translate-y-1/2 items-center justify-center rounded-full bg-white/80 p-2 shadow-md transition hover:bg-white md:flex"
        >
          <LeftArrowIcon className="size-6 text-[#221F20]" />
        </button>

        <div
          ref={trackRef}
          className="flex snap-x snap-mandatory scroll-smooth gap-[30px] overflow-x-auto scroll-px-4 px-4 [&::-webkit-scrollbar]:hidden"
          style={{ scrollbarWidth: "none" }}
        >
          {galleryItems.map((item) => (
            <a
              key={item.image}
              href={item.href}
              target="_blank"
              rel="noopener noreferrer"
              className="relative aspect-square w-[160px] shrink-0 snap-start sm:w-[200px] md:w-[246px]"
            >
              <Image
                src={item.image}
                alt="IVY moda gallery look"
                fill
                sizes="(max-width: 768px) 200px, 246px"
                className="block cursor-pointer object-cover"
              />
            </a>
          ))}
        </div>

        <button
          type="button"
          aria-label="Next gallery items"
          onClick={() => scrollByAmount("next")}
          className="absolute right-0 top-1/2 z-10 hidden -translate-y-1/2 items-center justify-center rounded-full bg-white/80 p-2 shadow-md transition hover:bg-white md:flex"
        >
          <RightArrowIcon className="size-6 text-[#221F20]" />
        </button>
      </div>
    </section>
  );
}
