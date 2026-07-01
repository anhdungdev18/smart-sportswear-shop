"use client"

import { useState } from "react"
import Image from "next/image"
import { ChevronLeft, ChevronRight } from "lucide-react"

import { cn } from "@/lib/utils"

interface ProductGalleryProps {
  images: string[]
  alt: string
}

export function ProductGallery({ images, alt }: ProductGalleryProps) {
  const [activeIndex, setActiveIndex] = useState(0)

  const goToPrev = () => {
    setActiveIndex((prev) => (prev - 1 + images.length) % images.length)
  }

  const goToNext = () => {
    setActiveIndex((prev) => (prev + 1) % images.length)
  }

  if (images.length === 0) {
    return null
  }

  return (
    <div className="flex flex-col md:flex-row gap-2">
      {/* Thumbnail rail */}
      <div className="hidden md:flex w-20 shrink-0 flex-col gap-2 overflow-y-auto">
        {images.map((image, index) => (
          <button
            key={image + index}
            type="button"
            onClick={() => setActiveIndex(index)}
            aria-label={`${alt} thumbnail ${index + 1}`}
            aria-current={index === activeIndex}
            className={cn(
              "relative aspect-square w-full shrink-0 overflow-hidden rounded-sm",
              index === activeIndex && "ring-2 ring-ivy-dark"
            )}
          >
            <Image
              src={image}
              alt={`${alt} thumbnail ${index + 1}`}
              fill
              sizes="80px"
              className="object-cover"
            />
          </button>
        ))}
      </div>

      {/* Main image */}
      <div className="relative flex-1 aspect-[4/5] overflow-hidden">
        {images.length > 1 && (
          <button
            type="button"
            onClick={goToPrev}
            aria-label="Previous image"
            className="absolute left-2 top-1/2 z-10 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-white/80 text-ivy-dark shadow-sm hover:bg-white"
          >
            <ChevronLeft className="size-5" />
          </button>
        )}

        <Image
          key={activeIndex}
          src={images[activeIndex]}
          alt={alt}
          fill
          sizes="(min-width: 768px) 50vw, 100vw"
          className="object-cover transition-opacity duration-200"
          priority
        />

        {images.length > 1 && (
          <button
            type="button"
            onClick={goToNext}
            aria-label="Next image"
            className="absolute right-2 top-1/2 z-10 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-white/80 text-ivy-dark shadow-sm hover:bg-white"
          >
            <ChevronRight className="size-5" />
          </button>
        )}

        {images.length > 1 && (
          <div className="absolute bottom-2 left-1/2 -translate-x-1/2 rounded-full bg-white/80 px-2 py-0.5 text-xs text-ivy-text-muted md:hidden">
            {activeIndex + 1} / {images.length}
          </div>
        )}
      </div>
    </div>
  )
}
