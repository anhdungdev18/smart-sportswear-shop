# CategorySidebarFilter Specification

## Overview
- **Target file:** `src/components/CategorySidebarFilter.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/category-nu-desktop-full.png` (left column)
- **Interaction model:** click-driven accordion (each filter group expands/collapses independently, click-driven radio/checkbox selection). UI-only — no real filtering logic needed (mock/demo per project scope), but the accordion open/close and selection highlighting must work.
- **Used on:** category page (`/danh-muc/nu`) and search page (`/tim-kiem`) — identical component, reused as-is.

## DOM Structure
```
div.sidebar-prod (max-width:270px, w-full)
  ul.list-side
    li.item-side (× 5: Size, Màu sắc, Mức giá, Mức chiết khấu, Nâng cao — border-bottom divider between each, no border on last)
      p.item-side-title (row: label + plus/minus icon, click toggles this group's open state)
      div.sub-list-side (collapsed by default: display:none; expanded: display:block)
        [Size]: 5 pill radio buttons (S, M, L, XL, XXL) in a row-wrap, each ~48px square with rounded corner (8px 0), border, selected = dark border + dark text
        [Màu sắc]: grid of circular color swatch radios (each with a tooltip label like "Đen", "Đỏ" — simplify tooltip to a `title` attribute), selected = checkmark overlay
        [Mức giá]: a price range slider (two-handle) — build with a simple styled `<input type="range">` pair or a static-looking track+two handles; real dual-slider drag logic is a nice-to-have, not required. Show min/max value labels below (e.g. "0đ" — "5.000.000đ")
        [Mức chiết khấu]: radio list: "Dưới 30%", "30% - 50%", "Trên 50%" (representative options)
        [Nâng cao]: nested sub-accordion, one entry "Chất liệu" that itself expands to more checkboxes (2 levels deep) — build one representative nested group, doesn't need to be exhaustive
  div (bottom action row, 2 buttons)
    button "BỎ LỌC" (outline style, clear filters)
    button "LỌC" (solid dark style, apply filters)
```

## Computed Styles (exact)
```css
.sidebar-prod { max-width:270px; width:100%; }
.item-side { border-bottom:1px solid #F7F8F9; margin-bottom:16px; padding-bottom:16px; }
.item-side:last-child { border-bottom:0; margin-bottom:0; padding-bottom:0; }
.item-side-title /* aka h4 in spec */ { font-size:16px; line-height:20px; color:#221F20; display:flex; align-items:center; justify-content:space-between; margin-bottom:18px; font-weight:400; cursor:pointer; }
/* plus icon shown when collapsed, minus icon shown when expanded — toggle via local useState per accordion item, e.g. { openKey: string | null } */
.item-sub-list { font-weight:600; font-size:14px; line-height:16px; color:#221F20; margin-bottom:20px; }
.item-side-size .item-sub-list { display:inline-block; margin-right:16px; max-width:48px; width:100%; text-align:center; }
.item-side-size .item-sub-title { font-size:12px; line-height:16px; padding:8px 0; color:#6C6D70; display:block; position:relative; }
.item-side-size .item-sub-title::before { content:""; position:absolute; inset:0; border:1px solid #E7E8E9; border-radius:8px 0px; }
.item-side-size input:checked ~ .item-sub-title::before { border:1px solid #221F20; }
.item-side-size input:checked ~ .item-sub-title { color:#221F20; }
.item-side-color .item-sub-list { display:inline-block; margin-right:16px; }
.item-side-color .item-sub-title { padding-left:18px; position:relative; cursor:pointer; }
.item-side-color .item-sub-title::before { content:""; position:absolute; left:0; top:-1px; width:18px; height:18px; border-radius:50%; background: var(--swatch-color); } /* set the swatch's actual hex via inline style per color */
/* Sample color hex values seen on the live site: Đen #221F20, Đỏ #D73831, Cam #E7973E, Vàng #EEB256, Cam đất #DC633A, Đỏ đô #AC2F33, Trắng #F7F8F9, Xám nhạt #E7E8E9, Xám #D1D2D4, Xám đậm #BCBDC0, Be #A8A9AD, Nâu nhạt #939598, Nâu #808285, Nâu đậm #6C6D70 */
```
Buttons at bottom: `BỎ LỌC` = `border border-ivy-dark text-ivy-dark bg-white rounded-full px-6 h-10`; `LỌC` = `bg-ivy-dark text-white rounded-full px-6 h-10`. Both inline-flex side by side with a gap.

## States & Behaviors
### Accordion expand/collapse
- **Trigger:** click on `.item-side-title` row
- **Before:** `.sub-list-side{display:none}`, plus-icon visible
- **After:** `display:block`, minus-icon visible
- Only one open at a time is NOT required (the source site allows multiple open simultaneously) — use independent boolean state per section, e.g. `useState<Record<string, boolean>>`.

### Radio/checkbox selection
- Clicking a size pill / color swatch / discount option marks it selected (local state), toggling the checked visual style described above. No real filtering needs to occur.

## Text Content (verbatim)
- Section titles: "Size", "Màu sắc", "Mức giá", "Mức chiết khấu", "Nâng cao"
- Size options: S, M, L, XL, XXL
- Discount options: "Dưới 30%", "30% - 50%", "Trên 50%"
- Advanced nested group label: "Chất liệu"
- Buttons: "BỎ LỌC", "LỌC"

## Responsive Behavior
- **Desktop (≥1024px):** fixed 270px sidebar, sits left of the product grid (flex row).
- **Mobile (<1024px):** the live site hides this sidebar and shows a "BỘ LỌC" trigger that opens a mobile filter drawer/modal — for this pass, it's acceptable to just stack the sidebar above the product grid on mobile (`flex-col lg:flex-row`) rather than building a full modal, given it's a UI-only demo. Note this simplification with a one-line comment in the component.
- **Breakpoint:** `lg:` (1024px).
