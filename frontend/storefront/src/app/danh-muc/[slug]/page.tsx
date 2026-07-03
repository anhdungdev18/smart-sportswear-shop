import { Breadcrumb } from "@/components/Breadcrumb";
import { CategorySidebarFilter } from "@/components/CategorySidebarFilter";
import { CategoryToolbar } from "@/components/CategoryToolbar";
import { ProductCard } from "@/components/ProductCard";
import { Pagination } from "@/components/Pagination";
import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import { mapProductListItem } from "@/lib/mappers";
import type { ProductListItem, PageMeta } from "@/types/api";

const PAGE_SIZE = 20;

type CategorySearchParams = {
  page?: string;
  sort?: string;
  sortBy?: string;
  sortOrder?: string;
  size?: string;
  color?: string;
  minPrice?: string;
  maxPrice?: string;
};

export default async function CategoryPage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>;
  searchParams: Promise<CategorySearchParams>;
}) {
  const { slug } = await params;
  const resolvedSearchParams = await searchParams;
  const { page: pageParam, sort, sortBy, sortOrder, size, color, minPrice, maxPrice } = resolvedSearchParams;
  const currentPage = Math.max(1, Number(pageParam ?? 1));

  let products: ProductListItem[] = [];
  let meta: PageMeta = { page: 1, size: PAGE_SIZE, total: 0, totalPages: 1 };
  let categoryName = slug.replace(/-/g, " ");

  const [productsResult, categoryResult] = await Promise.allSettled([
    apiFetch<ProductListItem[]>(endpoints.products, {
      query: {
        categorySlug: slug,
        page: currentPage,
        limit: PAGE_SIZE,
        sort: sort || undefined,
        sortBy: sort ? undefined : sortBy ?? "createdAt",
        sortOrder: sort ? undefined : sortOrder ?? "desc",
        size: size || undefined,
        color: color || undefined,
        minPrice: minPrice || undefined,
        maxPrice: maxPrice || undefined,
      },
    }),
    apiFetch<{ name: string }>(endpoints.category(slug)),
  ]);

  if (productsResult.status === "fulfilled") {
    products = productsResult.value.data;
    if (productsResult.value.meta) meta = productsResult.value.meta as unknown as PageMeta;
  }

  if (categoryResult.status === "fulfilled") {
    categoryName = categoryResult.value.data.name;
  }

  const mappedProducts = products.map(mapProductListItem);

  return (
    <main className="site-main flex-1 border-b border-ivy-hairline pt-[78px]">
      <Breadcrumb
        items={[
          { label: "Trang chủ", href: "/" },
          { label: categoryName },
        ]}
      />
      <div className="mx-auto flex max-w-[1368px] flex-col gap-8 px-4 pb-16 md:px-0 lg:flex-row">
        <CategorySidebarFilter
          initialSize={size}
          initialColor={color}
          initialMinPrice={minPrice ? Number(minPrice) : undefined}
          initialMaxPrice={maxPrice ? Number(maxPrice) : undefined}
          sizeType={slug.includes("giay") ? "shoe" : "cloth"}
        />
        <div className="flex-1">
          <CategoryToolbar title={categoryName} resultCount={meta.total} currentSort={sort} />
          {mappedProducts.length > 0 ? (
            <>
              <div className="grid grid-cols-2 gap-x-7 gap-y-12 sm:grid-cols-3 lg:grid-cols-4">
                {mappedProducts.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
              <div className="mt-10">
                <Pagination
                  currentPage={currentPage}
                  totalPages={meta.totalPages}
                  baseHref={`/danh-muc/${slug}`}
                  searchParams={resolvedSearchParams}
                />
              </div>
            </>
          ) : (
            <p className="border border-ivy-hairline py-10 text-center text-ivy-text-muted">
              Không có sản phẩm nào trong danh mục này.
            </p>
          )}
        </div>
      </div>
    </main>
  );
}
