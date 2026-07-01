# HomeBanner Specification

## Overview
- **Target file:** `src/components/HomeBanner.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/section-banner.png`
- **Interaction model:** time-driven autoplay (rotate every ~4-5s, standard carousel default) + click-driven prev/next arrows and dot indicators + drag/swipe enabled.

## DOM Structure
```
section.home-banner (margin-bottom:40px)
  div.slider-banner (carousel, rounded corners, overflow hidden)
    [slide 1] a > img (full width)
    [slide 2] a > img (full width)
    div.owl-nav: div.owl-prev (LeftArrowIcon), div.owl-next (RightArrowIcon)
    div.owl-dots: div.owl-dot (× 2, one `.active`)
```

## Computed Styles
```css
.home-banner { margin-bottom: 40px; border-top-left-radius: 80px; border-bottom-right-radius: 80px; background: #FFF; }
.slider-banner { border-top-left-radius: 80px; border-bottom-right-radius: 80px; overflow: hidden; }
.slider-banner .owl-nav > div { color:#BCBDC0; background:transparent; border:0; font-size:40px; position:absolute; top:50%; left:24px; transform:translateY(-50%); }
.slider-banner .owl-nav > div.owl-next { right:24px; left:auto; }
.slider-banner .owl-dots > div { width:12px; height:12px; border:1px solid #D1D2D4; border-radius:50%; margin-right:16px; }
.slider-banner .owl-dots > div:last-child { margin-right:0; }
.slider-banner .owl-dots > div.active { background:#FFF; } /* filled state — verify visually against screenshot, may need a darker active fill since dots sit on top of a light image */
```
Note the distinctive asymmetric rounded corners (top-left 80px, bottom-right 80px, other two corners square) — this is a recurring IVY moda brand motif, replicate exactly.

## Content (verbatim, real assets)
| Slide | Image | Link |
|---|---|---|
| 1 | `/images/ivymoda/banner/6a051c7c1a148911a0f04bb13704e9e4.webp` | `https://ivymoda.com/lookbook/daily-mood-226` |
| 2 | `/images/ivymoda/banner/da4faa3fe3af0cef91c4696275413c54.webp` | `https://ivymoda.com/danh-muc/sale-all-70-0626` |

Both images are already downloaded to `public/images/ivymoda/banner/`.

## Implementation approach
Build as a simple client-component carousel (no need for the Owl Carousel library — implement with local React state: `currentIndex`, `setInterval` for autoplay ~5000ms that pauses on hover, prev/next handlers, and a fade or slide transition via CSS `transition: transform 0.5s ease` or `opacity`). Use `next/image` for the two banner images (they are the LCP element, so no lazy-loading — prioritize).

## States & Behaviors
- **Autoplay:** advances to next slide every ~5s, loops back to slide 1 after slide 2. Pause autoplay on mouse hover over the carousel (common UX default, matches Owl Carousel behavior with `autoplayHoverPause:true`).
- **Arrow click:** immediately advances/retreats one slide, resets the autoplay timer.
- **Dot click:** jumps directly to that slide.
- **Drag/swipe:** optional nice-to-have if using a touch-swipe library already in the project; otherwise arrows+dots are sufficient for desktop-first pass.

## Responsive Behavior
- **Desktop (1440px):** full-width banner within the `1380px` container, ~549px tall (aspect ratio determined by the source image).
- **Mobile (390px):** same structure, banner scales to full container width, arrows may hide in favor of swipe-only (common mobile pattern) — keep dots visible.
- **Breakpoint:** 768px.
