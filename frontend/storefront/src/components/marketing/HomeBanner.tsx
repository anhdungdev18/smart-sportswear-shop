"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Image from "next/image";
import { cn } from "@/lib/utils";
import { LeftArrowIcon, RightArrowIcon } from "@/components/shared/icons";
import type { BannerSlide } from "@/modules/content/types";
import { FALLBACK_BANNER_SLIDES } from "@/modules/content/data/marketing";

const AUTOPLAY_INTERVAL_MS = 5000;

interface HomeBannerProps {
  slides?: BannerSlide[];
}

export function HomeBanner({ slides }: HomeBannerProps) {
  const activeSlides = slides && slides.length > 0 ? slides : FALLBACK_BANNER_SLIDES;
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const goToNext = useCallback(() => {
    setCurrentIndex((prev) => (prev + 1) % activeSlides.length);
  }, [activeSlides.length]);

  const goToPrev = useCallback(() => {
    setCurrentIndex((prev) => (prev - 1 + activeSlides.length) % activeSlides.length);
  }, [activeSlides.length]);

  useEffect(() => {
    if (isPaused) return;
    intervalRef.current = setInterval(goToNext, AUTOPLAY_INTERVAL_MS);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isPaused, goToNext]);

  return (
    <section className="mb-12 w-full px-8">
      <div
        className="relative aspect-1440/640 overflow-hidden rounded-xl"
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
      >
        {activeSlides.map((slide, index) => (
          <a
            key={slide.image}
            href={slide.href}
            className={cn(
              "absolute inset-0 transition-opacity duration-500 ease-in-out",
              index === currentIndex ? "z-10 opacity-100" : "z-0 opacity-0",
            )}
            aria-hidden={index !== currentIndex}
            tabIndex={index === currentIndex ? 0 : -1}
          >
            <Image
              src={slide.image}
              alt={slide.alt}
              fill
              priority={index === 0}
              sizes="(max-width: 768px) 100vw, 1368px"
              className="object-cover object-top"
            />
          </a>
        ))}

        <button
          type="button"
          aria-label="Slide trước"
          onClick={goToPrev}
          className="absolute left-8 top-1/2 z-20 hidden -translate-y-1/2 cursor-pointer text-[#d5d6d8] transition-colors hover:text-[#221F20] md:block"
        >
          <LeftArrowIcon className="size-9" strokeWidth={1.4} />
        </button>
        <button
          type="button"
          aria-label="Slide tiếp"
          onClick={goToNext}
          className="absolute right-8 top-1/2 z-20 hidden -translate-y-1/2 cursor-pointer text-[#d5d6d8] transition-colors hover:text-[#221F20] md:block"
        >
          <RightArrowIcon className="size-9" strokeWidth={1.4} />
        </button>

        <div className="absolute bottom-3 left-1/2 z-20 flex -translate-x-1/2">
          {activeSlides.map((slide, index) => (
            <button
              key={slide.image}
              type="button"
              aria-label={`Chuyển đến slide ${index + 1}`}
              aria-current={index === currentIndex}
              onClick={() => setCurrentIndex(index)}
              className={cn(
                "mr-3.5 size-2.5 cursor-pointer rounded-full border border-[#D1D2D4] bg-transparent last:mr-0 transition-colors",
                index === currentIndex && "border-[#221F20] bg-[#221F20]",
              )}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
