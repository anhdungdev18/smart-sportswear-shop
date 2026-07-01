# Pagination Specification

## Overview
- **Target file:** `src/components/Pagination.tsx`
- **Interaction model:** click-driven (page links)
- **Props:** `{ currentPage: number; totalPages: number; baseHref: string }` — generates page links as `${baseHref}/${page}` (page 1 has no suffix, matching the live site's `/danh-muc/nu`, `/danh-muc/nu/2`, `/danh-muc/nu/3` pattern).
- **Used on:** category page and search page.

## DOM Structure
```
ul.list-inline-pagination (flex row, centered, list-style:none)
  li > a "«" (previous page)
  li > a (page number, current page has a distinct active style) — show a window of ~5 page numbers around the current page
  li > a "»" (next page)
  li.last-page > a "Trang cuối" (jump to last page)
```

## Computed Styles (exact)
```css
.list-inline-pagination { display:flex; align-items:center; justify-content:center; list-style:none; padding-left:0; }
.list-inline-pagination li { background:#FFFFFF; border:1px solid #E7E8E9; border-radius:8px 0px; height:32px; min-width:32px; font-size:12px; color:#6C6D70; text-align:center; line-height:30px; margin-right:12px; }
.list-inline-pagination li:first-child { margin-right:17px; }
.list-inline-pagination li a { display:block; min-width:32px; }
.list-inline-pagination .first-page, .list-inline-pagination .last-page { padding:0 12px; }
```
Active/current page: `bg-ivy-dark text-white border-ivy-dark` (inverted from the default white/gray style — the live site marks it via an `id`/active class; replicate the visual distinction with a conditional class).

## Text Content (verbatim)
- Prev arrow: "«", Next arrow: "»", last-page link label: "Trang cuối"

## Responsive Behavior
Same layout at all breakpoints — the row may wrap or the page-number window can shrink to 3 on mobile (`hidden md:inline-block` on some middle page links) to avoid overflow on narrow screens.
