import Link from "next/link";
import { cn } from "@/lib/utils";

const SITE_ORIGIN = "";

export type LifestyleSlug = "tin-chinh" | "kien-thuc" | "xu-huong" | "phong-cach" | "blog";

interface LifestyleNavItem {
  label: string;
  slug: LifestyleSlug;
  href: string;
}

const NAV_ITEMS: LifestyleNavItem[] = [
  { label: "TIN TỨC", slug: "tin-chinh", href: `${SITE_ORIGIN}/tin-tuc/tin-chinh` },
  { label: "KIẾN THỨC", slug: "kien-thuc", href: `${SITE_ORIGIN}/tin-tuc/kien-thuc` },
  { label: "XU HƯỚNG", slug: "xu-huong", href: `${SITE_ORIGIN}/tin-tuc/xu-huong` },
  { label: "PHONG CÁCH", slug: "phong-cach", href: `${SITE_ORIGIN}/tin-tuc/phong-cach` },
  { label: "BLOG CHIA SẺ", slug: "blog", href: `${SITE_ORIGIN}/tin-tuc/blog` },
];

interface LifestyleHeaderProps {
  activeSlug: LifestyleSlug;
}

/**
 * Page title + sub-nav for the IVY moda "Lifestyle" magazine section
 * (/tin-tuc/*). Each link navigates to a distinct real category page on
 * this site; the one matching `activeSlug` is styled as active
 * (dark text + underline) while the rest render muted.
 */
export function LifestyleHeader({ activeSlug }: LifestyleHeaderProps) {
  return (
    <div className="py-10 text-center">
      <h1 className="mb-8 text-4xl font-extrabold tracking-[2px] text-ivy-dark uppercase md:text-[56px]">
        LIFESTYLE
      </h1>
      <nav className="mb-10 flex justify-start gap-8 overflow-x-auto border-b border-ivy-hairline whitespace-nowrap md:justify-center">
        {NAV_ITEMS.map((item) => {
          const isActive = item.slug === activeSlug;
          return (
            <Link
              key={item.slug}
              href={item.href}
              className={cn(
                "pb-2 text-sm font-semibold tracking-[1px]",
                isActive
                  ? "border-b-2 border-ivy-dark text-ivy-dark"
                  : "text-ivy-text-muted",
              )}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
