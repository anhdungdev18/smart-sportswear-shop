import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
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

export default async function CategoryPage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{ page?: string; sortBy?: string; sortOrder?: string }>;
}) {
  const { slug } = await params;
  const { page: pageParam, sortBy, sortOrder } = await searchParams;
  const currentPage = Math.max(1, Number(pageParam ?? 1));

  let products: ProductListItem[] = [];
  let meta: PageMeta = { page: 1, size: PAGE_SIZE, total: 0, totalPages: 1 };
  let categoryName = slug;

  try {
    const result = await apiFetch<ProductListItem[]>(endpoints.products, {
      query: {
        categorySlug: slug,
        page: currentPage,
        limit: PAGE_SIZE,
        sortBy: sortBy ?? "createdAt",
        sortOrder: sortOrder ?? "desc",
      },
    });
    products = result.data;
    if (result.meta) meta = result.meta as unknown as PageMeta;
  } catch {
    // Show empty state on API errors
  }

  try {
    const catResult = await apiFetch<{ name: string }>(endpoints.category(slug));
    categoryName = catResult.data.name;
  } catch {
    // Use slug as fallback name
  }

  const mappedProducts = products.map(mapProductListItem);

  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: categoryName },
          ]}
        />
        <div className="mx-auto flex max-w-[1380px] flex-col gap-8 px-4 pb-16 lg:flex-row">
          <CategorySidebarFilter />
          <div className="flex-1">
            <CategoryToolbar
              title={categoryName}
              resultCount={meta.total}
            />
            {mappedProducts.length > 0 ? (
              <>
                <div className="grid grid-cols-2 gap-x-6 gap-y-10 sm:grid-cols-3 lg:grid-cols-4">
                  {mappedProducts.map((product) => (
                    <ProductCard key={product.id} product={product} />
                  ))}
                </div>
                <div className="mt-10">
                  <Pagination
                    currentPage={currentPage}
                    totalPages={meta.totalPages}
                    baseHref={`/danh-muc/${slug}`}
                  />
                </div>
              </>
            ) : (
              <p className="py-10 text-ivy-text-muted">
                Không có sản phẩm nào trong danh mục này.
              </p>
            )}
          </div>
        </div>
      </main>
      <Footer />
    </>
  );
}
