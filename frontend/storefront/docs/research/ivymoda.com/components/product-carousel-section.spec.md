# ProductCarouselSection Specification

## Overview
- **Target file:** `src/components/ProductCarouselSection.tsx`
- **Screenshots:** `docs/design-references/ivymoda.com/section-new-arrival.png`, `section-featured.png`, `section-sale.png`
- **Interaction model:** click-driven tabs (when present) + click-driven/drag carousel navigation. NOT scroll-driven.
- **Depends on:** `ProductCard` (`src/components/ProductCard.tsx`, already spec'd separately — import it, do not reimplement the card)
- **Props:** `{ title: string; tabs?: { id: string; label: string; products: Product[] }[]; products?: Product[] }` (matches `ProductCarouselSectionData` in `src/types/ivy.ts`). Exactly one of `tabs` or `products` is provided per instance.

## DOM Structure
```
section.home-new-prod (margin-bottom:40px)
  div.title-section (centered heading)
  div.exclusive-tabs
    [if tabs] div.exclusive-head > ul (tab buttons, centered, margin-bottom:28px)
      li.exclusive-tab(.active) — click to switch
    div.exclusive-content (position:relative, min-height:100px, margin-bottom:24px)
      div.exclusive-inner(.active) — one per tab, only .active is displayed; if no tabs, exactly one always-active inner
        div.list-products (carousel track) > ProductCard × N
        carousel arrows (prev/next) positioned over the row, centered vertically
```

## Computed Styles
```css
.home-new-prod { margin-bottom: 40px; }
.title-section { text-align:center; letter-spacing:2px; text-transform:uppercase; font-weight:600; font-size:30px; line-height:32px; font-family:'Montserrat'; color:#221F20; margin-bottom:20px; }
.exclusive-head ul { text-align:center; margin-bottom:28px; }
.exclusive-tab { color:#6C6D70; font-size:20px; line-height:30px; margin-right:56px; display:inline-block; cursor:pointer; }
.exclusive-tab:last-child { margin-right:0; }
.exclusive-tab.active { color:#221F20; border-bottom:2px solid #221F20; }
.exclusive-content { margin-bottom:24px; position:relative; min-height:100px; }
.exclusive-inner { display:none; }
.exclusive-inner.active { display:block; }
.new-prod-slider .owl-stage-outer { margin:0 -7px; padding:0 7px; }
.owl-carousel .owl-nav > div { width:30px; height:30px; text-align:center; position:absolute; top:50%; margin-top:-65px; left:39px; color:#BCBDC0; border-radius:2px; font-size:40px; transition:all 0.3s ease-in-out; }
.owl-carousel .owl-nav > div.owl-next { left:auto; right:39px; }
```
Carousel item sizing (from live DOM): each `ProductCard` slot is `246px` wide with `30px` right margin between cards; ~5 cards visible at 1440px viewport width within the 1380px container.

## States & Behaviors
### Tab switch (click-driven, only for New Arrival & Sale sections)
- **Trigger:** click on `.exclusive-tab`
- **Before:** clicked tab `color:#6C6D70`, its `.exclusive-inner` `display:none`
- **After:** clicked tab gets `.active` (`color:#221F20; border-bottom:2px solid #221F20`), its `.exclusive-inner` gets `display:block`; all other tabs/inners revert to inactive state
- **Implementation:** local React state `activeTabId`, no transition/animation on the switch itself (instant show/hide, matches source)

### Carousel navigation
- Click-driven prev/next arrows scroll the `.list-products` row by one "page" (a handful of cards) with a smooth `transform` transition (~0.3-0.5s ease). Implement with a horizontally-scrollable flex container (`overflow-x-auto scroll-smooth` + arrow buttons calling `scrollBy`) — this is simpler and equally faithful to a manual reimplementation of Owl Carousel, and satisfies both click/drag interaction models (native scroll gives drag/swipe for free on touch and trackpads).
- No autoplay on these product carousels (unlike the home banner).

## Content — 3 instances to render on the homepage

### Instance 1: "NEW ARRIVAL" (tabs: IVY moda / Metagent — IVY kids tab exists but is hidden `d-none`, omit it)
**Tab "IVY moda" products** (all `1.390.000đ`/`1.690.000đ`/`890.000đ` etc, no discount):
1. Áo kiểu Day Dream — `/images/ivymoda/products/2cf9b85228b3e78f22cfc3718f6e24b6.webp` (hover: `15562de9138d1c5927ee7f195c512e06.webp`) — 1.390.000đ — `/sanpham/ao-kieu-day-dream-ms-16b0570-44652`
2. Đầm xòe Soft Vibes — `84ceec2e174853ef07c4201f1c60aee3.webp` / `9b5bf42f1df7ad28f286fab5b2cd6622.webp` — 1.690.000đ — `/sanpham/dam-xoe-soft-vibes-ms-48b0651-44767`
3. Áo lụa Art Elegance — `5877f3534b232631d1cf7fdd8277859b.webp` / `7b92778cdf3ca6ecdda23623e14c73dd.webp` — 890.000đ — `/sanpham/ao-lua-art-elegance-ms-16b0618-44735`
4. Áo lụa Pure Daily — `a9f9d92197126efdabe0be59ef2006e7.webp` / `049ba3d1ae8ce2ec91c7cdf00d01e0d7.webp` — 990.000đ — `/sanpham/ao-lua-pure-daily-ms-16m9377-44764`
5. Đầm xòe Silk Touch — `ac6ca29438fd3746a3c2e84b75b2a46a.webp` / `ffef76478137d3b4273a125121c96be7.webp` — 1.790.000đ — `/sanpham/dam-xoe-silk-touch-ms-48b0727-44667`
6. Áo sơ mi Deep Muse — `1c576e917832620b2d4b0f3cb78642a5.webp` / `b8b5eaf1a0640756809e0bdfeb2039ad.webp` — 850.000đ — `/sanpham/ao-so-mi-deep-muse-ms-16b0620-44762`

**Tab "Metagent" products** (all in `public/images/ivymoda/products/`):
1. Áo Polo Cotton Magic — `03a90dba7165fd770b5ee22d5a4460a0.jpg` / `f2bb04611bf0f1d67b35d57b7a34e402.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-magic-ms-57e4507-44963`
2. Áo Polo Cotton Magic (colorway) — `c6ca0a664adff97ed17bcefbe0c3edae.jpg` / `23b930813fdb472c25dd09fe6f6fdf66.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-magic-ms-57e4507-44964`
3. Áo Polo Cotton Magic (colorway) — `8f4eb4a124198497b85bf2efdaa811a5.jpg` / `e13c4d413613d49fe2aa50729f0496d1.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-magic-ms-57e4507-44965`
4. Áo Polo Cotton Daily — `aba820e5aca51542c7fea0a4da78854e.jpg` / `daec5eab3ff306ec23fa0909f7c50984.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-daily-ms-57e4499-44960`
5. Áo Polo Cotton Daily (colorway) — `3207e9df533a53bf889a9f97aa7b72ab.jpg` / `d99c5eb38fc914895b16c4dd3edf780a.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-daily-ms-57e4499-44961`
6. Áo Polo Frezing Regular — `608ff62c918cfef47d372e1e4eaddcd7.jpg` / `e6aa6d8ad9a789589acb4ab9b7f345f7.jpg` — 650.000đ — `/sanpham/ao-polo-frezing-regular-ms-57e4524-44978`

All products in this instance: no `oldPrice`/`discountPercent` (no badge shown). Default active tab: "IVY moda". Each product gets one placeholder color swatch (reuse `public/images/ivymoda/colors/001.png`) and standard sizes `["s","m","l","xl","xxl"]` unless noted otherwise — exact per-product swatches are not critical for the homepage pass since the PDP is out of scope.

### Instance 2: "THE CLASSY | BST ĐỘC QUYỀN ONLINE x HUYỀN LIZZIE" (no tabs — pass `products` directly, all discounted -45%/-40%)
1. Áo thun Polo Classe Zip — `c93add532002f65e80d83860a13e560c.webp` / `ae04423ceee236859f755a7e2f066700.webp` — price 434.500đ, oldPrice 790.000đ, discount -45% — `/sanpham/ao-thun-polo-classe-zip-ms-57t0295-44544`
2. Áo sơ mi Aurelia IVYmoda — `f3527d374c3e038d40454bd667b0ed23.webp` / `c8f2a665529316ae727ddae0484831ea.webp` — 544.500đ / 990.000đ / -45% — `/sanpham/ao-so-mi-aurelia-ivymoda-ms-16t0296-44724`
3. Áo ren Pearl Lace — `d1bbd95c2c658214dcc569873c556c6d.webp` / `636e80d7ccdd48a9e423ab1f92681f35.webp` — 434.500đ / 790.000đ / -45% — `/sanpham/ao-ren-pearl-lace-ms-57t0310-44752`
4. Áo kiểu Sculpt Moda — `58b0bbc0b98b112e263a1d7efa9dc209.webp` / `e02a2d4560504385fde2c344ddf4578f.webp` — 534.000đ / 890.000đ / -40% — `/sanpham/ao-kieu-sculpt-moda-ms-16t0251-43162`
5. Áo sơ mi Aurelia IVYmoda (colorway) — `3cea5000260703bb54bdd346b6365704.webp` / `636d0ca04b95d3d87c2e2f0d49400063.webp` — 544.500đ / 990.000đ / -45% — `/sanpham/ao-so-mi-aurelia-ivymoda-ms-16t0296-44730`
6. Chân váy Elara Lace Pencil — `a9ce6b5f673854c421ceeedbf6b53b34.webp` / `97e8ac59e39e7049f36aa04a17770521.webp` — 599.500đ / 1.090.000đ / -45% — `/sanpham/chan-vay-elara-lace-pencil-ms-31t0304-44736`

### Instance 3: "GIÁ MỚI - CHẠM ĐỈNH | SALE ALL 70% CHỈ CÓ TẠI ONLINE" (tabs: IVY moda / Metagent)
**Tab "IVY moda" products** (all -70% off):
1. Đầm voan hoa dáng xòe — `ee32ed354accc0fde3eec39f7e4e9ad6.webp` / `0bdfa4229827fcdf3d0da8b2b6c8ed02.webp` — 447.000đ / 1.490.000đ / -70% — `/sanpham/dam-voan-hoa-dang-xoe-ms-48m8652-40094`
2. Chân váy Tapta 2 lớp — `e5d337aa2ad7f994a507778c310bfa48.webp` / `1b87780e1a7cb8bfed979da5cfa0ce59.webp` — 237.000đ / 790.000đ / -70% — `/sanpham/chan-vay-tapta-2-lop-ms-31m8293-37520`
3. Đầm lụa xòe cổ V — `fe189f3f06ea21d1e71b9e78f49a7f6f.webp` / `82a734bb9824fceebe0bccb1052e2221.webp` — 537.000đ / 1.790.000đ / -70% — `/sanpham/dam-lua-xoe-co-v-ms-48m6598-37977`
4. Zuýp đuôi cá 2 lớp — `58963bb1a8709d6fb2fbc0df4c95cac3.jpg` / `03b041ffa59ceab4ede5c065b47dde0b.jpg` — 285.000đ / 950.000đ / -70% — `/sanpham/zuyp-duoi-ca-2-lop-ms-31m8636-38863`
5. Chân váy Tuytsi xếp ly — `e16447016e1280fb66d221bed6a12e4d.webp` / `60aad941648b59197eac563b91c400ea.webp` — 477.000đ / 1.590.000đ / -70% — `/sanpham/chan-vay-tuytsi-xep-ly-ms-30m8749-39604`
6. Áo Blazer Dahlia Set — `6d99f9f974702e2ea3607b0bd6594d6c.webp` / `c7da62588610e789d949ac6875d6dd71.webp` — 567.000đ / 1.890.000đ / -70% — `/sanpham/ao-blazer-dahlia-set-ms-67m8817-40194`

**Tab "Metagent" products** (no discount shown on these specific items — real content, keep as-is):
1. Áo Polo Cotton Magic (colorway) — `c6ca0a664adff97ed17bcefbe0c3edae.jpg` / `23b930813fdb472c25dd09fe6f6fdf66.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-magic-ms-57e4507-44964`
2. Áo sơ mi Khaki — `4611f30351e0b708a3887c6a03091f0c.jpg` / `0a15a4f09c111bfb2f2cff383d897df6.jpg` — 990.000đ — `/sanpham/ao-so-mi-khaki-ms-17e4483-44966`
3. Áo Polo Cotton Daily — `aba820e5aca51542c7fea0a4da78854e.jpg` / `daec5eab3ff306ec23fa0909f7c50984.jpg` — 690.000đ — `/sanpham/ao-polo-cotton-daily-ms-57e4499-44960`
4. Quần shorts thun Simple — `8375bf5e76b75eb981645f18ac10bfa9.jpg` / `47ff9628191a3696f1f4d20ba26a0f6c.jpg` — 590.000đ — `/sanpham/quan-shorts-thun-simple-ms-20d0012-44971`
5. Áo thun Cotton Relaxed — `d557ee88abffdc4720a47df783bcc090.jpg` / `8228728a826d3a5e95b9b785550b471c.jpg` — 490.000đ — `/sanpham/ao-thun-cotton-relaxed-ms-57d0012-44932`
6. Áo sơ mi Denim Urban — `40146f76a13c6b530ecd18f05ef25684.jpg` / `31a49a220313401e83d40be5d328f558.jpg` — 790.000đ — `/sanpham/ao-so-mi-denim-urban-ms-16e4505-44951`

Default active tab for instance 3: "IVY moda". All image paths above are relative to `public/images/ivymoda/products/` (already downloaded).

## Responsive Behavior
- **Desktop (1440px):** ~5 cards visible per row within the 1380px container.
- **Tablet (768px):** ~2-3 cards visible, same horizontal-scroll interaction.
- **Mobile (390px):** ~1.5 cards visible (partial next card peeking, common carousel affordance), horizontal scroll/swipe.
- **Breakpoint:** card visible-count changes are purely a function of container width ÷ fixed 246px card width — no special breakpoint CSS needed beyond the container's own responsive width.
