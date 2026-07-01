# ArticleRelatedSidebar Specification

## Overview
- **Target file:** `src/components/ArticleRelatedSidebar.tsx`
- **Interaction model:** static (plain links + one promo image link)
- **Props:** none — hardcode the 4 real "latest news" items + 1 promo banner below.

## DOM Structure
```
aside (sticky on desktop: lg:sticky lg:top-24, flex flex-col gap-8)
  div (list-last-news)
    div ("Tin mới nhất" title)
    ul (flex flex-col gap-4)
      li > a (× 4: small thumbnail image + headline + date)
  section (promo banner card, asymmetric rounded corner like other IVY moda banners)
    a > img (full-width promo image)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.aside-news-title { font-weight: 600; font-size: 18px; color: #221F20; margin-bottom: 20px; }
.last-news__item a { display: flex; gap: 12px; }
.last-news__item .img { width: 80px; height: 80px; flex-shrink: 0; border-radius: 8px; overflow: hidden; }
.last-news__item .desc h4 { font-size: 13px; font-weight: 600; color: #221F20; line-height: 18px; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.last-news__item .desc p { font-size: 12px; color: #A8A9AD; }
.promo-banner { border-radius: 0 24px 0 24px; overflow: hidden; }
```

## Text Content (verbatim, real content — images already downloaded to `public/images/ivymoda/article/`)

**"Tin mới nhất" items:**
1. Image: `4727bbbb0a8ba53e0b97d36c35ea5d91.webp` — "Top 9 mùi hương nước hoa Dior cho nữ thơm nhất" — "11/07/2025" — href `https://ivymoda.com/tin-tuc/bai-viet/top-9-mui-huong-nuoc-hoa-dior-cho-nu-thom-nhat-1547`
2. Image: `54c15ce9d63542e090c87910a7deaf31.webp` — "Tìm hiểu về thương hiệu thời trang xa xỉ Dior và các dòng sản phẩm nổi bật của thương hiệu" — "10/07/2025" — href `https://ivymoda.com/tin-tuc/bai-viet/tim-hieu-ve-thuong-hieu-thoi-trang-xa-xi-dior-va-cac-dong-san-pham-noi-bat-cua-thuong-hieu-1546`
3. Image: `5d6c83f3593c42a63be5e000512187f9.webp` — "Tìm Hiểu 20+ Đại Sứ Thương Hiệu Toàn Cầu Của Dior" — "09/07/2025" — href `https://ivymoda.com/tin-tuc/bai-viet/tim-hieu-20-dai-su-thuong-hieu-toan-cau-cua-dior-1545`
4. Image: `3d5f89e07422bb46124c41bbd920e9c3.webp` — "Lịch Sử Hình Thành Và Hành Trình Phát Triển Thương Hiệu Nước Hoa Dior" — "09/07/2025" — href `https://ivymoda.com/tin-tuc/bai-viet/lich-su-hinh-thanh-va-hanh-trinh-phat-trien-thuong-hieu-nuoc-hoa-dior-1544`

All 4 images are at `/images/ivymoda/article/<filename>`.

**Promo banner:** image `/images/ivymoda/brand/3f9cd315b0280fcc5726e7c1816a233d.webp` → href `https://ivymoda.com/danh-muc/sale-all-70-0626`

## Responsive Behavior
- **Desktop (≥1024px):** right column of the 3-column article layout, sticky.
- **Mobile (<1024px):** stacks below the article content, full width, not sticky.
- **Breakpoint:** `lg:` (1024px).
