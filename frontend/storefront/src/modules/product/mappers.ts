import type { Product, ProductDetail, ProductListItem } from "@/modules/product/types";
import { NO_IMAGE } from "@/modules/ui/placeholder";

export function mapProductListItem(p: ProductListItem): Product {
  return {
    id: p.id,
    name: p.name,
    href: `/sanpham/${p.slug}`,
    image: p.thumbnail ?? NO_IMAGE,
    hoverImage: p.thumbnail ?? NO_IMAGE,
    price: p.minPrice,
    oldPrice: p.maxPrice > p.minPrice ? p.maxPrice : undefined,
    colors: [],
    sizes: [],
  };
}

export function mapProductDetail(p: ProductDetail): {
  id: string;
  name: string;
  slug: string;
  href: string;
  images: string[];
  price: number;
  compareAtPrice?: number;
  sku?: string;
  description?: string;
  colors: Array<{ id: string; image: string; label: string; active: boolean }>;
  sizes: Array<{ id: string; label: string; quantity: number; disabled?: boolean }>;
  reviewSummary?: ProductDetail["reviewSummary"];
  relatedProducts: ReturnType<typeof mapProductListItem>[];
} {
  const sortedImages = [...p.images].sort((a, b) => a.sortOrder - b.sortOrder);

  const uniqueColors = Array.from(
    new Map(
      p.variants
        .filter((v) => v.color)
        .map((v) => [v.color, v]),
    ).values(),
  ).map((v, idx) => ({
    id: v.id,
    image: sortedImages[idx]?.imageUrl ?? NO_IMAGE,
    label: v.color ?? "",
    active: idx === 0,
  }));

  const uniqueSizes = Array.from(
    new Map(
      p.variants
        .filter((v) => v.size)
        .map((v) => [v.size, v]),
    ).values(),
  ).map((v) => ({
    id: v.id,
    label: v.size ?? "",
    quantity: v.availableQuantity,
    disabled: v.availableQuantity === 0,
  }));

  return {
    id: p.id,
    name: p.name,
    slug: p.slug,
    href: `/sanpham/${p.slug}`,
    images: sortedImages.map((img) => img.imageUrl),
    price: p.variants[0]?.price ?? p.variants.reduce((min, v) => Math.min(min, v.price), Infinity),
    compareAtPrice: p.variants[0]?.compareAtPrice,
    sku: p.variants[0]?.sku?.split("-")[0],
    description: p.description,
    colors: uniqueColors,
    sizes: uniqueSizes,
    reviewSummary: p.reviewSummary,
    relatedProducts: p.relatedProducts.map(mapProductListItem),
  };
}
