"use client";

import { RightArrowIcon } from "@/components/icons";

interface ArticleCategory {
  label: string;
  href: string;
}

const CATEGORIES: ArticleCategory[] = [
  {
    label: "Sự kiện thời trang",
    href: "/tin-tuc/danh-muc/su-kien-thoi-trang",
  },
  {
    label: "Blog chia sẻ",
    href: "/tin-tuc/danh-muc/blog",
  },
  {
    label: "Fashion Show",
    href: "/tin-tuc/danh-muc/Fashion-Show",
  },
  {
    label: "Hoạt động cộng đồng",
    href: "/tin-tuc/danh-muc/community-activity",
  },
  {
    label: "Tin nội bộ",
    href: "/tin-tuc/danh-muc/tin-noi-bo",
  },
];

export function ArticleCategorySidebar() {
  return (
    <aside className="lg:sticky lg:top-24">
      <div className="mb-5 text-lg font-semibold text-ivy-dark">Danh mục</div>

      <ul className="hidden lg:block">
        {CATEGORIES.map((category) => (
          <li key={category.href} className="border-b border-ivy-hairline last:border-b-0">
            <a
              href={category.href}
              className="flex items-center justify-between py-3 text-sm text-ivy-text hover:text-ivy-dark"
            >
              {category.label}
              <RightArrowIcon className="size-4 text-ivy-text-muted" />
            </a>
          </li>
        ))}
      </ul>

      <select
        defaultValue=""
        onChange={(e) => {
          if (e.target.value) {
            window.location.href = e.target.value;
          }
        }}
        className="block w-full rounded-lg border border-ivy-hairline px-4 py-3 text-sm lg:hidden"
      >
        <option value="" disabled>
          Danh mục
        </option>
        {CATEGORIES.map((category) => (
          <option key={category.href} value={category.href}>
            {category.label}
          </option>
        ))}
      </select>
    </aside>
  );
}
