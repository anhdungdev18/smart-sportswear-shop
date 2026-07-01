# BrandPromoCarousel Specification

## Overview
- **Target file:** `src/components/BrandPromoCarousel.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/section-brand.png`
- **Interaction model:** click-driven arrows + drag (Owl Carousel `owl-drag`); no autoplay confirmed but harmless to add a slow autoplay (~6s) since it's a promo banner strip — optional, static row for a simpler build is also acceptable.

## DOM Structure
```
section.list-ads-brand (margin-bottom:107px)
  div.title-section (same styling as ProductCarouselSection title)
  div.slider-ads-brand (carousel track)
    a > img (× 2 visible items, 660px wide each, 30px gap)
```

## Computed Styles
```css
.list-ads-brand { margin-bottom: 107px; }
.title-section { text-align:center; letter-spacing:2px; text-transform:uppercase; font-weight:600; font-size:30px; line-height:32px; font-family:'Montserrat'; color:#221F20; margin-bottom:20px; }
```
Items are `660px` wide with `30px` right margin in the live DOM (2 items visible at 1440px viewport width, container 1380px).

## Content (verbatim, real assets — already downloaded to `public/images/ivymoda/brand/`)
- Title: "HÈ SANG RỘN RÀNG - TẶNG ƯU ĐÃI ĐẶC BIỆT"
- Item 1: image `59eeeabf630f72988274fb1a3840a980.webp` → `https://ivymoda.com/danh-muc/deal-gia-cuoi-mtg-0526`
- Item 2: image `3a41dbc144753c0b810e8eecb1104835.webp` → `https://ivymoda.com/danh-muc/mua-2-tang-1-mtg-0526`

## Implementation approach
Simple horizontally-scrollable row (`overflow-x-auto scroll-smooth` + prev/next arrow buttons using the same `owl-nav`-style arrow visual as other carousels, `LeftArrowIcon`/`RightArrowIcon` from `icons.tsx`) — matches the click/drag interaction model without needing the Owl Carousel library.

## Responsive Behavior
- **Desktop (1440px):** 2 full-width banners visible side by side.
- **Mobile (390px):** 1 banner visible at a time (near-full-width), horizontal swipe to see the second.
- **Breakpoint:** 768px.
