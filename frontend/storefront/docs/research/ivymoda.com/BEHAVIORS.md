# IVY moda (ivymoda.com) — Behavior Bible

Extraction method: live computed styles via Playwright MCP + ground-truth production CSS fetched directly from `https://pubcdn.ivymoda.com/ivy2/css/new_style/{style.css,style_02.css,custom.css,fix.css,fixed.css}`. All values below are exact, not estimated.

## 1. Header — static fixed, NOT scroll-reactive
- `#header.site-header { position:fixed; top:0; left:0; right:0; background:#FFF; z-index:20; }`
- Container: `height:80px; align-items:center; justify-content:space-between;` bottom hairline via `::after` pseudo-element: `width:calc(100% - 30px); height:1px; background:#E7E8E9;` centered.
- Verified: scrolling to `scrollY=600` produces IDENTICAL computed styles (boxShadow:none, background unchanged, height unchanged, no class added). **Do not build a scroll-shrink header — there isn't one.**
- Logo: `.site-brand { position:absolute; top:50%; left:50%; transform:translate(-50%,-50%); }`, `img { width:206px; height:66px; }`.
- `main { padding-top: 80px }` (to clear the fixed header) — becomes `64px` under 768px breakpoint.

## 2. Main Nav — hover-driven mega menu (desktop)
- Trigger: `:hover` on `.main-menu .menu > li` (mouse enter, no click, no delay).
- Top-level link: `color:#221F20; font-weight:600; text-transform:uppercase;` → hover: `color:#AC2F33` (transition not specified = instant/inherit default, treat as `color 0.2s ease` for a natural feel).
- Dropdown (`.sub-menu`):
  - Before: `display:none !important` (confirmed via computed style, not just opacity)
  - After (on parent `li:hover`): `display:flex !important; animation: fade_in_show 0.5s;`
  - Positioning: `position:absolute; top:55px; left:15px; width:100%; background:#FFF; border:1px solid #E7E8E9; padding:23px 24px; z-index:10;`
  - Variant `.sub-menu-collection`: `width:max-content; left:0; max-width:unset;`
  - Variant `.sub-menu-about` (for "Về Chúng Tôi"): `display:block !important` on hover instead of flex (simple vertical list, no columns).
- Implementation approach: CSS `:hover` + `display` toggle + a CSS `@keyframes fade_in_show` (opacity 0→1 fade over 0.5s). No JS needed for desktop.
- Menu items with dropdowns: NỮ, NAM, HÈ SALE CHẠM ĐỈNH 70%, Bộ sưu tập (collection variant), Về Chúng Tôi (about variant). "LIFESTYLE" has no dropdown (plain link).

## 3. Home Banner — Owl Carousel (autoplay + click)
- 2 real slides (`.owl-item:not(.cloned)`), each full-width image + link.
- Controls: `.owl-nav` with `.owl-prev`/`.owl-next` (icon-ic_left-arrow/icon-ic_right-arrow), `.owl-dots` with `.owl-dot` (active state = filled dot).
- Interaction model: time-driven autoplay (standard Owl Carousel behavior) + click-driven arrows/dots override. Drag/swipe also enabled (`owl-drag` class present).
- Slide 1: image `https://cotton4u.vn/files/news/2026/06/19/6a051c7c1a148911a0f04bb13704e9e4.webp` → links to `/lookbook/daily-mood-226`
- Slide 2: image `https://cotton4u.vn/files/news/2026/06/23/da4faa3fe3af0cef91c4696275413c54.webp` → links to `/danh-muc/sale-all-70-0626`

## 4. Product Carousel Sections (NEW ARRIVAL / Featured / Sale) — click-driven tabs + carousel
- Section title: `.title-section { font-size:30px; font-weight:600; font-family:Montserrat; color:#221F20; }`
- Tabs (`.exclusive-tab`, present in 2 of 3 sections): `font-size:20px; font-weight:400; color:#221F20;` — clicking a tab sets `.active` and shows matching `.exclusive-inner#tab-X.active` (`display:none` → `display:block`), hides the rest. Pure click-driven, NOT scroll driven (confirmed no IntersectionObserver / scroll listener present, plain `<li data-tab="tab-women">` click handlers).
- Non-tabbed section (Featured Collection) just renders one `.exclusive-inner.active` directly.
- Carousel: Owl Carousel, click-driven arrows/drag, no autoplay observed (product grids).

### Product Card (`.item-new-prod`) — shared across all 3 sections
- Thumbnail swap on hover: `.thumb-product .hover-img { position:absolute; top:0; left:0; opacity:0; visibility:hidden; transition: all 0.3s ease-in-out; }` → `.thumb-product:hover .hover-img { opacity:1; visibility:visible; }`. Pure CSS `:hover`, no JS.
- Color swatches: `.list-color ul li` small circular swatch images (`checked` class = currently selected swatch, shown with a ring/border).
- Favorite/heart icon: `.favourite` top-right of image, icon font glyph (heart outline).
- Title: `.title-product a { color:#57585A; font-size:14px; line-height:16px; font-weight:400; text-transform:capitalize; }`
- Price: `.price-product ins { color:#3E3E3F; font-weight:600; font-size:16px; line-height:24px; text-decoration:none; }`, discounted original price `.price-product del { color:#A8A9AD; font-size:12px; line-height:16px; font-family:'Roboto',sans-serif; text-decoration line-through (browser default for <del>) }`.
- Discount badge (when on sale): `.badget.badget_02 { }` renders e.g. "-45%" top-left of thumbnail (exact positioning/colors: red background typical for sale badges — verify against screenshot; class-based, content is server-rendered text).
- Add-to-cart button: `.add-to-cart a { position:absolute; bottom:0; right:0; background:#221F20; border-radius:8px 0; width:32px; height:32px; color:#FFF; display:flex; align-items:center; justify-content:center; }`.
- Size selector popup (`.list-size`):
  - Before: `position:absolute; bottom:0; right:0; background:#FFF; border:1px solid #E7E8E9; opacity:0; visibility:hidden; transition: all 0.3s ease-in-out;`
  - After (`.list-size.open` — JS-toggled class, likely on add-to-cart hover/click): `opacity:1; visibility:visible; bottom:35px;`
  - Size link: `font-weight:600; font-size:16px; color:#57585A;` → hover `color:#221F20`. Out-of-stock size: `.unactive { pointer-events:none; } .unactive a { color:#D1D2D4; }`.
  - **Trigger note:** class is JS-toggled (`open`), not a pure `:hover` selector on a parent — build with onMouseEnter/onMouseLeave on the add-to-cart button toggling local state, matching the same visual transition.

## 5. Brand/Promo Carousel (`.list-ads-brand`)
- Title: same `.title-section` styling.
- Owl Carousel, 2 real slides visible at once (660px wide items, 30px gap), `owl-drag` enabled, click-driven arrows.

## 6. Gallery (`.home-gallery`)
- Title: `.title-gallery` (h3).
- Owl Carousel of square images (246px wide, 30px gap), links to product pages, click-driven arrows/drag.

## 7. Footer
- No interactive behaviors beyond standard link hovers and the email subscribe form (`#frm_subscribe`, out of scope — no backend).
- Social icons: plain `<img>` icons linking out (Facebook, Google, Instagram — Pinterest commented out in source, do not include).

## Responsive Breakpoints (from production CSS `@media` rules)
- `max-width: 768px` — mobile: header height 64px, `.menu{display:none}` (hamburger `.mobile-menu` shown instead), search form hidden from header bar, `main{padding-top:64px}`.
- `min-width: 992px` / `1200px` / `1365px` / `1440px` / `1600px` / `1860px` — progressive desktop grid/spacing refinements (container max-widths widen at each step). Treat 1200px as the primary desktop breakpoint and 768px as the primary mobile breakpoint for this clone; use Tailwind's `md`/`lg`/`xl` accordingly.
- No scroll-snap, no Lenis/Locomotive smooth scroll, no `data-aos` scroll-reveal animations anywhere on the homepage.

## Global animation
- `@keyframes fade_in_show` used for the mega-menu reveal (opacity 0→1 over 0.5s). No other named keyframes affect the homepage.
