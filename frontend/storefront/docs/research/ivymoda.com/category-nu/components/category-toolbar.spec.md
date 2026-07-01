# CategoryToolbar Specification

## Overview
- **Target file:** `src/components/CategoryToolbar.tsx`
- **Interaction model:** click-driven dropdown (sort options)
- **Props:** `{ title: string; resultCount?: number }` — title is the page heading (e.g. "Thời Trang Nữ" or a search-results phrase like `Kết quả tìm kiếm theo 'vay'`).
- **Used on:** category page and search page (identical component).

## DOM Structure
```
div.top-main-prod (position:relative, margin-bottom:26px)
  h1.sub-title-main (page title)
  div.filter-prod (position:absolute, top:0, right:0)
    button (label "Sắp xếp theo" + chevron-down icon) — click toggles a dropdown panel
    div (dropdown panel, absolute, right-aligned, bg-white border shadow, appears below the button)
      button × 6, one per sort option, click sets the active option and closes the dropdown
```

## Computed Styles (exact)
```css
.sub-title-main { font-weight:600; font-size:24px; line-height:32px; color:#221F20; text-transform:uppercase; }
.top-main-prod { position:relative; margin-bottom:26px; }
.filter-prod { position:absolute; right:0; top:0; }
```
Dropdown panel: `bg-white border border-ivy-hairline rounded shadow-md`, options `px-4 py-2 text-sm text-ivy-text hover:bg-gray-50 cursor-pointer`, selected option `font-semibold text-ivy-dark`.

## Text Content (verbatim)
- Sort trigger label: "Sắp xếp theo"
- Sort options (in order): "Mặc định", "Mới nhất", "Được mua nhiều nhất", "Được yêu thích nhất", "Giá: cao đến thấp", "Giá: thấp đến cao"

## States & Behaviors
### Sort dropdown
- **Trigger:** click on the "Sắp xếp theo" button
- **Before:** dropdown panel hidden
- **After:** dropdown panel visible below the trigger; clicking any option sets local `useState<string>` selection, closes the dropdown, and updates the trigger label is NOT required (source site keeps "Sắp xếp theo" as the static label — only the active option gets a checkmark/bold treatment inside the panel). No real re-sorting needs to occur (mock/demo).
- Close on click-outside is a nice-to-have (use a simple blur/outside-click handler if trivial).

## Responsive Behavior
- **Desktop (≥768px):** as described, title left-aligned, sort dropdown absolutely positioned top-right.
- **Mobile (<768px):** stack the sort control below the title instead of absolute-positioning it (`static` position, `mt-2`), since absolute positioning against a short title would overlap on narrow screens.
- **Breakpoint:** `md:` (768px).
