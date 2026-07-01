# HeaderActions Specification

## Overview
- **Target file:** `src/components/HeaderActions.tsx` (rendered inside `SiteHeader`, positioned in the header's right side)
- **Screenshot:** `docs/design-references/ivymoda.com/header-default.png`
- **Interaction model:** static layout; search input is a controlled text field with no live backend (mock — pressing enter/clicking submit can be a no-op `onSubmit={(e) => e.preventDefault()}`). Cart count badge is static (mock value `0`).

## DOM Structure
```
div.right-header (flex, height:40px, gap between children)
  form.search-form (icon-button + text input, out of scope for full quick-search dropdown results — build just the input + search icon)
    button.submit > SearchIcon
    input#search-quick (placeholder "TÌM KIẾM SẢN PHẨM")
  a (Outlet text link) → mock href "#"
  div.icon (Headphones/CS icon — hover reveals a small dropdown with support links; build as a simple dropdown list, no backend)
    ul: Hotline (tel link, PhoneCallIcon), Live Chat (ChatIcon), Messenger (MessengerIcon), Email (EnvelopeIcon), "Tra cứu đơn hàng" (OrderLookupIcon)
  a.icon (AvatarIcon) → "/customer/login" styled as mock link "#"
  a.icon (ShoppingBagIcon + cart count badge showing "0")
```

## Computed Styles
- Container: `.right-header { display:flex; align-items:center; height:40px; }` (children spaced with gaps — use `gap-6` / `24px` between clusters as a reasonable match to the reference screenshot)
- Search input: `background:#FFF; color:#87888A` (rgb(87,88,90) = `#57585A`, use this instead as more precise per CSS ground truth); `border-color:#E7E8E9`; `font-size:12px`.
- Search submit button: `background:#FFF; color:#221F20; font-size:14px;` icon-only button, no visible border by default.
- Icon links (`.icon`): plain icon glyphs, color `#221F20`, sized ~20-24px to match reference screenshot proportions.
- Cart badge (`.number-cart`): small circular counter overlapping the top-right of the bag icon — style as `absolute -top-1 -right-1 bg-[#AC2F33] text-white text-[10px] rounded-full w-4 h-4 flex items-center justify-center`.

## Text Content (verbatim)
- Search placeholder: "TÌM KIẾM SẢN PHẨM"
- "Outlet" link label: "Outlet"
- CS dropdown items: "Hotline" (`tel:02466623434`), "Live Chat", "Messenger", "Email" (`mailto:saleadmin@ivy.com.vn`), "Tra cứu đơn hàng"
- Cart count: "0"

## Assets / Icons
Use from `src/components/icons.tsx`: `SearchIcon`, `HeadphonesIcon`, `PhoneCallIcon`, `ChatIcon`, `MessengerIcon`, `EnvelopeIcon`, `OrderLookupIcon`, `AvatarIcon`, `ShoppingBagIcon`.

## States & Behaviors
- CS icon dropdown: hover-driven (same `group-hover` pattern as nav mega menu), simple vertical list, `bg-white border border-[#E7E8E9] shadow-sm` dropdown panel below the icon.
- Search input focus: standard browser focus ring is acceptable (no custom focus style observed beyond border color).
- Cart/avatar links: static hrefs `"#"` (no real account/cart system — mock per project scope).

## Responsive Behavior
- **Desktop (≥1200px):** full row as described, all icons + search + outlet link visible.
- **Mobile (<768px):** per production CSS, `.right-header form { display:none }` (search hidden from the header bar on mobile) — hide the search input at `<768px` (`hidden md:flex` on the search form), keep account/cart icons visible.
- **Breakpoint:** 768px.
