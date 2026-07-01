# Footer Specification

## Overview
- **Target file:** `src/components/Footer.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/section-footer.png`
- **Interaction model:** static, standard link hovers only. Email subscribe form is a UI-only mock (`onSubmit={(e) => e.preventDefault()}`, no backend — out of scope per project defaults).

## DOM Structure
```
div.site-bottom (border-top:1px solid #D1D2D4 from .site-main)
  div.container
    div.main-footer (flex, 3 columns)
      div.left-footer
        div.logo-footer > img (logo-footer.png)
        a.dmca-badge > img (dmca.png) [decorative external badge — include as static image, link out with rel=nofollow]
        div.logo-conthuong > img (img-congthuong.png) [Bộ Công Thương registration badge — same treatment]
        ul.list-social (3 icons: Facebook, Google, Instagram)
      div.center-footer (flex, 3 sub-columns)
        div.item-center-ft (title "Giới thiệu" + 3 links)
        div.item-center-ft (title "Dịch vụ khách hàng" + 7 links)
        div.item-center-ft (title "Liên hệ" + 5 links)
      div.right-footer
        form#frm_subscribe (email input + submit button)
        div.info-right-ft (title "Download App" + App Store / Google Play badge links)
footer#footer.site-footer
  div.container > div.coppy-right ("©IVYmoda All rights reserved")
```

## Computed Styles
```css
.title-footer { font-weight:600; font-size:24px; color:#221F20; }
.center-footer a, .footer a { color:#57585A; font-size:14px; }
.center-footer a:hover { color:#221F20; }
```
Layout: 3-column flex row at desktop (`left-footer` ~25%, `center-footer` ~50% split into 3 sub-columns, `right-footer` ~25%). Footer background is white, matching page background; a 1px `#D1D2D4` divider sits above the whole footer block (`.site-main { border-bottom: 1px solid #D1D2D4; }`).

## Text Content (verbatim)

**Left column:**
- Logo: `public/images/ivymoda/common/logo-footer.png`
- DMCA badge image: `public/images/ivymoda/common/dmca.png`, links to `https://www.dmca.com/Protection/Status.aspx?ID=0cfdeac4-6e7f-4fca-941f-57a0a0962777` (`rel=nofollow`, `target=_blank`)
- Bộ Công Thương badge: `public/images/ivymoda/common/img-congthuong.png`, links to `http://online.gov.vn/Home/WebDetails/36596` (`rel=nofollow`, `target=_blank`)
- Social icons (all `target=_blank rel=nofollow`): Facebook (`ic_fb.svg` → `https://www.facebook.com/thoitrangivymoda/`), Google (`ic_gg.svg` → `https://ivymoda.com/`), Instagram (`ic_instagram.svg`, height 28px → `https://www.instagram.com/ivy_moda/`)

**Center column 1 — "Giới thiệu":**
- Về IVY moda → `/about/gioi-thieu`
- Tuyển dụng → `https://tuyendung.ivy.com.vn` (external, `target=_blank rel=nofollow`)
- Hệ thống cửa hàng → `/page/cuahang`

**Center column 2 — "Dịch vụ khách hàng":**
- Chính sách điều khoản → `/about/chinhsach-dieukhoan`
- Hướng dẫn mua hàng → `/about/huong-dan-mua-hang`
- Chính sách thanh toán → `/about/chinh-sach-thanh-toan`
- Chính sách đổi trả → `/about/chinh-sach-doi-tra`
- Chính sách bảo hành → `/about/chinh-sach-bao-hanh`
- Chính sách thẻ thành viên → `/about/chinh-sach-the-thanh-vien`
- Q&A → `/about/qa`

**Center column 3 — "Liên hệ":**
- Hotline → `tel:02466623434`
- Email → `mailto:saleadmin@ivy.com.vn`
- Live Chat → mock `href="#"` (no live chat backend)
- Messenger → `http://messenger.com/t/thoitrangivymoda` (`target=_blank rel=nofollow`)
- Liên hệ → `/lien-he`

**Right column:**
- Title: "Nhận thông tin các chương trình của IVY moda"
- Email input placeholder: "Nhập địa chỉ email"
- Submit button label: "Đăng ký"
- "Download App" title + App Store badge (`appstore.png` → mock `#`) + Google Play badge (`googleplay.png` → mock `#`)

**Copyright bar:** "©IVYmoda All rights reserved"

All prefixed hrefs (`/about/...`, `/danh-muc/...`, `/page/...`) resolve against `https://ivymoda.com` — use them as full external links or relative mock `#` links, whichever is simpler; since this is a homepage-only clone, relative internal links can point to `#` if the target page doesn't exist in this project, but external ones (Facebook, Instagram, Messenger, DMCA, government registry) should keep their real absolute URLs since they're genuinely external.

## Responsive Behavior
- **Desktop (≥1200px):** 3-column flex row as described.
- **Mobile (<768px):** columns stack vertically (`left-footer`, then `center-footer`'s 3 sub-columns, then `right-footer`), full width.
- **Breakpoint:** 768px.
