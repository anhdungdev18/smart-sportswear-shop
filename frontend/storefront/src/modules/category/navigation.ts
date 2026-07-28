import type { Category } from "@/modules/category/types";
import { HEADER_NAV_ITEMS } from "@/modules/content/data/layout";
import type { NavCategoryMenu, NavSubItemGroup } from "@/modules/content/types";

const NON_CATEGORY_ITEMS = HEADER_NAV_ITEMS.filter(
  (item) => item.variant !== "category",
);

function categoryHref(slug: string) {
  return `/danh-muc/${slug}`;
}

function chunkChildren(children: Category[], size = 6): Category[][] {
  const chunks: Category[][] = [];
  for (let index = 0; index < children.length; index += size) {
    chunks.push(children.slice(index, index + size));
  }
  return chunks;
}

function buildGroups(root: Category): NavSubItemGroup[] {
  const children = [...(root.children ?? [])].sort(
    (left, right) =>
      (left.sortOrder ?? 0) - (right.sortOrder ?? 0) ||
      left.name.localeCompare(right.name, "vi"),
  );

  return chunkChildren(children).map((chunk, index) => ({
    heading: index === 0 ? `Danh mục ${root.name}` : `${root.name} khác`,
    headingHref: categoryHref(root.slug),
    links: chunk.map((child) => ({
      label: child.name,
      href: categoryHref(child.slug),
    })),
  }));
}

export function buildHeaderNavItems(categoryTree: Category[]): NavCategoryMenu[] {
  const roots = categoryTree
    .filter((category) => category.nodeType === "GROUP" || (category.children?.length ?? 0) > 0)
    .sort(
      (left, right) =>
        (left.sortOrder ?? 0) - (right.sortOrder ?? 0) ||
        left.name.localeCompare(right.name, "vi"),
    );

  if (roots.length === 0) return HEADER_NAV_ITEMS;

  const categoryItems: NavCategoryMenu[] = roots.map((root) => ({
    label: root.name.toLocaleUpperCase("vi"),
    href: categoryHref(root.slug),
    variant: "category",
    quickLinks: [
      {
        label: `TẤT CẢ ${root.name.toLocaleUpperCase("vi")}`,
        href: categoryHref(root.slug),
      },
    ],
    groups: buildGroups(root),
  }));

  return [...categoryItems, ...NON_CATEGORY_ITEMS];
}
