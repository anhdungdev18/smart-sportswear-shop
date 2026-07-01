# LookbookIntro Specification

## Overview
- **Target file:** `src/components/LookbookIntro.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/lookbook-desktop-full.png` (top section, above the product grid)
- **Interaction model:** static (no interactivity — a hero banner + title/description + 2 editorial images)
- **Props:**
```ts
interface LookbookIntroProps {
  bannerImage: string;
  eyebrow: string; // e.g. "NEW COLLECTION 2026"
  title: string; // e.g. "DAILY MOOD"
  description: string;
  editorialImages: [string, string]; // 2 side-by-side images
}
```

## DOM Structure
```
section.banner-look-book
  div (container) > img (full-width hero banner, same asymmetric-rounded-corner treatment as the homepage banner: rounded-tl-[80px] rounded-br-[80px])
section.section-info-lb
  div.main-info-lb (centered text block, max-width ~700px, mx-auto, text-center)
    h4 (eyebrow, small uppercase letter-spaced label)
    h3 (title, large bold)
    p (description paragraph)
  div.list-img-lb (flex row, 2 columns, gap)
    div.item-img-lb > img (× 2)
```

## Computed Styles (faithful approximation matching screenshot + reusing known site tokens)
```css
.banner-look-book { margin-bottom: 40px; }
.img-banner-look { border-top-left-radius: 80px; border-bottom-right-radius: 80px; overflow: hidden; }
.main-info-lb { text-align: center; max-width: 700px; margin: 0 auto 40px; }
.main-info-lb h4 { font-size: 14px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase; color: #6C6D70; margin-bottom: 8px; }
.main-info-lb h3 { font-size: 32px; font-weight: 700; letter-spacing: 2px; color: #221F20; margin-bottom: 16px; }
.main-info-lb p { font-size: 14px; line-height: 24px; color: #57585A; }
.list-img-lb { display: flex; gap: 24px; margin-bottom: 40px; }
.item-img-lb { flex: 1; }
.item-img-lb img { width: 100%; display: block; object-fit: cover; }
```

## Content (verbatim, real data for the "Daily Mood" lookbook)
- Banner image: `/images/ivymoda/banner/6a051c7c1a148911a0f04bb13704e9e4.webp` (already downloaded — same image as the homepage banner slide 1)
- Eyebrow: "NEW COLLECTION 2026"
- Title: "DAILY MOOD"
- Description: "Bộ sưu tập khơi nguồn cảm hứng từ những khoảnh khắc trà chiều thành thị yên bình, nơi thời gian như lắng đọng bên khung cửa gỗ để nhường chỗ cho sự thư thái. Trên tinh thần ấy, các nhà thiết kế đã lựa chọn ngôn ngữ tối giản nhưng đầy chiều sâu khi để hai gam màu Trắng - Đen kinh điển xuyên suốt các thiết kế, điểm xuyết thêm các họa tiết hoa lá thiên nhiên đơn sắc được vẽ nét mảnh vô cùng tinh tế, nhẹ nhàng mà không hề phô trương."
- Editorial images (2, already downloaded — use these local paths): `/images/ivymoda/lookbook/565c2865e6497f2b6a1310017af86f39.webp` and `/images/ivymoda/lookbook/164e491a614ae80c318fc4e3376b9ac5.webp`

## Responsive Behavior
- **Desktop (1440px):** as described, 2-column editorial image row.
- **Mobile (390px):** editorial images stack to 1 column (`flex-col md:flex-row`), text block padding reduces, title font-size drops to ~24px.
- **Breakpoint:** `md:` (768px).
