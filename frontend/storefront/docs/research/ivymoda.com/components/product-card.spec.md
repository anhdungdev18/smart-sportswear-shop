# ProductCard Specification

## Overview
- **Target file:** `src/components/ProductCard.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/section-new-arrival.png` (product cards visible in grid)
- **Interaction model:** hover-driven (thumbnail image swap is pure CSS `:hover`; size-selector popup is JS-toggled state on add-to-cart hover)
- **Props:** `product: Product` (see `src/types/ivy.ts`)

## DOM Structure
```
div.item-new-prod
  div.product (position:relative)
    span.badget.badget_02 (optional, only if product.discountPercent is set) — "-45%" style badge, top-left
    div.thumb-product (position:relative, margin-bottom:17px)
      a > img (primary thumbnail, class="owl-lazy")
      a > img.hover-img (secondary/hover thumbnail, position:absolute top:0 left:0, opacity:0→1 on :hover)
    div.info-product
      div.list-color (flex, justify-content:space-between, margin-bottom:13px)
        ul > li.color-picker (× N, circular swatch images, 18px, margin-right:10px; `.checked` = selected, shows a white checkmark overlay via ::before)
        div.favourite (HeartIcon, cursor:pointer)
      h3.title-product > a (product name)
      div.price-product
        ins (current price) [+ del (old price) if discounted]
    div.add-to-cart > a (dark square button, bottom-right of thumbnail, ShoppingBagIcon)
    div.list-size (popup revealed near add-to-cart, size buttons)
```

## Computed Styles (exact)
```css
.thumb-product .hover-img { position:absolute; top:0; left:0; opacity:0; visibility:hidden; transition: all 0.3s ease-in-out; height:100%; }
.thumb-product:hover .hover-img { opacity:1; visibility:visible; } /* pure CSS hover, no JS */
.title-product a { color:#57585A; font-size:14px; line-height:16px; font-weight:400; text-transform:capitalize; margin-bottom:10px; display:block; }
.price-product ins { color:#3E3E3F; font-weight:600; font-size:16px; line-height:24px; text-decoration:none; display:inline-block; vertical-align:middle; }
.price-product del { color:#A8A9AD; font-size:12px; line-height:16px; font-weight:400; font-family:'Roboto',sans-serif; display:inline-block; vertical-align:middle; margin-left:8px; text-decoration:line-through; }
.list-color { margin-bottom:13px; display:flex; justify-content:space-between; }
.list-color ul li { width:18px; margin-right:10px; border-radius:50%; display:inline-block; position:relative; cursor:pointer; }
.list-color ul li.checked::before { content:""; left:6px; top:3px; width:5px; height:9px; border:solid #FFF; border-width:0 1px 1px 0; transform:rotate(45deg); position:absolute; z-index:1; } /* checkmark drawn over the swatch */
.add-to-cart a { position:absolute; bottom:0; right:0; background:#221F20; border-radius:8px 0; width:32px; height:32px; color:#FFF; display:flex; align-items:center; justify-content:center; border:1px solid transparent; }
.add-to-cart a:hover { color:#221F20; background:#FFF; border:1px solid #221F20; }
.list-size { position:absolute; bottom:0; right:0; background:#FFF; border:1px solid #E7E8E9; opacity:0; visibility:hidden; transition: all 0.3s ease-in-out; }
.list-size.open { opacity:1; visibility:visible; bottom:35px; } /* JS-toggled class */
.list-size li a { font-weight:600; font-size:16px; line-height:24px; color:#57585A; margin-bottom:16px; display:block; }
.list-size li a:hover { color:#221F20; }
.list-size li.unactive a { color:#D1D2D4; pointer-events:none; } /* out-of-stock size */
.badget { z-index:1; position:absolute; font-weight:600; font-size:14px; color:#fff; padding:13px 12px 13px 8px; top:8px; right:9px; } /* background is a small decorative ribbon image on the live site; approximate with a solid rounded-corner tag using #AC2F33 background instead of importing the ribbon PNG */
```
Card width in its carousel context: `246px` with `30px` right margin (set by the parent carousel, not the card itself — card should be `w-full` and let the carousel control column width).

## States & Behaviors
### Thumbnail hover-swap
- **Trigger:** CSS `:hover` on `.thumb-product`
- **Before:** hover-img `opacity:0; visibility:hidden`
- **After:** `opacity:1; visibility:visible`
- **Transition:** `0.3s ease-in-out`

### Size popup
- **Trigger:** mouse enter/leave on the card's add-to-cart button (or the whole card — match reference screenshot; implement with React `onMouseEnter`/`onMouseLeave` toggling local `open` state, applied as a conditional class)
- **Before:** `opacity:0; visibility:hidden; bottom:0`
- **After:** `opacity:1; visibility:visible; bottom:35px`
- **Transition:** `all 0.3s ease-in-out`

### Color swatch selection
- Clicking a swatch sets it `.checked` (only one active at a time) — implement with local `useState` for `selectedColorId`, defaulting to `product.colors.find(c => c.active)`.

## Text/Data Content
Use the `Product` type. Example real product (from NEW ARRIVAL tab):
```json
{
  "id": "44652",
  "name": "Áo kiểu Day Dream",
  "href": "https://ivymoda.com/sanpham/ao-kieu-day-dream-ms-16b0570-44652",
  "image": "/images/ivymoda/products/2cf9b85228b3e78f22cfc3718f6e24b6.webp",
  "hoverImage": "/images/ivymoda/products/15562de9138d1c5927ee7f195c512e06.webp",
  "price": 690000,
  "colors": [{ "id": "44652", "image": "/images/ivymoda/colors/h01.png", "label": "h01", "active": true }],
  "sizes": [{"id":"215449","label":"s"},{"id":"215450","label":"m"},{"id":"215451","label":"l"},{"id":"215452","label":"xl"},{"id":"215453","label":"xxl"}]
}
```
Format price with Vietnamese thousands separator + "đ" suffix, e.g. `690.000đ` (use `product.price.toLocaleString("vi-VN") + "đ"`).

## Responsive Behavior
- **Desktop (1440px):** fixed ~246px card width inside carousel.
- **Mobile (390px):** card shrinks to fit 1.5-2 visible per viewport in the carousel; font sizes unchanged (no mobile-specific overrides found for card typography).
- **Breakpoint:** carousel item width changes are controlled by the parent `ProductCarouselSection`, not this component.
