import { CategorySidebarFilter } from "@/components/CategorySidebarFilter";
import { CategoryToolbar } from "@/components/CategoryToolbar";
import { ProductCard } from "@/components/ProductCard";
import { Pagination } from "@/components/Pagination";
import { apiFetch } from "@/lib/api";
import { endpoints } from "@/lib/endpoints";
import { mapProductListItem } from "@/lib/mappers";
import type { ProductListItem, PageMeta } from "@/types/api";
const PAGE_SIZE = 20;
export default async function SearchPage({
  searchParams,
}: {
  searchParams: Promise<{ q?: string; page?: string }>;
}) {
  const { q, page: pageParam } = await searchParams;
  const query = q?.trim() ?? "";
  const currentPage = Math.max(1, Number(pageParam ?? 1));
  let products: ProductListItem[] = [];
  let meta: PageMeta = { page: 1, size: PAGE_SIZE, total: 0, totalPages: 1 };
  if (query) {
    try {
      const result = await apiFetch<ProductListItem[]>(endpoints.products, {
        query: { q: query, page: currentPage, limit: PAGE_SIZE },
      });
      products = result.data;
      if (result.meta) meta = result.meta as unknown as PageMeta;
    } catch {
      // Show empty state on API errors
    }
  }
  const mappedProducts = products.map(mapProductListItem);
  return (
    <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
      <div className="mx-auto flex max-w-[1380px] flex-col gap-8 px-4 pb-16 pt-6 lg:flex-row">
        <CategorySidebarFilter />
        <div className="flex-1">
          <CategoryToolbar
            title={query ? `Kết quả tìm kiếm theo "${query}"` : "Kết quả tìm kiếm"}
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
                  baseHref="/tim-kiem"
                />
              </div>
            </>
          ) : (
            <p className="text-ivy-text-muted">
              {query
                ? "Không tìm thấy sản phẩm phù hợp."
                : "Nhập từ khóa để tìm kiếm sản phẩm."}
            </p>
          )}
        </div>
      </div>
    </main>
  );
}
