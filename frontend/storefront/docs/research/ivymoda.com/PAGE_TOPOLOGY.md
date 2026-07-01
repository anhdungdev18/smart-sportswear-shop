# IVY moda (ivymoda.com) — Homepage Page Topology

Target URL: https://ivymoda.com/
Tech stack observed: legacy jQuery + Bootstrap 4 + Owl Carousel + custom CSS (NOT React/Next). We clone visuals/behavior only, rebuilt in Next.js/React/Tailwind.

## Global Layout
- `<body>` → `<header id="header" class="site-header">` (fixed, always visible) + `<main id="main" class="site-main">` (div.container wraps all sections) + `<div class="site-bottom">` (footer content) + `<footer id="footer">` (copyright bar)
- No smooth-scroll library (no Lenis/Locomotive). No scroll-snap. No AOS/scroll-reveal animations detected (`[data-aos]` count = 0).
- Header is `position: fixed; top:0; z-index:20` permanently — it does NOT change appearance on scroll (verified: boxShadow/background/height identical at scrollY=0 and scrollY=600).
- z-index layers: header nav dropdown z-index:10, header itself z-index:20.

## Section Order (top → bottom)

1. **Header** (`#header.site-header`) — fixed overlay, height 80px
   - Logo (centered, absolute positioned)
   - Main nav (`.main-menu`) — 6 top items, hover-driven mega menus for 4 of them
   - Search form + quick-search dropdown
   - Right icons: Outlet link, CS/headphones dropdown (hotline/live chat/messenger/email/order lookup), account, cart

2. **Home Banner** (`section.home-banner`) — Owl Carousel, 2 real slides, autoplay, prev/next arrows + dots. Click-driven navigation (arrows/dots), auto-rotate (time-driven).

3. **New Arrival Section** (`section.home-new-prod` #1) — title "NEW ARRIVAL", tabbed (click-driven) product carousel with 3 tabs: "IVY moda", "Metagent", "IVY kids" (kids tab hidden via `d-none` currently). Each tab shows its own Owl Carousel of product cards.

4. **Featured Collection Section** (`section.home-new-prod` #2) — title "THE CLASSY | BST ĐỘC QUYỀN ONLINE x HUYỀN LIZZIE", NO tabs (single product carousel, same card component as #3).

5. **Sale Section** (`section.home-new-prod` #3) — title "GIÁ MỚI - CHẠM ĐỈNH | SALE ALL 70% CHỈ CÓ TẠI ONLINE", tabbed like #3 (IVY moda / Metagent / IVY kids).

   → Sections 3, 4, 5 all share ONE reusable component: `ProductCarouselSection` (title + optional tabs + Owl-style carousel of `ProductCard`).

6. **Brand/Promo Banner Carousel** (`section.list-ads-brand`) — title "HÈ SANG RỘN RÀNG - TẶNG ƯU ĐÃI ĐẶC BIỆT", Owl Carousel of promo banner images (2-up visible, autoplay/drag).

7. **Gallery Section** (`section.home-gallery`) — title "GALLERY", Owl Carousel of square lookbook/gallery images linking to products.

8. **Footer content** (`.site-bottom`) — 3 columns: left (logo, DMCA badge, Bộ Công Thương badge, social icons), center (3 link lists: Giới thiệu / Dịch vụ khách hàng / Liên hệ), right (email subscribe form + app store/play store download badges).

9. **Copyright bar** (`footer#footer.site-footer`) — single line "©IVYmoda All rights reserved".

## Non-content overlays present in DOM (not part of visual homepage, skip building)
- `#fancybox-add-to-cart` (size/add-to-cart modal, empty until triggered)
- `#overlay`, `.modal_loading`, `#modal_phone` (hidden modals)
- `.nav-bottom` (mobile-only bottom nav, height 0 on desktop)
- `.box-support` (floating support widget, height 0 collapsed by default)

These exist for later interaction (PDP/cart flows) but are out of scope for the homepage pixel-perfect clone per project defaults (mock data, no real backend).

## Interaction Model Summary
| Section | Model |
|---|---|
| Header nav dropdowns | Hover-driven (CSS `li:hover > .sub-menu`, `display:none → flex` + `animation: fade_in_show 0.5s`) |
| Header search quick-search | Focus/type-driven (dropdown shows results) — out of scope (needs backend) |
| Home banner | Time-driven autoplay + click-driven arrows/dots (Owl Carousel) |
| Product carousel tabs (sections 3 & 5) | Click-driven tab switch (`.exclusive-tab` → `.exclusive-inner.active`) |
| Product carousels (all) | Click-driven arrows + drag (Owl Carousel), no autoplay |
| Product card hover | Hover-driven: thumbnail image swap (opacity 0→1, 0.3s ease-in-out), size-selector popup slides up (`.list-size.open`, bottom 0→35px + opacity 0→1, 0.3s ease-in-out) |
| Brand promo carousel | Click-driven arrows/drag, likely autoplay (Owl default) |
| Gallery carousel | Click-driven arrows/drag |

## Responsive Breakpoints (from CSS media queries observed)
- Desktop: ≥1200px (container width per Bootstrap 4 grid)
- Tablet: ~768–1199px — mega menus / multi-column layouts collapse
- Mobile: <768px — hamburger menu (`.mobile-menu`), stacked sections, bottom nav bar (`.nav-bottom`) appears

See `docs/research/ivymoda.com/BEHAVIORS.md` for exact trigger/before/after CSS values.
