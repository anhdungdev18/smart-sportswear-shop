# FeaturedStoryCard Specification

## Overview
- **Target file:** `src/components/FeaturedStoryCard.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/news-desktop-full.png` (two large cards below the LifestyleHeader)
- **Interaction model:** static card with a link ("XEM NGAY" / "view now")
- **Props:**
```ts
interface FeaturedStoryCardProps {
  image: string;
  eyebrow: string; // "STORY"
  title: string;
  excerpt: string;
  date: string;
  href: string;
  reverse?: boolean; // true = image on the left, text on the right (2nd instance)
}
```

## DOM Structure
```
div (root, rounded card, flex row — order reversed via `reverse` prop, border, asymmetric rounded corner like other IVY moda banner cards)
  div.image (flex-1, image fills it, object-cover, full height of the row)
  div.content (flex-1, padding, flex-col justify-center)
    h3 (eyebrow, small uppercase gray label) "STORY"
    h2 > a (title, large bold, links to the article)
    p (excerpt paragraph, muted gray)
    div.time (date, small gray)
    div.action > a.btn.btn-link "XEM NGAY" (underlined text link)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.root { border: 1px solid #E7E8E9; border-radius: 0 80px 0 80px; overflow: hidden; display: flex; margin-bottom: 24px; min-height: 320px; } /* reverse prop flips flex-direction: row-reverse */
.content { padding: 40px; display: flex; flex-direction: column; justify-content: center; }
h3 /* eyebrow */ { font-size: 12px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase; color: #6C6D70; margin-bottom: 12px; }
h2 a { font-size: 22px; font-weight: 700; color: #221F20; line-height: 30px; margin-bottom: 16px; display: block; }
p { font-size: 14px; line-height: 22px; color: #57585A; margin-bottom: 16px; }
.time { font-size: 13px; color: #A8A9AD; margin-bottom: 16px; }
.action a { font-size: 13px; font-weight: 600; letter-spacing: 1px; text-decoration: underline; color: #221F20; }
```

## Content — 2 real instances to render

### Instance 1 (image right, `reverse=false`)
- Image: `/images/ivymoda/news/47b630796bec23aa195d7a59d1597231.jpg`
- Eyebrow: "STORY"
- Title: "ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW"
- Excerpt: "Bên cạnh những hình ảnh trên sàn catwalk, khoảnh khắc tại hậu trường là nơi thể hiện sống động nhất tinh thần cống hiến hết mình của toàn bộ đội ngũ ekip thực hiện."
- Date: "25/10/2023"
- Href: mock `#` (article page out of scope)

### Instance 2 (image left, `reverse=true`)
- Image: `/images/ivymoda/news/5aaf578c14a70d76a45c36de9e77a037.jpg`
- Eyebrow: "STORY"
- Title: "QUIETLUXURY: KHI SỰ KHIÊM NHƯỜNG ẨN CHỨA NÉT CAO SANG"
- Excerpt: "Quietluxury của IVY moda mang đến vẻ đẹp riêng biệt một cách thầm lặng. Giống như người phụ nữ an tĩnh và sâu sắc, họ vẫn luôn dùng trái tim yêu để đối diện với khó khăn, theo đuổi lối sống tinh tế, tao nhã và chẳng cần chưng diện những họa tiết logo để khẳng định mình là ai."
- Date: "19/10/2023"
- Href: mock `#`

## Responsive Behavior
- **Desktop (1440px):** side-by-side flex row as described (~50/50 split).
- **Mobile (390px):** stack to `flex-col` (image on top, text below) regardless of the `reverse` prop, full width.
- **Breakpoint:** `md:` (768px).
