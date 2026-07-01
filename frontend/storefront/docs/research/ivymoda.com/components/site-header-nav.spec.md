# SiteHeaderNav Specification

## Overview
- **Target file:** `src/components/SiteHeader.tsx` (contains logo + `NavMenu` sub-component in the same file or `src/components/NavMenu.tsx` if cleaner)
- **Screenshots:** `docs/design-references/ivymoda.com/header-default.png`, `docs/design-references/ivymoda.com/header-megamenu-nu.png`
- **Interaction model:** hover-driven (desktop mega menus). No scroll-reactive behavior at all — header is `position:fixed` with identical styles at scrollY=0 and scrollY=600 (verified live).

## DOM Structure
```
header#header.site-header (position:fixed, top:0, z-index:20, bg:#FFF)
  div.container (height:80px, flex, align-items:center, justify-content:space-between; ::after = 1px hairline #E7E8E9 full-width centered at bottom)
    nav.main-menu
      div.mobile-menu (hamburger, 3 spans — mobile only, hidden ≥768px)
      ul.menu (6 top-level items, inline-block, margin-right:24px between, padding:15px 0 each)
        li > a (color:#221F20, font-weight:600, uppercase) — hover: color:#AC2F33
        li > ul.sub-menu (mega menu, see below) — one of 4 variants, or none for plain link
    div.site-brand (position:absolute, top:50%, left:50%, transform:translate(-50%,-50%) — centered over the whole header regardless of nav width)
      a > img (width:206px, height:66px) — logo at `/images/ivymoda/common/logo.png`
    [right-header cluster is a SEPARATE component — see header-actions.spec.md]

## Mega Menu — exact CSS
```css
.sub-menu {
  display: none !important; /* default hidden — NOT opacity-based */
  position: absolute; top: 55px; left: 15px;
  width: 100%; max-width: 100%;
  background: #FFF; border: 1px solid #E7E8E9;
  padding: 23px 24px; z-index: 10;
}
.main-menu .menu > li:hover > .sub-menu {
  display: flex !important;
  animation: fade_in_show 0.5s; /* @keyframes fade_in_show { from{opacity:0} to{opacity:1} } already added to globals.css */
}
.sub-menu.sub-menu-collection { width: max-content; left: 0; max-width: unset; }
.main-menu .menu > li:hover > .sub-menu.sub-menu-collection { top: 52px; }
.sub-menu.sub-menu-about { }
.main-menu .menu > li:hover > .sub-menu.sub-menu-about { display: block !important; }
.cat-sub-menu a, .item-list-submenu h3 { font-weight:600; font-size:14px; line-height:16px; color:#221F20; display:block; }
.cat-sub-menu a { margin-bottom: 24px; }
.sub-menu-collection .item-list-submenu { margin-right: 65px; }
.sub-menu-about li { display:block; }
.sub-menu-about li a { font-size:14px; line-height:16px; font-weight:600; color:#221F20; margin-bottom:24px; display:block; }
```
Implementation: use Tailwind `hidden group-hover:flex` on `li.group` won't animate; instead use a real CSS class in globals.css replicating the exact rule above (`.sub-menu{display:none} li:hover>.sub-menu{display:flex;animation:...}`) since Tailwind's arbitrary variants can express this via `group-hover:flex` + `animate-[fade_in_show_0.5s]` utility — either approach is acceptable as long as computed behavior matches (hidden by default, flex+fade on parent hover, no JS).

## Content — Nav Items (verbatim, in order)

1. **NỮ** → `/danh-muc/nu` — variant: category mega menu
   - Quick links: "ALL ITEMS" → `/danh-muc/nu`; "NEW ARRIVAL " → `/danh-muc/hang-nu-moi-ve`; "SALE 40% ++ | CHỈ CÓ TẠI ONLINE" (red, `color:#FF0000`) → `/danh-muc/online-exclusive-210525`
   - Groups (heading + links):
     - ÁO (`/danh-muc/ao-nu`): Áo sơ mi (`/danh-muc/ao-so-mi-nu`), Áo thun (`/danh-muc/ao-thun-nu`), Áo croptop (`/danh-muc/ao-croptop`), Áo len (`/danh-muc/ao-len-nu`)
     - ÁO KHOÁC (`/danh-muc/ao-khoac-nu-dep`): Áo dạ/ măng tô (`/danh-muc/ao-khoac-da-nu`), Áo vest/ blazer (`/danh-muc/ao-vest-nu`), Áo phao (`/danh-muc/ao-phao-nu`), Áo GIle (`/danh-muc/ao-gile-nu`)
     - SET BỘ (`/danh-muc/bst-set-bo-nu`): Set bộ công sở (`/danh-muc/bst-set-bo-vest-nu`), Set bộ co-ords (`/danh-muc/bst-set-bo-kieu-nu`), Set bộ thun/ len (`/danh-muc/bst-set-bo-thun-nu`)
     - QUẦN & JUMPSUIT (`/danh-muc/quan-nu`): Quần dài (`/danh-muc/quan-dai-nu`), Quần jeans (`/danh-muc/quan-jean-nu`), Quần lửng/ short (`/danh-muc/quan-lung-short-nu`), Jumpsuit (`/danh-muc/jumpsuit`)
     - CHÂN VÁY (`/danh-muc/bst-chan-vay-nu`): Chân váy bút chì (`/danh-muc/bst-cv-but-chi-nu`), Chân váy chữ A (`/danh-muc/chan-vay-chu-A`), Chân váy jeans (`/danh-muc/bst-cv-jeans-nu`)
     - ĐẦM/ ÁO DÀI (`/danh-muc/dam-nu`): Đầm công sở (`/danh-muc/dam`), Đầm voan hoa/ maxi (`/danh-muc/dam-maxi`), Đầm thun (`/danh-muc/dam-thun-nu`)
     - SENORA (`/danh-muc/dam-da-hoi-senora-2410`): Senora - Đầm dạ hội (`/danh-muc/dam-da-hoi-ng-2410`)

2. **NAM** → `/danh-muc/nam` — variant: category mega menu
   - Quick links: "ALL ITEMS" (red) → `/danh-muc/nam`; "NEW ARRIVAL" → `/danh-muc/hang-nam-moi-ve`
   - Groups: ÁO (`/danh-muc/ao-nam`): Áo thun, Áo polo, Áo sơ mi, Áo len, Áo khoác (hrefs: `/danh-muc/ao-thun-nam`, `/danh-muc/ao-polo-nam`, `/danh-muc/ao-so-mi-nam`, `/danh-muc/ao-len-nam`, `/danh-muc/ao-khoac-nam`) · QUẦN NAM (`/danh-muc/quan-nam`): Quần dài (`/danh-muc/quan-dai-nam`), Quần jeans (`/danh-muc/quan-jean-nam`), Quần lửng/short (`/danh-muc/quan-lung-nam`)

3. **HÈ SALE CHẠM ĐỈNH 70%** → `/danh-muc/sale-all-70-0626` — variant: category mega menu
   - Quick links (both red): "OUTLET đồ NỮ từ 150k" → `/danh-muc/outlet-ivy-0126`; "OUTLET đồ NAM từ 150k" → `/danh-muc/outlet-mtg-0126`
   - Group: GIÁ MỚI | SALE ALL 70% (`/danh-muc/sale-all-70-0626`): ÁO (`/danh-muc/sale-all-70-ao-0626`), ĐẦM (`/danh-muc/sale-all-70-dam-0626`), QUẦN (`/danh-muc/sale-all-70-quan-0626`), CHÂN VÁY (`/danh-muc/sale-all-70-cv-0626`)

4. **Bộ sưu tập** → variant: collection mega menu (`sub-menu-collection`, single flat list, no quick links/groups)
   - DAILY MOOD (`/lookbook/daily-mood-226`), LADY GRACE (`/lookbook/lady-grace-225`), THE CLASSY | BST ĐỘC QUYỀN ONLINE (`/lookbook/the-classy-bst-doc-quyen-online-224`), MAROON MUSE (`/lookbook/maroon-muse-223`), BLUE HORIZON (`/lookbook/blue-horizon-222`), BLOOMING BLUSH (`/lookbook/blooming-blush-221`), SUN-KISSED (`/lookbook/sun-kissed-220`), A SIP OF SPRING (`/lookbook/a-sip-of-spring-219`), HER LUXE (`/lookbook/her-luxe-217`)

5. **LIFESTYLE** → `/tin-tuc/tin-chinh` — variant: plain link, no dropdown

6. **Về Chúng Tôi** → variant: about mega menu (`sub-menu-about`, vertical list, `display:block` on hover instead of flex)
   - Về IVY moda (`/about/gioi-thieu`), Fashion Show (`/tin-tuc/fashion-show`), Hoạt động cộng đồng (`/tin-tuc/hoat-dong-cong-dong`)

Use `NavCategoryMenu[]` and `NavSubItemGroup[]` from `src/types/ivy.ts` for the data model. Since this is a static homepage clone, hardcode this data as a local array in the component file (or a co-located `data.ts`) — no CMS/backend needed.

## Assets
- Logo: `public/images/ivymoda/common/logo.png` (206×66 at 2x retina source; render at `width={206} height={66}`)

## Responsive Behavior
- **Desktop (≥1200px):** as described above, container width 1140–1800px depending on breakpoint (1200:1140px, 1365:1300px, 1440:1380px, 1600:1460px, 1860:1800px). Use `1380px` as the reference max-width for our 1440px design target.
- **Mobile (<768px):** `.menu { display:none }`, `.mobile-menu` (hamburger) becomes visible instead, header height drops to `64px` (`main{padding-top:64px}`). Build the hamburger as a static visual for now (opens nothing) — full mobile drawer menu is out of scope for this pass; note it as a TODO comment if a click handler stub is trivial to add, but do not build a full slide-out drawer unless trivial.
- **Breakpoint:** 768px is the primary mobile switch point.

## States & Behaviors
### Mega menu reveal
- **Trigger:** `:hover` on `.main-menu .menu > li` (desktop only, mouse enter — NOT click, NOT scroll)
- **State A (closed):** `display:none`
- **State B (open):** `display:flex` (or `display:block` for the About variant), fade-in via `animation: fade_in_show 0.5s`
- **Implementation:** CSS-only, no JS/state needed (use Tailwind `group` + `group-hover:flex` pattern with a manual `@keyframes fade_in_show` utility, or plain CSS classes)

### Top-level link hover
- **Element:** `.main-menu .menu > li > a`
- `color: #221F20` → hover `color: #AC2F33`, treat as `transition-colors duration-200`
