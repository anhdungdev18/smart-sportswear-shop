# ContactInfoCards Specification

## Overview
- **Target file:** `src/components/ContactInfoCards.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/contact-desktop-full.png` (left column)
- **Interaction model:** static
- **Props:** none — hardcode the 4 real cards below.

## DOM Structure
```
div (root, flex flex-col gap-4)
  div.ci__item (× 4, border rounded-lg p-5, flex gap-4 items-start)
    div.ci__item__icon (circular icon badge, bg-ivy-hairline/50, flex items-center justify-center, shrink-0)
    div.ci__item__info
      h4 (label, bold)
      p (value text, can be multi-line)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.ci__item { border: 1px solid #E7E8E9; border-radius: 12px; padding: 20px; display: flex; gap: 16px; align-items: flex-start; }
.ci__item__icon { width: 48px; height: 48px; border-radius: 50%; background: #F7F8F9; display: flex; align-items: center; justify-content: center; color: #221F20; }
.ci__item__info h4 { font-size: 16px; font-weight: 600; color: #221F20; margin-bottom: 6px; }
.ci__item__info p { font-size: 14px; line-height: 22px; color: #57585A; }
```
Icons (the live site's icons failed to load/render as blank circles — use these reasonable Lucide equivalents instead, imported directly from `lucide-react`): `MapPin` (Địa chỉ), `Mail` (Email), `ShoppingBag` (Mua hàng online), `Headphones` (Chăm sóc khách hàng).

## Text Content (verbatim)
1. **Địa chỉ**: "Tầng 14, Toà nhà Hapulico Complex 24T- 85 Vũ Trọng Phụng - Quận Thanh Xuân, HN"
2. **Email**: "cskh@ivy.com.vn"
3. **Mua hàng online**: "02466623434"
4. **Chăm sóc khách hàng**: "Email: cskh@ivy.com.vn" / "Hotline: 0905 89 86 83" / "Thứ Hai đến Thứ Bảy, từ 8:00 đến 17:30" (render as 3 separate lines within the same `<p>`, using `<br/>` between them)

## Responsive Behavior
- **Desktop (≥1024px):** left column, stacked vertically, paired with `ContactForm` on the right.
- **Mobile (<1024px):** full width, same vertical stack.
- **Breakpoint:** `lg:` (1024px).
