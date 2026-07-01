# LifestyleHeader Specification

## Overview
- **Target file:** `src/components/LifestyleHeader.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/news-desktop-full.png` (top section)
- **Interaction model:** static page title + a row of link-style sub-nav tabs (each is a real navigational link to a different news category page — not a JS tab switcher). Only one is ever "active" per page (styled with underline), matching whichever category page is currently being viewed.
- **Props:** `{ activeSlug: string }` — one of `"tin-chinh" | "kien-thuc" | "xu-huong" | "phong-cach" | "blog"`.

## DOM Structure
```
div (root, text-center, py-10)
  h1 "LIFESTYLE" (huge bold uppercase)
  nav (row of 5 links, centered, gap)
    a × 5: "TIN TỨC" (→ /tin-tuc/tin-chinh), "KIẾN THỨC" (→ /tin-tuc/kien-thuc), "XU HƯỚNG" (→ /tin-tuc/xu-huong), "PHONG CÁCH" (→ /tin-tuc/phong-cach), "BLOG CHIA SẺ" (→ /tin-tuc/blog)
```

## Computed Styles (faithful approximation matching screenshot)
```css
h1 { font-size: 56px; font-weight: 800; letter-spacing: 2px; color: #221F20; text-transform: uppercase; margin-bottom: 32px; }
nav a { font-size: 14px; font-weight: 600; letter-spacing: 1px; color: #6C6D70; padding-bottom: 8px; }
nav a.active { color: #221F20; border-bottom: 2px solid #221F20; }
nav { display: flex; justify-content: center; gap: 32px; border-bottom: 1px solid #E7E8E9; padding-bottom: 0; margin-bottom: 40px; }
```
Since these are real external links to sibling category pages that aren't part of this clone, prefix hrefs with `https://ivymoda.com` (they're reference links, not app routes).

## Text Content (verbatim)
"LIFESTYLE"; nav items: "TIN TỨC", "KIẾN THỨC", "XU HƯỚNG", "PHONG CÁCH", "BLOG CHIA SẺ"

## Responsive Behavior
- **Desktop (1440px):** as described.
- **Mobile (390px):** title font-size drops to ~32px, nav row becomes horizontally scrollable (`overflow-x-auto`) if it doesn't fit, rather than wrapping awkwardly.
- **Breakpoint:** `md:` (768px).
