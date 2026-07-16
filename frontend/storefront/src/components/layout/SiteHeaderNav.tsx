"use client";

import { useRef, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import type { NavCategoryMenu, NavSubItemGroup } from "@/components/layout/types";
import { HEADER_NAV_ITEMS } from "@/modules/content/data/layout";
import { cn } from "@/lib/utils";

function CategorySubMenu({ item }: { item: NavCategoryMenu }) {
  const pathname = usePathname();
  return (
    <div className="fixed left-0 top-[var(--site-header-height)] z-30 w-full border-y border-ivy-hairline bg-white px-14 py-8 shadow-[0_10px_30px_rgba(34,31,32,0.06)]">
      <div className="grid grid-cols-[220px_repeat(7,minmax(0,1fr))] gap-8">
        {item.quickLinks?.length ? (
          <div className="pr-3">
            {item.quickLinks.map((quickLink) => {
              // Red = an intentional promo accent (highlight) OR the category the
              // user is currently viewing. Without the active check the left-column
              // marker never follows navigation between sub-categories.
              const isActive = quickLink.href !== "#" && pathname === quickLink.href;
              return (
                <Link
                  key={quickLink.label}
                  href={quickLink.href}
                  className={cn(
                    "mb-7 block text-[15px] font-semibold leading-6 text-ivy-dark last:mb-0",
                    (quickLink.highlight || isActive) && "text-[#ff1f1f]",
                  )}
                >
                  {quickLink.label}
                </Link>
              );
            })}
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
    <div className="absolute left-0 top-[calc(100%-1px)] z-30 w-125 border border-ivy-hairline bg-white px-6 py-8 shadow-[0_10px_30px_rgba(34,31,32,0.06)]">
      <div className="max-w-90">
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
    <div className="absolute left-0 top-[calc(100%-1px)] z-30 w-90 border border-ivy-hairline bg-white px-6 py-8 shadow-[0_10px_30px_rgba(34,31,32,0.06)]">
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
    <li className="relative py-4.5" onMouseEnter={onOpen} onMouseLeave={onClose}>
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
        {HEADER_NAV_ITEMS.map((item) => (
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
