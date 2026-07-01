# ProductGallery Specification

## Overview
- **Target file:** `src/components/ProductGallery.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/product-detail-desktop-full.png` (left column)
- **Interaction model:** click-driven (click a thumbnail to change the main image; main image also has prev/next arrows that cycle through the same images). No autoplay.
- **Props:** `{ images: string[]; alt: string }`

## DOM Structure
```
div (gallery root, flex row: thumbnail rail + main image)
  div (vertical thumbnail rail, ~80px wide, scrollable if many images)
    button × N (one per image, ~72px square thumbnail; active thumbnail gets a highlighted border)
  div (main image area, relative, flex-1)
    button (prev arrow, top-left area, chevron rotated to point left — the live site uses an up/down chevron rotated 90°, a left/right chevron is an equally faithful simplification)
    Image (current image, object-cover, rounded corners not required — plain rectangle)
    button (next arrow, mirrored on the right)
```

## Computed Styles (approximate, faithful to screenshot since this is a newer BEM-named template without exact computed-style extraction)
- Main image: roughly 4:5 portrait aspect ratio (~560×700 at desktop), `object-fit:cover`.
- Thumbnail rail: each thumbnail ~72-80px square, `gap-2` (8px) vertical spacing, active thumbnail `ring-2 ring-ivy-dark`.
- Arrow buttons: circular or plain icon buttons, `text-ivy-text-muted hover:text-ivy-dark`, positioned `absolute top-1/2 -translate-y-1/2` at `left-2`/`right-2` with a semi-transparent white background circle for contrast against photos.

## States & Behaviors
### Thumbnail click
- **Trigger:** click on a thumbnail button
- **Effect:** sets `activeIndex` state, main image swaps instantly (a `transition-opacity duration-200` cross-fade is a nice, faithful touch since the live site uses a fade-effect Swiper instance)

### Arrow click
- **Trigger:** click prev/next button on the main image
- **Effect:** decrements/increments `activeIndex` (wrapping at the ends), same as clicking the corresponding thumbnail

## Content
This component receives images as props — no data to hardcode. For manual sanity-checking while building, the real product ("Áo kiểu Day Dream") has 12 images, e.g. first one: `/images/ivymoda/products/2cf9b85228b3e78f22cfc3718f6e24b6.webp` (reuse the already-downloaded homepage product asset — same product). You do not need to source additional images; the page-assembly step will pass in whatever list is available (can be as few as 1-2 images reused from the existing downloaded set — treat the prop as the source of truth, don't hardcode a specific count).

## Responsive Behavior
- **Desktop (≥768px):** thumbnail rail + main image side by side (flex-row), as described.
- **Mobile (<768px):** hide the vertical thumbnail rail (`hidden md:flex`), main image becomes full-width with prev/next arrows and a simple dot-count indicator below (e.g. "1/12") since a vertical thumb rail doesn't fit well on narrow screens.
- **Breakpoint:** `md:` (768px).
