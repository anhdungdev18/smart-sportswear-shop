# GallerySection Specification

## Overview
- **Target file:** `src/components/GallerySection.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/section-gallery.png`
- **Interaction model:** click-driven arrows + drag carousel, no autoplay.

## DOM Structure
```
section.home-gallery (margin-bottom:40px)
  h3.title-gallery
  div.list-gallery (carousel track)
    a > img (× 7 items, 246px wide, 30px gap, square lookbook photos)
```

## Computed Styles
```css
.home-gallery { margin-bottom: 40px; }
.home-gallery .title-gallery { font-weight:600; font-size:38px; line-height:46px; color:#221F20; text-align:center; letter-spacing:2px; margin-bottom: 20px; /* verify exact margin against screenshot, use 20-28px range consistent with other section titles */ }
.item-gallery img { display:block; cursor:pointer; width:100%; }
```
Items are `246px` wide with `30px` right margin (same sizing as product cards, square aspect ratio ~246×246 based on visual reference — verify against screenshot).

## Content (verbatim, real assets — already downloaded to `public/images/ivymoda/gallery/`)
- Title: "GALLERY"
- Item 1: `7b06c32a834e8032b0139df98ff1e2ce.webp` → `/sanpham/chan-vay-but-chi-xanh-coban-ms-31b0608-44353`
- Item 2: `719650f8a4399ebad50b32b42f4e2098.webp` → `/sanpham/ao-canh-polo-phoi-ren-ms-16m9204-43337`
- Item 3: `892245aeb1635dc06c48acb0dfb130f6.webp` → `/sanpham/ao-kieu-petal-light-ms-16b0646-44338`
- Item 4: `6351c0d504bed1fc5ecb737e700d81cd.webp` → `/sanpham/ao-kieu-blooming-touch-ms-16m9353-44349`
- Item 5: `114db62022947c3cf9997a9f4dca5095.webp` → `/sanpham/ao-gile-elegant-layer-ms-76b0551-44337`
- Item 6: `52b32974abb653aa0b54ee95d8d77cc8.webp` → `/sanpham/ao-blazer-dahlia-set-ms-67m8817-40193`
- Item 7: `7f4c9433ea83a0ea92619d1ac9469aad.webp` → `/sanpham/ao-khoac-tweed-ruby-classic-ms-67h9916-43703`

All image paths above are relative to `public/images/ivymoda/gallery/`.

## Implementation approach
Same horizontally-scrollable pattern as `BrandPromoCarousel`/`ProductCarouselSection` (`overflow-x-auto scroll-smooth` + arrow buttons) for consistency across the codebase.

## Responsive Behavior
- **Desktop (1440px):** ~5-6 square images visible per row.
- **Mobile (390px):** ~1.5-2 images visible, horizontal swipe.
- **Breakpoint:** 768px.
