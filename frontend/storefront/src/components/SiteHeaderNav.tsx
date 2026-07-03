"use client";

import { useRef, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import type { NavCategoryMenu, NavSubItemGroup } from "@/types/ivy";

const SITE_ORIGIN = "";

const NAV_ITEMS: NavCategoryMenu[] = [
  {
    label: "ĐÁ BÓNG",
    href: `${SITE_ORIGIN}/danh-muc/ao-da-bong`,
    variant: "category",
    quickLinks: [
      { label: "TẤT CẢ SẢN PHẨM", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong` },
      { label: "MÙA GIẢI 2024/25 – HÀNG MỚI VỀ", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong`, highlight: true },
    ],
    groups: [
      {
        heading: "TRANG PHỤC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-da-bong`,
        links: [
          { label: "Áo đấu CLB & ĐTQG", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong` },
          { label: "Quần đá bóng", href: `${SITE_ORIGIN}/danh-muc/quan-da-bong` },
          { label: "Áo tập luyện", href: `${SITE_ORIGIN}/danh-muc/ao-da-bong` },
        ],
      },
      {
        heading: "GIÀY ĐÁ BÓNG",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-da-bong-fg`,
        links: [
          { label: "Giày cỏ thật (FG)", href: `${SITE_ORIGIN}/danh-muc/giay-da-bong-fg` },
          { label: "Giày cỏ nhân tạo (TF)", href: `${SITE_ORIGIN}/danh-muc/giay-da-bong-tf` },
          { label: "Giày futsal (IC)", href: `${SITE_ORIGIN}/danh-muc/giay-futsal` },
        ],
      },
      {
        heading: "PHỤ KIỆN",
        headingHref: `${SITE_ORIGIN}/danh-muc/phu-kien-da-bong`,
        links: [
          { label: "Phụ kiện đá bóng", href: `${SITE_ORIGIN}/danh-muc/phu-kien-da-bong` },
          { label: "Găng tay thủ môn", href: `${SITE_ORIGIN}/danh-muc/gang-tay-thu-mon` },
          { label: "Bóng thể thao", href: `${SITE_ORIGIN}/danh-muc/bong-the-thao` },
        ],
      },
    ],
  },
  {
    label: "CHẠY BỘ",
    href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo`,
    variant: "category",
    quickLinks: [
      { label: "TẤT CẢ SẢN PHẨM", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo` },
      { label: "SUMMER RUN 2024", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo`, highlight: true },
    ],
    groups: [
      {
        heading: "TRANG PHỤC CHẠY BỘ",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-chay-bo`,
        links: [
          { label: "Áo chạy bộ nam", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo` },
          { label: "Áo chạy bộ nữ", href: `${SITE_ORIGIN}/danh-muc/ao-chay-bo` },
          { label: "Quần chạy bộ", href: `${SITE_ORIGIN}/danh-muc/quan-chay-bo` },
        ],
      },
      {
        heading: "GIÀY CHẠY BỘ",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-chay-bo`,
        links: [
          { label: "Nike Running", href: `${SITE_ORIGIN}/danh-muc/giay-chay-bo` },
          { label: "Adidas Running", href: `${SITE_ORIGIN}/danh-muc/giay-chay-bo` },
          { label: "Tất cả giày chạy bộ", href: `${SITE_ORIGIN}/danh-muc/giay-chay-bo` },
        ],
      },
    ],
  },
  {
    label: "BÓNG RỔ",
    href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro`,
    variant: "category",
    quickLinks: [
      { label: "BASKETBALL COLLECTION", href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro`, highlight: true },
    ],
    groups: [
      {
        heading: "TRANG PHỤC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-bong-ro`,
        links: [
          { label: "Áo bóng rổ", href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro` },
          { label: "Quần bóng rổ", href: `${SITE_ORIGIN}/danh-muc/ao-bong-ro` },
        ],
      },
      {
        heading: "GIÀY BÓNG RỔ",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-bong-ro`,
        links: [
          { label: "Nike Basketball", href: `${SITE_ORIGIN}/danh-muc/giay-bong-ro` },
          { label: "Adidas Basketball", href: `${SITE_ORIGIN}/danh-muc/giay-bong-ro` },
          { label: "Puma Basketball", href: `${SITE_ORIGIN}/danh-muc/giay-bong-ro` },
        ],
      },
    ],
  },
  {
    label: "GYM & FITNESS",
    href: `${SITE_ORIGIN}/danh-muc/do-gym-nam`,
    variant: "category",
    quickLinks: [
      { label: "ĐỒ GYM NAM", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
      { label: "ĐỒ GYM NỮ", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu`, highlight: true },
    ],
    groups: [
      {
        heading: "GYM NAM",
        headingHref: `${SITE_ORIGIN}/danh-muc/do-gym-nam`,
        links: [
          { label: "Áo tập gym nam", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
          { label: "Quần tập gym nam", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
          { label: "Compression nam", href: `${SITE_ORIGIN}/danh-muc/do-gym-nam` },
        ],
      },
      {
        heading: "GYM NỮ",
        headingHref: `${SITE_ORIGIN}/danh-muc/do-gym-nu`,
        links: [
          { label: "Sports bra", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
          { label: "Legging nữ", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
          { label: "Áo tank top nữ", href: `${SITE_ORIGIN}/danh-muc/do-gym-nu` },
        ],
      },
    ],
  },
  {
    label: "CẦU LÔNG & TENNIS",
    href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis`,
    variant: "category",
    quickLinks: [
      { label: "TẤT CẢ SẢN PHẨM", href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis` },
    ],
    groups: [
      {
        heading: "TRANG PHỤC",
        headingHref: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis`,
        links: [
          { label: "Áo cầu lông & tennis nam", href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis` },
          { label: "Áo cầu lông & tennis nữ", href: `${SITE_ORIGIN}/danh-muc/ao-cau-long-tennis` },
        ],
      },
      {
        heading: "GIÀY CẦU LÔNG & TENNIS",
        headingHref: `${SITE_ORIGIN}/danh-muc/giay-cau-long`,
        links: [
          { label: "Giày cầu lông", href: `${SITE_ORIGIN}/danh-muc/giay-cau-long` },
          { label: "Giày tennis", href: `${SITE_ORIGIN}/danh-muc/giay-cau-long` },
        ],
      },
    ],
  },
  {
    label: "BỘ SƯU TẬP",
    href: `${SITE_ORIGIN}/bo-suu-tap`,
    variant: "collection",
    groups: [
      {
        heading: "Bộ sưu tập nổi bật",
        headingHref: `${SITE_ORIGIN}/bo-suu-tap`,
        links: [
          { label: "MÙA GIẢI 2024/25", href: `${SITE_ORIGIN}/lookbook/mua-giai-2024-25` },
          { label: "ĐỘI TUYỂN VIỆT NAM 2024", href: `${SITE_ORIGIN}/lookbook/doi-tuyen-viet-nam-2024` },
          { label: "SUMMER RUN COLLECTION", href: `${SITE_ORIGIN}/lookbook/summer-run-collection` },
          { label: "BASKETBALL COLLECTION", href: `${SITE_ORIGIN}/lookbook/basketball-collection` },
          { label: "Xem tất cả →", href: `${SITE_ORIGIN}/bo-suu-tap` },
        ],
      },
    ],
  },
  {
    label: "VỀ CHÚNG TÔI",
    href: "#",
    variant: "about",
    groups: [
      {
        heading: "John's Sport Shop",
        headingHref: `${SITE_ORIGIN}/about/gioi-thieu`,
        links: [
          { label: "Giới thiệu cửa hàng", href: `${SITE_ORIGIN}/about/gioi-thieu` },
          { label: "Hệ thống cửa hàng", href: `${SITE_ORIGIN}/lien-he` },
          { label: "Tin tức thể thao", href: `${SITE_ORIGIN}/tin-tuc/tin-chinh` },
          { label: "Liên hệ", href: `${SITE_ORIGIN}/lien-he` },
        ],
      },
    ],
  },
];

function CategorySubMenu({ item }: { item: NavCategoryMenu }) {
  return (
    <div className="fixed left-[62px] top-[77px] z-30 w-[calc(100vw-124px)] border border-ivy-hairline bg-white px-6 py-8 shadow-[0_10px_30px_rgba(34,31,32,0.06)]">
      <div className="grid grid-cols-[220px_repeat(7,minmax(0,1fr))] gap-8">
        {item.quickLinks?.length ? (
          <div className="pr-3">
            {item.quickLinks.map((quickLink) => (
              <Link
                key={quickLink.label}
                href={quickLink.href}
                className={cn(
                  "mb-7 block text-[15px] font-semibold leading-6 text-ivy-dark last:mb-0",
                  quickLink.highlight && "text-[#ff1f1f]",
                )}
              >
                {quickLink.label}
              </Link>
            ))}
          </div>
        ) : null}

        {item.groups?.map((group: NavSubItemGroup) => (
          <div key={group.heading} className="min-w-0">
            <Link href={group.headingHref} className="mb-5 block text-[15px] font-semibold uppercase leading-5 text-ivy-dark">
              {group.heading}
            </Link>
            <ul className="space-y-3.5">
              {group.links.map((link) => (
                <li key={link.label}>
                  <Link href={link.href} className="block text-[15px] leading-6 text-ivy-text hover:text-ivy-accent">
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}

function CollectionSubMenu({ item }: { item: NavCategoryMenu }) {
  const group = item.groups?.[0];
  if (!group) return null;

  return (
    <div className="absolute left-0 top-[calc(100%-1px)] z-30 w-[500px] border border-ivy-hairline bg-white px-6 py-8 shadow-[0_10px_30px_rgba(34,31,32,0.06)]">
      <div className="max-w-[360px]">
        <p className="mb-6 text-[16px] font-semibold leading-5 text-ivy-dark">{group.heading}</p>
        <ul className="space-y-4">
          {group.links.map((link) => (
            <li key={link.label}>
              <Link href={link.href} className="block text-[15px] leading-6 text-ivy-dark hover:text-ivy-accent">
                {link.label}
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function AboutSubMenu({ item }: { item: NavCategoryMenu }) {
  const group = item.groups?.[0];
  if (!group) return null;

  return (
    <div className="absolute left-0 top-[calc(100%-1px)] z-30 w-[360px] border border-ivy-hairline bg-white px-6 py-8 shadow-[0_10px_30px_rgba(34,31,32,0.06)]">
      <div className="max-w-[320px]">
        <p className="mb-6 text-[16px] font-semibold leading-5 text-ivy-dark">{group.heading}</p>
        <ul className="space-y-4">
          {group.links.map((link) => (
            <li key={link.label}>
              <Link href={link.href} className="block text-[15px] leading-6 text-ivy-dark hover:text-ivy-accent">
                {link.label}
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function NavMenuItem({
  item,
  open,
  active,
  onOpen,
  onClose,
}: {
  item: NavCategoryMenu;
  open: boolean;
  active: boolean;
  onOpen: () => void;
  onClose: () => void;
}) {
  return (
    <li className="relative py-[18px]" onMouseEnter={onOpen} onMouseLeave={onClose}>
      <Link
        href={item.href}
        className={cn(
          "block whitespace-nowrap text-[15px] font-semibold uppercase tracking-[0.005em] text-ivy-dark transition-colors duration-200",
          active ? "text-[#d92d20]" : "hover:text-ivy-accent",
        )}
      >
        {item.label}
      </Link>

      {open && item.variant === "category" ? <CategorySubMenu item={item} /> : null}
      {open && item.variant === "collection" ? <CollectionSubMenu item={item} /> : null}
      {open && item.variant === "about" ? <AboutSubMenu item={item} /> : null}
    </li>
  );
}

export function SiteHeaderNav() {
  const pathname = usePathname();
  const [openLabel, setOpenLabel] = useState<string | null>(null);
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const clearCloseTimer = () => {
    if (closeTimerRef.current) {
      clearTimeout(closeTimerRef.current);
      closeTimerRef.current = null;
    }
  };

  const openMenu = (label: string) => {
    clearCloseTimer();
    setOpenLabel(label);
  };

  const scheduleClose = () => {
    clearCloseTimer();
    closeTimerRef.current = setTimeout(() => {
      setOpenLabel(null);
    }, 120);
  };

  const itemMatchesPath = (item: NavCategoryMenu) => {
    const paths = new Set<string>();
    if (item.href && item.href !== "#") paths.add(item.href);
    item.quickLinks?.forEach((quickLink) => paths.add(quickLink.href));
    item.groups?.forEach((group) => {
      paths.add(group.headingHref);
      group.links.forEach((link) => paths.add(link.href));
    });

    return Array.from(paths).some((path) => path && path !== "#" && pathname === path);
  };

  return (
    <nav className="hidden h-full items-center justify-start md:flex" onMouseLeave={scheduleClose} onMouseEnter={clearCloseTimer}>
      <ul className="flex h-full items-center gap-7 xl:gap-8">
        {NAV_ITEMS.map((item) => (
          <NavMenuItem
            key={item.label}
            item={item}
            open={openLabel === item.label}
            active={itemMatchesPath(item)}
            onOpen={() => openMenu(item.label)}
            onClose={scheduleClose}
          />
        ))}
      </ul>
    </nav>
  );
}
