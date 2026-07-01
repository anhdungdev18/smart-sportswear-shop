# MagazineGrid Specification

## Overview
- **Target file:** `src/components/MagazineGrid.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/news-desktop-full.png` (3-column article row below the two featured stories)
- **Interaction model:** static cards, each links out to its article.
- **Props:** none — hardcode the 3 real items below directly in this file (page-specific content).

## DOM Structure
```
div (root, 3-column grid, gap)
  a.e-magazine-new-item × 3
    div.image > img (16:10-ish landscape photo)
    div.desc
      div.author (small gray paragraph — actually a short lede/caption, not a byline)
      div.title (bold headline)
      div.time (date, small gray)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.root { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-bottom: 48px; }
.image img { width: 100%; aspect-ratio: 4/3; object-fit: cover; display: block; margin-bottom: 16px; }
.desc .author { font-size: 13px; line-height: 20px; color: #57585A; margin-bottom: 8px; }
.desc .title { font-size: 15px; font-weight: 700; color: #221F20; line-height: 22px; margin-bottom: 8px; }
.desc .time { font-size: 12px; color: #A8A9AD; }
```

## Content (verbatim, real items — already downloaded to `public/images/ivymoda/news/`)

1. Image: `47b630796bec23aa195d7a59d1597231.jpg`
   - Author/caption: "Tối 21/10 , ca sĩ Văn Mai Hương đã có mặt tại Trung tâm Hội nghị Quốc Gia Hà Nội để tham dự EXPRESS_FALL/WINTER 2023 FASHION SHOW của IVY moda."
   - Title: "SÀN RUNWAY EXPRESS BÙNG NỔ VỚI 2 BẢN REMIX MỚI NHẤT CỦA CA SỸ VĂN MAI HƯƠNG"
   - Date: "23/10/2023"
2. Image: `3ac10c24b55ebbab6e1af7078643fd81.jpg`
   - Author/caption: "Vào ngày 21.10.2023 tại Trung tâm Hội nghị Quốc Gia - 57 Phạm Hùng, Hà Nội, IVY moda ra mắt thành công show diễn thứ 22 mang tên EXPRESS."
   - Title: "EXPRESS_FALL/WINTER 2023 FASHION SHOW - LỜI BÀY TỎ TỪ GIÁ TRỊ ĐÍCH THỰC"
   - Date: "23/10/2023"
3. Image: `f50407a8b2dc3e8dce84e5aabee3b688.jpg`
   - Author/caption: "Tối 21/10, Kỳ Duyên - Minh Triệu thu hút nhiều ánh nhìn khi xuất hiện trong show diễn thời trang Express 22 FW2023 của IVY moda"
   - Title: "Kỳ Duyên - Minh Triệu diện váy cúp ngực khoe vóc dáng gợi cảm tại EXPRESS 22 FW2023"
   - Date: "23/10/2023"

All hrefs: mock `#` (individual article pages are out of scope for this clone).

## Responsive Behavior
- **Desktop (1440px):** 3-column grid as described.
- **Tablet (768px):** 2-column grid.
- **Mobile (390px):** 1-column stack.
- **Breakpoint:** `sm:grid-cols-2 lg:grid-cols-3`.
