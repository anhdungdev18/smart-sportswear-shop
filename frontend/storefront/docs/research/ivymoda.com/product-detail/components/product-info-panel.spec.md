# ProductInfoPanel Specification

## Overview
- **Target file:** `src/components/ProductInfoPanel.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/product-detail-desktop-full.png` (right column, top half)
- **Interaction model:** click-driven (color/size selection, quantity stepper, add-to-cart/buy buttons — all UI-only/mock, no cart backend).
- **Props:**
```ts
interface ProductInfoPanelProps {
  name: string;
  sku: string;
  ratingPercentage: number; // 0-100, e.g. 100 = full 5 stars
  reviewCount: number;
  price: number;
  colors: { id: string; image: string; label: string; active?: boolean }[];
  sizes: { id: string; label: string; quantity: number }[]; // quantity 0 = out of stock, disabled
}
```

## DOM Structure
```
div (root)
  h1 (product name, uppercase)
  div (sub-info row: "SKU: <code>" + star rating + "(<N> đánh giá)")
  div (price, bold large)
  div (color section: "Màu sắc: <selected label>" + row of circular swatch radio buttons)
  div (size section: row of size buttons S/M/L/XL/XXL, disabled style for quantity=0)
    a "Kiểm tra size của bạn" (icon + link — opens a size-guide modal; a simplified static modal or even a no-op link is acceptable, this is a secondary affordance)
  div (quantity stepper: "Số lượng" label + [-] [number input] [+] )
  div (action row: "THÊM VÀO GIỎ" solid dark button, "MUA HÀNG" outline button, heart/wishlist icon button)
  a "Tìm tại cửa hàng" (underlined text link, find-in-store — mock href="#")
```

## Computed Styles (exact where captured, otherwise faithful approximation matching the screenshot)
```css
h1 { font-size: 28px; font-weight: 600; text-transform: uppercase; color: #221F20; margin-bottom: 12px; }
/* SKU + rating row */ p, .product-detail__rating span { font-size: 14px; color: #6C6D70; }
/* rating bar is percentage-based: a background row of 5 outline stars with a foreground row of 5 filled stars clipped to `width: <ratingPercentage>%` via overflow:hidden. Simplify with 5 Lucide Star icons, filling floor(ratingPercentage/20) of them solid amber (#F5A623 or similar) and the rest outline gray. */
.product-detail__price b { font-size: 24px; font-weight: 700; color: #221F20; }
/* color/size section labels */ p { font-size: 14px; font-weight: 600; color: #221F20; margin-bottom: 8px; }
/size buttons/ button.size-option { min-width: 48px; height: 40px; border: 1px solid #E7E8E9; border-radius: 8px 0px; text-transform: uppercase; font-size: 14px; }
button.size-option[data-selected="true"] { border-color: #221F20; color: #221F20; font-weight: 600; }
button.size-option:disabled { color: #D1D2D4; border-color: #E7E8E9; cursor: not-allowed; text-decoration: line-through; }
/* quantity stepper */ input[name=quantity] { width: 48px; text-align: center; border: 1px solid #E7E8E9; height: 40px; }
.product-quantity { width: 40px; height: 40px; border: 1px solid #E7E8E9; display:flex; align-items:center; justify-content:center; cursor:pointer; }
/* buttons */ button.add-to-cart-detail /* "THÊM VÀO GIỎ" */ { background:#221F20; color:#FFF; height:48px; padding:0 32px; border-radius: 8px 0px; font-weight:600; text-transform:uppercase; }
button.btn--outline /* "MUA HÀNG" */ { background:#FFF; color:#221F20; border:1px solid #221F20; height:48px; padding:0 32px; border-radius: 8px 0px; font-weight:600; text-transform:uppercase; }
button.btn--wishlist { width:48px; height:48px; border:1px solid #221F20; border-radius: 8px 0px; display:flex; align-items:center; justify-content:center; }
a[href*=cuahang] /* "Tìm tại cửa hàng" */ { font-size:14px; color:#221F20; text-decoration:underline; }
```

## States & Behaviors
### Color selection — click sets `selectedColorId`, updates the "Màu sắc: <label>" text to match, highlights the selected swatch (ring or border).
### Size selection — click sets `selectedSizeId` (only among sizes with `quantity > 0`); sizes with `quantity === 0` render disabled per the CSS above and are not clickable.
### Quantity stepper — `[-]`/`[+]` buttons adjust a local `quantity` state (`useState(1)`), clamped to a minimum of 1 (no real stock-max enforcement needed).
### Add to cart / Buy now — both are mock actions; `onClick` can be a no-op or a `console.log`, no real cart state needed for this clone.
### "Kiểm tra size của bạn" — mock link, `href="#"` or a simple `alert`/no-op; do NOT build the full multi-category size-chart modal (out of scope, minor secondary feature).

## Text Content (verbatim, real data for "Áo kiểu Day Dream")
- Name: "Áo kiểu Day Dream", SKU: "16B0570", rating: 100% / "(0 đánh giá)", price: 1.390.000đ (format via `toLocaleString("vi-VN")+"đ"`)
- Color: "Họa tiết Trắng" (single color option, swatch image `/images/ivymoda/colors/h01.png`)
- Sizes: S (qty 11), M (qty 26), L (qty 4), XL (qty 2), XXL (qty 2) — all in stock for this product
- Buttons: "THÊM VÀO GIỎ", "MUA HÀNG", "Tìm tại cửa hàng"
- Quantity label: "Số lượng"
- Size-guide link label: "Kiểm tra size của bạn"

## Responsive Behavior
- **Desktop (≥768px):** as described, right column of a 2-column layout (paired with `ProductGallery`).
- **Mobile (<768px):** stacks below the gallery, full width; action buttons can go full-width (`w-full`) stacked or two-up depending on space — match the general pattern of stacking on narrow screens used elsewhere in this project.
- **Breakpoint:** `md:` (768px).
