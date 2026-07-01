# StoreMapPanel Specification

## Overview
- **Target file:** `src/components/StoreMapPanel.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/store-locator-desktop-full.png` (right column)
- **Interaction model:** static. IMPORTANT: the live site embeds a real Google Maps JS widget, but it's out of scope to wire a real Maps API (requires a paid API key — the live site itself shows a "This page can't load Google Maps correctly" error, confirming this integration is broken/backend-dependent even on the original). Build a static placeholder instead: a neutral map-toned background (e.g. a light gray-green div with a subtle grid pattern via CSS, or just a solid `bg-[#E8EAE6]` block) with 2-3 pin icons (Lucide `MapPin`) scattered at fixed positions to visually suggest a map, plus a small "Bản đồ minh họa" (illustrative map) caption — this is a deliberate, documented simplification, not a bug.
- **Props:** none — hardcode the policy text below.

## DOM Structure
```
div (root)
  div (map placeholder area, aspect-[4/3] or h-[500px], rounded-lg, relative, overflow-hidden)
    div (neutral background fill)
    MapPin icon × 3 (absolutely positioned at varied top/left percentages, text-ivy-accent, size-8, drop-shadow)
    span (small caption, absolute bottom-2 right-2, text-xs, text-white bg-black/40 px-2 py-1 rounded, "Bản đồ minh họa")
  div (policy text block, mt-6)
    h4 "Chính sách vận chuyển" (bold)
    ul (3 bullet items)
    h4 "Chính sách miễn phí vận chuyển" (bold, mt-4)
    ul (2 bullet items)
```

## Computed Styles (faithful approximation matching screenshot)
```css
h4 { font-size: 15px; font-weight: 600; color: #221F20; margin-bottom: 8px; }
ul { list-style: disc; padding-left: 20px; }
li { font-size: 14px; line-height: 22px; color: #57585A; margin-bottom: 4px; }
li .highlight { color: #E74C3C; } /* the live site colors specific phrases red via inline style */
```

## Text Content (verbatim)

**Chính sách vận chuyển:**
- "Nội thành Hà Nội, TP Hồ Chí Minh: **đồng giá 25k**" (the bolded/red part is "đồng giá 25k")
- "Đơn đi tỉnh gần (Khoảng cách đến HN/HCM < 100km): đồng giá 33k"
- "Đơn đi tỉnh gần (Khoảng cách đến HN/HCM > 100 km): đồng giá 38k"

**Chính sách miễn phí vận chuyển:**
- "**Miễn phí vận chuyển** cho đơn hàng giá trị từ 700,000đ" (red part: "Miễn phí vận chuyển")
- "**Miễn phí vận chuyển** cho đơn hàng có sản phẩm nguyên giá > 500k" (red part: "Miễn phí vận chuyển")

Use `text-[#E74C3C]` (or the project's `ivy-sale` token, which is `#FF0000` — either red works, `#E74C3C` matches this specific page's exact shade) for the highlighted phrases.

## Responsive Behavior
- **Desktop (≥1024px):** right column, map placeholder roughly square/4:3, policy text below it.
- **Mobile (<1024px):** full width, map placeholder shorter (`h-[300px]`).
- **Breakpoint:** `lg:` (1024px).
