# ArticleContent Specification

## Overview
- **Target file:** `src/components/ArticleContent.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/article-detail-desktop-full.png`
- **Interaction model:** static (a rich-content article body — real text/image blocks in DOM order, no interactivity beyond plain links)
- **Props:**
```ts
interface ArticleContentBlock {
  type: "text" | "image";
  text?: string; // for type "text"
  src?: string; // for type "image", local path
}
interface ArticleContentProps {
  title: string;
  date: string;
  blocks: ArticleContentBlock[];
  tags: { label: string; href: string }[];
}
```

## DOM Structure
```
article
  header (news__title)
    h1 (article title, uppercase, bold, centered or left — match screenshot: left-aligned)
    div.time (date, small gray, below title)
  div (news__content — render `blocks` in order: type "text" → <p>, type "image" → <img> centered, full content-column width)
  div (tags + socials row, flex justify-between, border-top divider, padding-top)
    div.tags (flex-wrap gap-2) — pill-style tag links
    div.socials (flex gap-3) — 5 circular icon buttons (Facebook, Google, Twitter/X, "V" unknown icon [likely Viber/Zalo — use a generic Share icon], YouTube)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.news__title h1 { font-size: 26px; font-weight: 700; color: #221F20; line-height: 34px; margin-bottom: 12px; }
.news__title .time { font-size: 13px; color: #A8A9AD; margin-bottom: 24px; }
.news__content p { font-size: 15px; line-height: 26px; color: #57585A; margin-bottom: 16px; }
.news__content img { width: 100%; display: block; margin: 16px 0; border-radius: 4px; }
.news__content p[centered-caption] { text-align: center; font-size: 13px; color: #A8A9AD; font-style: italic; } /* the live site centers most caption paragraphs — apply text-center + text-sm + text-ivy-text-muted to short caption-like text blocks (a heuristic: text under ~120 chars that follows an image) */
.tags a.tag__item { display: inline-block; padding: 6px 14px; border: 1px solid #E7E8E9; border-radius: 999px; font-size: 12px; color: #57585A; }
.tags a.tag__item:hover { border-color: #221F20; color: #221F20; }
.socials a.social__item { width: 32px; height: 32px; border-radius: 50%; background: #F7F8F9; display: flex; align-items: center; justify-content: center; }
```

## Text Content (verbatim, real content for "ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW")
- Title: "ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW"
- Date: "25/10/2023"
- Content blocks in exact order (type, then content):
  1. text: "Bên cạnh những hình ảnh trên sàn catwalk, khoảnh khắc tại hậu trường là nơi thể hiện sống động nhất tinh thần cống hiến hết mình của toàn bộ đội ngũ ekip thực hiện."
  2. text: "Cùng ghé thăm \"sau ánh hào quang\" của show diễn Express và trải nghiệm sự chuyên nghiệp hoàn hảo của những người mẫu cũng như ekip chụp hình đã tạo nên những tác phẩm thời trang vô cùng đẳng cấp!"
  3. image: `/images/ivymoda/article/36ac628758c67e487555251e9b8558b4.jpg`
  4. text: "Sự chỉn chu, cẩn thận được thể hiện trong từng khâu chuẩn bị."
  5. image: `/images/ivymoda/article/620f0349a0b3afa079fe6db482c7a97a.jpg`
  6. text: "Outfits lên kệ đầy đủ, ngăn nắp chuẩn bị cho những màn xuất hiện nảy lửa trên sàn Runway."
  7. image: `/images/ivymoda/article/3f5a54bcb0a5a093f27cfe22e0ded7c7.jpg`
  8. text: "Siêu mẫu Minh Triệu bên cạnh đội ngũ makeup chuyên nghiệp."
  9. image: `/images/ivymoda/article/3e42bbcce0a9d3ffc1cbca5e6ea570d9.jpg`
  10. image: `/images/ivymoda/article/ff98885c6855b2cf1f2ca7a088f89119.jpg`
  11. text: "Dàn model đắt giá và tài năng của IVY moda fashion show chăm chút diện mạo trước khi xuất hiện."
  12. image: `/images/ivymoda/article/e410f84d93796e59d903150b8b47499a.jpg`
  13. image: `/images/ivymoda/article/444aded901552aaef95be4b7107e9303.jpg`
  14. text: "Sự hỗ trợ nhiệt tình đến từ đội ngũ Ekip IVY moda."
  15. image: `/images/ivymoda/article/5ec995f5f0ddebda5c231225331c8202.jpg`
  16. text: "Những khoảnh khắc đáng nhớ được ghi lại trong hậu trường Express 22 FW2023."
  17. image: `/images/ivymoda/article/6b425ea03e2d138db56c61d09f81c92e.jpg`
  18. image: `/images/ivymoda/article/dca1c2344a84fb02b2f98050269b532f.jpg`
  19. image: `/images/ivymoda/article/0142aa10c737d5369b8ebe058ce73b61.jpg`
  20. image: `/images/ivymoda/article/6f33e64f346bc9f9ec8299e59293c857.jpg`
  21. image: `/images/ivymoda/article/9e2926c0190b4ba3760d2861f9c89345.jpg`
  22. image: `/images/ivymoda/article/db67e715bbe17999c15513a5bb5fd0f6.jpg`
  23. image: `/images/ivymoda/article/522322a61672d0db9de8873e82bda97d.jpg`
  24. text: "BST EXPRESS đã có mặt trên tất cả hệ thống showroom IVY moda toàn quốc. Hãy đến để chạm và tự cảm nhận sự cao cấp qua từng thiết kế IVY moda gửi tới nàng nhé!"
- Tags (mock hrefs, real category links): "áo sơ mi nam" → `https://ivymoda.com/danh-muc/ao-so-mi-nam`, "Quần jeans nữ" → `https://ivymoda.com/danh-muc/quan-jean-nu`, "Đầm" → `https://ivymoda.com/danh-muc/dam`, "Quần bé gái" → `https://ivymoda.com/danh-muc/quan-be-gai`, "Quần bé trai" → `https://ivymoda.com/danh-muc/quan-be-trai`
- Social icons: 5 buttons (Facebook, Google, Twitter/X — use Lucide `Facebook`... wait, no brand icons in lucide-react; use generic `Share2` for all 5 as a faithful simplification, OR — better — since these are just icon-only share buttons, use `Share2` from lucide-react for all 5 identically-styled circular buttons since there is no reliable brand-icon set available in this project). All 5 point to `href="#"` (mock, no real share integration).

## Responsive Behavior
- **Desktop (≥1024px):** article content column is the center of a 3-column layout (paired with `ArticleCategorySidebar` on the left and `ArticleRelatedSidebar` on the right).
- **Mobile (<1024px):** full width, images remain full-bleed within the padded content column.
- **Breakpoint:** `lg:` (1024px).
