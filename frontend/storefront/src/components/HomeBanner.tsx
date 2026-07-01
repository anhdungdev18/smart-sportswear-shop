"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Image from "next/image";
import { cn } from "@/lib/utils";
import { LeftArrowIcon, RightArrowIcon } from "@/components/icons";

interface BannerSlide {
  image: string;
  href: string;
  alt: string;
}

const SLIDES: BannerSlide[] = [
  {
    image: "/images/ivymoda/banner/6a051c7c1a148911a0f04bb13704e9e4.webp",
    href: "/lookbook/daily-mood-226",
    alt: "Daily Mood lookbook",
  },
  {
    image: "/images/ivymoda/banner/da4faa3fe3af0cef91c4696275413c54.webp",
    href: "/danh-muc/sale-all-70-0626",
    alt: "Sale all 70% off",
  },
];

const AUTOPLAY_INTERVAL_MS = 5000;

export function HomeBanner() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const goToNext = useCallback(() => {
    setCurrentIndex((prev) => (prev + 1) % SLIDES.length);
  }, []);

  const goToPrev = useCallback(() => {
    setCurrentIndex((prev) => (prev - 1 + SLIDES.length) % SLIDES.length);
  }, []);

  useEffect(() => {
    if (isPaused) return;

    intervalRef.current = setInterval(goToNext, AUTOPLAY_INTERVAL_MS);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isPaused, goToNext]);

  return (
    <section className="mx-auto mb-10 max-w-[1380px] rounded-tl-[80px] rounded-br-[80px] bg-white px-4 md:px-0">
      <div
        className="relative aspect-[1380/549] overflow-hidden rounded-tl-[80px] rounded-br-[80px]"
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
      >
        {SLIDES.map((slide, index) => (
          <a
            key={slide.image}
            href={slide.href}
            className={cn(
              "absolute inset-0 transition-opacity duration-500 ease-in-out",
              index === currentIndex ? "z-10 opacity-100" : "z-0 opacity-0"
            )}
            aria-hidden={index !== currentIndex}
            tabIndex={index === currentIndex ? 0 : -1}
          >
            <Image
              src={slide.image}
              alt={slide.alt}
              fill
              priority={index === 0}
              sizes="(max-width: 768px) 100vw, 1380px"
              className="object-cover"
            />
          </a>
        ))}

        {/* Prev/Next arrows */}
        <button
          type="button"
          aria-label="Previous slide"
          onClick={goToPrev}
          className="absolute left-6 top-1/2 z-20 hidden -translate-y-1/2 cursor-pointer text-[#BCBDC0] transition-colors hover:text-[#221F20] md:block"
        >
          <LeftArrowIcon className="size-10" strokeWidth={1.5} />
        </button>
        <button
          type="button"
          aria-label="Next slide"
          onClick={goToNext}
          className="absolute right-6 top-1/2 z-20 hidden -translate-y-1/2 cursor-pointer text-[#BCBDC0] transition-colors hover:text-[#221F20] md:block"
        >
          <RightArrowIcon className="size-10" strokeWidth={1.5} />
        </button>

        {/* Dot indicators */}
        <div className="absolute bottom-2.5 left-1/2 z-20 flex -translate-x-1/2">
          {SLIDES.map((slide, index) => (
            <button
              key={slide.image}
              type="button"
              aria-label={`Go to slide ${index + 1}`}
              aria-current={index === currentIndex}
              onClick={() => setCurrentIndex(index)}
              className={cn(
                "mr-4 size-3 cursor-pointer rounded-full border border-[#D1D2D4] bg-transparent last:mr-0 transition-colors",
                index === currentIndex && "border-[#221F20] bg-[#221F20]"
              )}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
