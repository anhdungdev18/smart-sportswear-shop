# ArticleCategorySidebar Specification

## Overview
- **Target file:** `src/components/ArticleCategorySidebar.tsx`
- **Interaction model:** static (plain links; the real site swaps this for a `<select>` dropdown on mobile — see Responsive section)
- **Props:** none — hardcode the 5 real category links below.

## DOM Structure
```
aside (sticky on desktop: lg:sticky lg:top-24)
  div ("Danh mục" title)
  ul
    li > a (× 5, each with a right-pointing chevron icon)
```

## Computed Styles (faithful approximation matching the live site's design language)
```css
.aside-news-title { font-weight: 600; font-size: 18px; color: #221F20; margin-bottom: 20px; }
ul li a { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #E7E8E9; font-size: 14px; color: #57585A; }
ul li a:hover { color: #221F20; }
ul li:last-child a { border-bottom: 0; }
```
Chevron icon: use `RightArrowIcon` from `src/components/icons.tsx`, sized `size-4`, `text-ivy-text-muted`.

## Text Content (verbatim, real links — external reference links since these category pages aren't part of this clone, prefix with `https://ivymoda.com`)
- Title: "Danh mục"
- "Sự kiện thời trang" → `/tin-tuc/danh-muc/su-kien-thoi-trang`
- "Blog chia sẻ" → `/tin-tuc/danh-muc/blog`
- "Fashion Show" → `/tin-tuc/danh-muc/Fashion-Show`
- "Hoạt động cộng đồng" → `/tin-tuc/danh-muc/community-activity`
- "Tin nội bộ" → `/tin-tuc/danh-muc/tin-noi-bo`

## Responsive Behavior
- **Desktop (≥1024px, `lg:`):** vertical list sidebar as described, sticky while scrolling past it.
- **Mobile (<1024px):** the live site swaps this for a `<select>` dropdown to save space. Replicate with a simple native `<select>` (same 5 options as `<option>` elements, `onChange` navigates via `window.location.href` or a no-op) shown only below `lg:` (`block lg:hidden` on the select, `hidden lg:block` on the list).
- **Breakpoint:** `lg:` (1024px).
