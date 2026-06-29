"use client";

import { MagnifyingGlass } from "@phosphor-icons/react";
import { useEffect, useRef, useState } from "react";
import { mockShoeImage } from "@/modules/catalog/products";
import { galleryCopy, languageCookieName, type Language } from "@/modules/i18n";

type ProductGalleryProps = {
  images: string[];
  name: string;
  initialLanguage?: Language;
};

const fallbackProductImage = mockShoeImage;

function readCookieLanguage(): Language | null {
  const value = document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${languageCookieName}=`))
    ?.split("=")[1];

  return value === "en" || value === "vi" ? value : null;
}

export function ProductGallery({ images, name, initialLanguage = "vi" }: ProductGalleryProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [direction, setDirection] = useState<"next" | "prev">("next");
  const [language, setLanguage] = useState<Language>(initialLanguage);
  const stageRef = useRef<HTMLDivElement | null>(null);
  const wheelLockRef = useRef(false);
  const activeImage = images[activeIndex] ?? images[0] ?? fallbackProductImage;

  useEffect(() => {
    const stored = readCookieLanguage() ?? window.localStorage.getItem(languageCookieName);
    if (stored === "en" || stored === "vi") {
      setLanguage(stored);
    }
  }, []);

  useEffect(() => {
    const stage = stageRef.current;

    if (!stage) {
      return;
    }

    function handleNativeWheel(event: WheelEvent) {
      event.preventDefault();
      event.stopPropagation();

      if (images.length < 2 || wheelLockRef.current) {
        return;
      }

      wheelLockRef.current = true;
      setDirection(event.deltaY > 0 ? "next" : "prev");
      setActiveIndex((current) => {
        const step = event.deltaY > 0 ? 1 : -1;
        return (current + step + images.length) % images.length;
      });

      window.setTimeout(() => {
        wheelLockRef.current = false;
      }, 360);
    }

    stage.addEventListener("wheel", handleNativeWheel, { passive: false });

    return () => {
      stage.removeEventListener("wheel", handleNativeWheel);
    };
  }, [images.length]);

  const t = galleryCopy[language];

  return (
    <div className="product-gallery">
      <div className="gallery-stage" ref={stageRef}>
        <img
          className={`gallery-main-image ${direction}`}
          src={activeImage}
          alt={t.imageAlt(name, activeIndex)}
          key={activeImage}
          onError={(event) => {
            event.currentTarget.src = fallbackProductImage;
          }}
        />
        <div className="gallery-wheel-hint">{t.wheelHint}</div>
        <button className="zoom-btn" type="button" aria-label={t.zoomLabel}>
          <MagnifyingGlass size={20} weight="bold" />
        </button>
      </div>
      <div className="gallery-thumbnails" aria-label={t.thumbnailsLabel}>
        {images.map((image, index) => (
          <button
            className={index === activeIndex ? "active" : ""}
            key={`${image}-${index}`}
            type="button"
            aria-label={t.viewImage(index, name)}
            aria-pressed={index === activeIndex}
            onClick={() => {
              setDirection(index > activeIndex ? "next" : "prev");
              setActiveIndex(index);
            }}
          >
            <img
              src={image}
              alt=""
              onError={(event) => {
                event.currentTarget.src = fallbackProductImage;
              }}
            />
          </button>
        ))}
      </div>
    </div>
  );
}
