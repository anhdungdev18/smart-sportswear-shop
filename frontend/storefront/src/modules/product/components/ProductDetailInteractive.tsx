"use client";

import { useMemo, useState } from "react";
import { ProductGallery } from "@/modules/product/components/ProductGallery";
import {
  ProductPurchasePanel,
  type ProductColor,
  type ProductSize,
} from "@/modules/product/components/ProductPurchasePanel";
import { NO_IMAGE } from "@/modules/ui/placeholder";
import type { ProductVariant } from "@/modules/product/types";

interface ProductDetailInteractiveProps {
  productId: string;
  name: string;
  sku: string;
  ratingPercentage: number;
  reviewCount: number;
  price: number;
  colors: ProductColor[];
  sizes: ProductSize[];
  variants: ProductVariant[];
  images: Array<{ url: string; color: string | null }>;
}

// Holds the selected color so both the gallery (which images to show) and the
// purchase panel (which sizes/price) stay in sync when the customer switches color.
export function ProductDetailInteractive({
  productId,
  name,
  sku,
  ratingPercentage,
  reviewCount,
  price,
  colors,
  sizes,
  variants,
  images,
}: ProductDetailInteractiveProps) {
  const [selectedColor, setSelectedColor] = useState(
    colors.find((color) => color.active)?.label ?? colors[0]?.label ?? "",
  );

  // Show the selected color's images first, plus any untagged (shared) images.
  // Fall back to the full set when a color has no images of its own.
  const galleryImages = useMemo(() => {
    const tagged = images.filter((img) => img.color === selectedColor).map((img) => img.url);
    const untagged = images.filter((img) => img.color == null).map((img) => img.url);
    const forColor = [...tagged, ...untagged];
    const all = images.map((img) => img.url);
    const list = forColor.length ? forColor : all;
    return list.length ? list : [NO_IMAGE];
  }, [images, selectedColor]);

  return (
    <div className="grid grid-cols-1 gap-10 lg:grid-cols-[620px_minmax(0,1fr)] lg:gap-12">
      {/* Remount on color change so the gallery resets to the first image. */}
      <ProductGallery key={selectedColor} images={galleryImages} alt={name} />
      <ProductPurchasePanel
        productId={productId}
        name={name}
        sku={sku}
        ratingPercentage={ratingPercentage}
        reviewCount={reviewCount}
        price={price}
        colors={colors}
        sizes={sizes}
        variants={variants}
        selectedColorLabel={selectedColor}
        onSelectColor={setSelectedColor}
      />
    </div>
  );
}
