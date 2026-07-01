import Image from "next/image";
import Link from "next/link";
import { cn } from "@/lib/utils";
import type { NavCategoryMenu, NavSubItemGroup } from "@/types/ivy";

const SITE_ORIGIN = "";

const NAV_ITEMS: NavCategoryMenu[] = [
  {
    label: "NỮ",
    href: `${SITE_ORIGIN}/danh-muc/nu`,
    variant: "category",
    quickLinks: [
      { label: "ALL ITEMS", href: `${SITE_ORIGIN}/danh-muc/nu` },
      { label: "NEW ARRIVAL ", href: `${SITE_ORIGIN}/danh-muc/hang-nu-moi-ve` },
      {
        label: "SALE 40% ++ | CHỈ CÓ TẠI ONLINE",
        href: `${SITE_ORIGIN}/danh-muc/online-exclusive-210525`,
        highlight: true,
      },
    ],
    groups: [
      {
        heading: "ÁO",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-nu`,
        links: [
          { label: "Áo sơ mi", href: `${SITE_ORIGIN}/danh-muc/ao-so-mi-nu` },
          { label: "Áo thun", href: `${SITE_ORIGIN}/danh-muc/ao-thun-nu` },
          { label: "Áo croptop", href: `${SITE_ORIGIN}/danh-muc/ao-croptop` },
          { label: "Áo len", href: `${SITE_ORIGIN}/danh-muc/ao-len-nu` },
        ],
      },
      {
        heading: "ÁO KHOÁC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-khoac-nu-dep`,
        links: [
          { label: "Áo dạ/ măng tô", href: `${SITE_ORIGIN}/danh-muc/ao-khoac-da-nu` },
          { label: "Áo vest/ blazer", href: `${SITE_ORIGIN}/danh-muc/ao-vest-nu` },
          { label: "Áo phao", href: `${SITE_ORIGIN}/danh-muc/ao-phao-nu` },
          { label: "Áo GIle", href: `${SITE_ORIGIN}/danh-muc/ao-gile-nu` },
        ],
      },
      {
        heading: "SET BỘ",
        headingHref: `${SITE_ORIGIN}/danh-muc/bst-set-bo-nu`,
        links: [
          { label: "Set bộ công sở", href: `${SITE_ORIGIN}/danh-muc/bst-set-bo-vest-nu` },
          { label: "Set bộ co-ords", href: `${SITE_ORIGIN}/danh-muc/bst-set-bo-kieu-nu` },
          { label: "Set bộ thun/ len", href: `${SITE_ORIGIN}/danh-muc/bst-set-bo-thun-nu` },
        ],
      },
      {
        heading: "QUẦN & JUMPSUIT",
        headingHref: `${SITE_ORIGIN}/danh-muc/quan-nu`,
        links: [
          { label: "Quần dài", href: `${SITE_ORIGIN}/danh-muc/quan-dai-nu` },
          { label: "Quần jeans", href: `${SITE_ORIGIN}/danh-muc/quan-jean-nu` },
          { label: "Quần lửng/ short", href: `${SITE_ORIGIN}/danh-muc/quan-lung-short-nu` },
          { label: "Jumpsuit", href: `${SITE_ORIGIN}/danh-muc/jumpsuit` },
        ],
      },
      {
        heading: "CHÂN VÁY",
        headingHref: `${SITE_ORIGIN}/danh-muc/bst-chan-vay-nu`,
        links: [
          { label: "Chân váy bút chì", href: `${SITE_ORIGIN}/danh-muc/bst-cv-but-chi-nu` },
          { label: "Chân váy chữ A", href: `${SITE_ORIGIN}/danh-muc/chan-vay-chu-A` },
          { label: "Chân váy jeans", href: `${SITE_ORIGIN}/danh-muc/bst-cv-jeans-nu` },
        ],
      },
      {
        heading: "ĐẦM/ ÁO DÀI",
        headingHref: `${SITE_ORIGIN}/danh-muc/dam-nu`,
        links: [
          { label: "Đầm công sở", href: `${SITE_ORIGIN}/danh-muc/dam` },
          { label: "Đầm voan hoa/ maxi", href: `${SITE_ORIGIN}/danh-muc/dam-maxi` },
          { label: "Đầm thun", href: `${SITE_ORIGIN}/danh-muc/dam-thun-nu` },
        ],
      },
      {
        heading: "SENORA",
        headingHref: `${SITE_ORIGIN}/danh-muc/dam-da-hoi-senora-2410`,
        links: [{ label: "Senora - Đầm dạ hội", href: `${SITE_ORIGIN}/danh-muc/dam-da-hoi-ng-2410` }],
      },
    ],
  },
  {
    label: "NAM",
    href: `${SITE_ORIGIN}/danh-muc/nam`,
    variant: "category",
    quickLinks: [
      { label: "ALL ITEMS", href: `${SITE_ORIGIN}/danh-muc/nam`, highlight: true },
      { label: "NEW ARRIVAL", href: `${SITE_ORIGIN}/danh-muc/hang-nam-moi-ve` },
    ],
    groups: [
      {
        heading: "ÁO",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-nam`,
        links: [
          { label: "Áo thun", href: `${SITE_ORIGIN}/danh-muc/ao-thun-nam` },
          { label: "Áo polo", href: `${SITE_ORIGIN}/danh-muc/ao-polo-nam` },
          { label: "Áo sơ mi", href: `${SITE_ORIGIN}/danh-muc/ao-so-mi-nam` },
          { label: "Áo len", href: `${SITE_ORIGIN}/danh-muc/ao-len-nam` },
          { label: "Áo khoác", href: `${SITE_ORIGIN}/danh-muc/ao-khoac-nam` },
        ],
      },
      {
        heading: "QUẦN NAM",
        headingHref: `${SITE_ORIGIN}/danh-muc/quan-nam`,
        links: [
          { label: "Quần dài", href: `${SITE_ORIGIN}/danh-muc/quan-dai-nam` },
          { label: "Quần jeans", href: `${SITE_ORIGIN}/danh-muc/quan-jean-nam` },
          { label: "Quần lửng/short", href: `${SITE_ORIGIN}/danh-muc/quan-lung-nam` },
        ],
      },
    ],
  },
  {
    label: "HÈ SALE CHẠM ĐỈNH 70%",
    href: `${SITE_ORIGIN}/danh-muc/sale-all-70-0626`,
    variant: "category",
    highlight: true,
    quickLinks: [
      { label: "OUTLET đồ NỮ từ 150k", href: `${SITE_ORIGIN}/danh-muc/outlet-ivy-0126`, highlight: true },
      { label: "OUTLET đồ NAM từ 150k", href: `${SITE_ORIGIN}/danh-muc/outlet-mtg-0126`, highlight: true },
    ],
    groups: [
      {
        heading: "GIÁ MỚI | SALE ALL 70%",
        headingHref: `${SITE_ORIGIN}/danh-muc/sale-all-70-0626`,
        links: [
          { label: "ÁO", href: `${SITE_ORIGIN}/danh-muc/sale-all-70-ao-0626` },
          { label: "ĐẦM", href: `${SITE_ORIGIN}/danh-muc/sale-all-70-dam-0626` },
          { label: "QUẦN", href: `${SITE_ORIGIN}/danh-muc/sale-all-70-quan-0626` },
          { label: "CHÂN VÁY", href: `${SITE_ORIGIN}/danh-muc/sale-all-70-cv-0626` },
        ],
      },
    ],
  },
  {
    label: "Bộ sưu tập",
    href: "#",
    variant: "collection",
    groups: [
      {
        heading: "",
        headingHref: "",
        links: [
          { label: "DAILY MOOD", href: `${SITE_ORIGIN}/lookbook/daily-mood-226` },
          { label: "LADY GRACE", href: `${SITE_ORIGIN}/lookbook/lady-grace-225` },
          {
            label: "THE CLASSY | BST ĐỘC QUYỀN ONLINE",
            href: `${SITE_ORIGIN}/lookbook/the-classy-bst-doc-quyen-online-224`,
          },
          { label: "MAROON MUSE", href: `${SITE_ORIGIN}/lookbook/maroon-muse-223` },
          { label: "BLUE HORIZON", href: `${SITE_ORIGIN}/lookbook/blue-horizon-222` },
          { label: "BLOOMING BLUSH", href: `${SITE_ORIGIN}/lookbook/blooming-blush-221` },
          { label: "SUN-KISSED", href: `${SITE_ORIGIN}/lookbook/sun-kissed-220` },
          { label: "A SIP OF SPRING", href: `${SITE_ORIGIN}/lookbook/a-sip-of-spring-219` },
          { label: "HER LUXE", href: `${SITE_ORIGIN}/lookbook/her-luxe-217` },
        ],
      },
    ],
  },
  {
    label: "LIFESTYLE",
    href: `${SITE_ORIGIN}/tin-tuc/tin-chinh`,
    variant: "plain",
  },
  {
    label: "Về Chúng Tôi",
    href: "#",
    variant: "about",
    groups: [
      {
        heading: "",
        headingHref: "",
        links: [
          { label: "Về IVY moda", href: `${SITE_ORIGIN}/about/gioi-thieu` },
          { label: "Fashion Show", href: `${SITE_ORIGIN}/tin-tuc/fashion-show` },
          { label: "Hoạt động cộng đồng", href: `${SITE_ORIGIN}/tin-tuc/hoat-dong-cong-dong` },
        ],
      },
    ],
  },
];

function CategorySubMenu({ item }: { item: NavCategoryMenu }) {
  return (
    <ul
      className={cn(
        "sub-menu absolute left-[15px] top-[55px] z-10 hidden w-full max-w-full items-start",
        "border border-ivy-hairline bg-white p-[23px_24px]",
        "group-hover:flex group-hover:animate-[fade_in_show_0.5s_ease-in-out]",
      )}
    >
      {item.quickLinks && item.quickLinks.length > 0 && (
        <li className="cat-sub-menu mr-16 shrink-0">
          {item.quickLinks.map((quickLink) => (
            <Link
              key={quickLink.label}
              href={quickLink.href}
              className="mb-6 block text-sm leading-4 font-semibold text-ivy-dark last:mb-0"
              style={quickLink.highlight ? { color: "#FF0000" } : undefined}
            >
              {quickLink.label}
            </Link>
          ))}
        </li>
      )}
      {item.groups?.map((group: NavSubItemGroup) => (
        <li key={group.heading} className="item-list-submenu mr-[65px] shrink-0 last:mr-0">
          <Link href={group.headingHref} className="mb-6 block text-sm leading-4 font-semibold text-ivy-dark">
            {group.heading}
          </Link>
          <ul>
            {group.links.map((link) => (
              <li key={link.label}>
                <Link href={link.href} className="mb-6 block text-sm leading-4 text-ivy-text last:mb-0">
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>
        </li>
      ))}
    </ul>
  );
}

function CollectionSubMenu({ item }: { item: NavCategoryMenu }) {
  const group = item.groups?.[0];
  if (!group) return null;

  return (
    <ul
      className={cn(
        "sub-menu sub-menu-collection absolute left-0 top-[52px] z-10 hidden w-max max-w-none items-start",
        "border border-ivy-hairline bg-white p-[23px_24px]",
        "group-hover:flex group-hover:animate-[fade_in_show_0.5s_ease-in-out]",
      )}
    >
      <li className="item-list-submenu mr-[65px] shrink-0 last:mr-0">
        <ul>
          {group.links.map((link) => (
            <li key={link.label}>
              <Link href={link.href} className="mb-6 block text-sm leading-4 font-semibold text-ivy-dark last:mb-0 whitespace-nowrap">
                {link.label}
              </Link>
            </li>
          ))}
        </ul>
      </li>
    </ul>
  );
}

function AboutSubMenu({ item }: { item: NavCategoryMenu }) {
  const group = item.groups?.[0];
  if (!group) return null;

  return (
    <ul
      className={cn(
        "sub-menu sub-menu-about absolute left-[15px] top-[55px] z-10 hidden w-full max-w-full",
        "border border-ivy-hairline bg-white p-[23px_24px]",
        "group-hover:block group-hover:animate-[fade_in_show_0.5s_ease-in-out]",
      )}
    >
      {group.links.map((link) => (
        <li key={link.label} className="block">
          <Link href={link.href} className="mb-6 block text-sm leading-4 font-semibold text-ivy-dark last:mb-0 whitespace-nowrap">
            {link.label}
          </Link>
        </li>
      ))}
    </ul>
  );
}

function NavMenuItem({ item }: { item: NavCategoryMenu }) {
  return (
    <li className="group relative mr-6 inline-block py-[15px] last:mr-0">
      <Link
        href={item.href}
        className={cn(
          "text-xs font-semibold uppercase transition-colors duration-200 hover:text-ivy-accent",
          item.highlight ? "text-ivy-accent" : "text-ivy-dark",
        )}
      >
        {item.label}
      </Link>
      {item.variant === "category" && <CategorySubMenu item={item} />}
      {item.variant === "collection" && <CollectionSubMenu item={item} />}
      {item.variant === "about" && <AboutSubMenu item={item} />}
    </li>
  );
}

/**
 * Logo + main nav (with mega-menus) for the IVY moda homepage clone.
 * Renders WITHOUT a wrapping <header> — a sibling HeaderActions component
 * (search + account/cart icons) is composed alongside it inside a shared
 * <header> during page assembly. The parent <header> must be
 * `position: relative` and must NOT add its own height — this component's
 * root already sets `h-20` (80px) so the absolutely-centered logo math works.
 */
export function SiteHeaderNav() {
  return (
    <div className="relative h-16 md:h-20">
      <button
        type="button"
        aria-label="Open menu"
        className="mobile-menu absolute left-4 top-1/2 flex h-6 w-7 -translate-y-1/2 flex-col justify-between md:hidden"
      >
        <span className="block h-0.5 w-full bg-ivy-dark" />
        <span className="block h-0.5 w-full bg-ivy-dark" />
        <span className="block h-0.5 w-full bg-ivy-dark" />
      </button>

      <nav className="main-menu hidden h-full items-center md:flex">
        <ul className="menu flex h-full items-center">
          {NAV_ITEMS.map((item) => (
            <NavMenuItem key={item.label} item={item} />
          ))}
        </ul>
      </nav>

      <div className="site-brand absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
        <Link href="/">
          <Image
            src="/images/ivymoda/common/logo.png"
            alt="IVY moda"
            width={206}
            height={66}
          />
        </Link>
      </div>
    </div>
  );
}
