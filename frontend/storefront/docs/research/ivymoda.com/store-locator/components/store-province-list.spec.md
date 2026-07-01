# StoreProvinceList Specification

## Overview
- **Target file:** `src/components/StoreProvinceList.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/store-locator-desktop-full.png` (left column)
- **Interaction model:** click-driven region tabs (Miền Bắc/Miền Trung/Miền Nam switch the visible province list) + click-driven accordion (clicking a province name expands/collapses its store list). No real map/backend wiring needed — UI-only per project scope.
- **Props:** none — hardcode the data below directly in this file.

## DOM Structure
```
div (root)
  div (region tabs row, centered, border-bottom): 3 tabs "Miền Bắc" (active by default), "Miền Trung", "Miền Nam"
  div (panel, border rounded card, p-6)
    h3 "Cửa hàng Miền <X>" (updates with active region)
    div (search input with a search icon, placeholder "Tìm cửa hàng" — UI-only, no real filtering required, though a simple client-side substring filter on province name is a nice-to-have)
    ul (province accordion list)
      li × N (one per province)
        button (province name + chevron, click toggles this province's store list open/closed; only "Hà Nội" starts open)
        div (collapsible store list, only rendered when open)
          div × (stores for that province, only Hà Nội has real data — see below)
            p (store name/address)
            p (phone, with a phone icon)
            a "Chỉ đường" (get-directions link — build a real Google Maps directions URL: `https://www.google.com/maps/dir/Current+Location/<url-encoded store address>`, matches the live site's actual link pattern)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.region-tabs { display: flex; justify-content: center; gap: 32px; border-bottom: 1px solid #E7E8E9; margin-bottom: 24px; }
.region-tab { font-size: 16px; font-weight: 500; color: #6C6D70; padding-bottom: 8px; cursor: pointer; }
.region-tab.active { color: #221F20; border-bottom: 2px solid #221F20; font-weight: 600; }
.panel { border: 1px solid #E7E8E9; border-radius: 16px; padding: 24px; }
.panel h3 { font-size: 18px; font-weight: 600; color: #221F20; margin-bottom: 16px; }
.search-input { border: 1px solid #E7E8E9; border-radius: 999px; padding: 10px 16px; font-size: 14px; width: 100%; margin-bottom: 8px; }
.province-item button { width: 100%; display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid #F7F8F9; font-size: 15px; color: #221F20; }
.store-detail { padding: 12px 0 12px 8px; border-bottom: 1px solid #F7F8F9; }
.store-detail p { font-size: 13px; color: #57585A; line-height: 20px; }
.store-detail a { color: #AC2F33; font-size: 13px; }
```
Chevron icon: use Lucide `ChevronDown` rotating 180deg when open (import directly from `lucide-react`, or add a `ChevronDownIcon` export to `src/components/icons.tsx` if not already present — check the file first, it may already have one).

## Content (verbatim, real data)

**Region tabs:** "Miền Bắc" (default active), "Miền Trung", "Miền Nam" — for Miền Trung and Miền Nam, it's acceptable to show a shorter representative province list (2-3 real Vietnamese province/city names each, e.g. Miền Trung: "Đà Nẵng", "Huế", "Nghệ An"; Miền Nam: "TP. Hồ Chí Minh", "Cần Thơ", "Bình Dương") with an empty/placeholder store list ("Đang cập nhật" — updating soon) since exhaustive real store data for all provinces isn't necessary for this demo.

**Miền Bắc province list (real, in order):** Hà Nội, Hải Phòng, Bắc Giang, Hải Dương, Hưng Yên, Lào Cai, Nam Định, Ninh Bình, Phú Thọ, Quảng Ninh, Thái Bình, Thái Nguyên, Tuyên Quang, Vĩnh Yên, Yên Bái.

**Hà Nội stores (open by default, real data — only these 2 need full detail, other provinces can show "Đang cập nhật"):**
1. "IVY moda 267 Đ. Quang Trung, P. Quang Trung (Hà Đông), TP. Hà Nội" — phone "0243 834 1002"
2. "IVY moda 261-263 Cao Lỗ, Uy Nỗ, Đông Anh, Hà Nội" — phone (use a placeholder real-format number like "0243 834 1003" since the exact second number wasn't captured — reasonable approximation)

**Search placeholder:** "Tìm cửa hàng"

## Responsive Behavior
- **Desktop (≥1024px):** as described, sits left of `StoreMapPanel` in a 2-column layout (handled by the parent page).
- **Mobile (<1024px):** full width, stacks above the map panel.
- **Breakpoint:** `lg:` (1024px).
