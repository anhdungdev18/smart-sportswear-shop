# Breadcrumb Specification

## Overview
- **Target file:** `src/components/Breadcrumb.tsx`
- **Interaction model:** static (plain links)
- **Props:** `{ items: { label: string; href?: string }[] }` — last item has no href (current page, not a link).
- **Used on:** category page, product detail, lookbook, news, about, search (all 5 new pages get a breadcrumb under the header).

## DOM Structure
```
nav (ol.breadcrumb__list)
  li.breadcrumb__item > a.breadcrumb__link (for each item except last)
  li.breadcrumb__item > span (last item, current page, no link)
```

## Computed Styles (exact)
```css
.breadcrumb__list { display:flex; align-items:center; padding: 12px 0; list-style:none; }
.breadcrumb__item:first-child .breadcrumb__link { color:#6c6d70; }
.breadcrumb__item:first-child .breadcrumb__link:hover { color:#221f20; }
.breadcrumb__item + .breadcrumb__item .breadcrumb__link { color:#221f20; }
.breadcrumb__item + .breadcrumb__item:before { content:"›"; margin: 0 8px; color:#6c6d70; } /* the live site uses an icon font glyph here; a plain "›" character is a faithful simplification */
```
Font-size ~13-14px, matches body text size. Wrap the whole breadcrumb in the page's standard `mx-auto max-w-[1380px] px-4` container.

## Example usage
```tsx
<Breadcrumb items={[{ label: "Trang chủ", href: "https://ivymoda.com/" }, { label: "NỮ", href: "https://ivymoda.com/danh-muc/nu" }, { label: "Áo kiểu Day Dream" }]} />
```

## Responsive Behavior
Same on all breakpoints — no layout change, text may wrap on very narrow mobile widths (acceptable).
