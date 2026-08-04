import { Pagination } from "@/components/shared/Pagination";
import { CategorySidebarFilter } from "@/modules/category/components/CategorySidebarFilter";
import { CategoryToolbar } from "@/modules/category/components/CategoryToolbar";
import { fetchSearchResults } from "@/modules/category/queries";
import type { PageMeta } from "@/modules/category/types";
import { ProductCard } from "@/modules/product/components/ProductCard";
import { mapProductListItem } from "@/modules/product/mappers";
import type { ProductListItem } from "@/modules/product/types";

const PAGE_SIZE = 20;

export async function SearchResultsPage({
  searchParams,
}: {
  searchParams: Promise<{ q?: string; page?: string; discount?: string }>;
}) {
  const { q, page: pageParam, discount } = await searchParams;
  const query = q?.trim() ?? "";
  const currentPage = Math.max(1, Number(pageParam ?? 1));
  const isSaleListing = discount === "any";
  const { products, meta }: { products: ProductListItem[]; meta: PageMeta } = await fetchSearchResults(
    query,
    currentPage,
    PAGE_SIZE,
    isSaleListing ? "any" : undefined,
  );

  const mappedProducts = products.map(mapProductListItem);
  const title = isSaleListing
    ? "Tất cả sản phẩm ưu đãi"
    : query
      ? `Kết quả tìm kiếm theo "${query}"`
      : "Kết quả tìm kiếm";

  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <div className="mx-auto flex max-w-[1380px] flex-col gap-8 px-4 pb-16 pt-6 lg:flex-row">
        <CategorySidebarFilter />
        <div className="flex-1">
          <CategoryToolbar title={title} resultCount={meta.total} />
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
                  searchParams={{ q: query || undefined, discount: isSaleListing ? "any" : undefined }}
                />
              </div>
            </>
          ) : (
            <p className="text-ivy-text-muted">
              {isSaleListing
                ? "Hiện chưa có sản phẩm ưu đãi."
                : query
                  ? "Không tìm thấy sản phẩm phù hợp."
                  : "Nhập từ khóa để tìm kiếm sản phẩm."}
            </p>
          )}
        </div>
      </div>
    </main>
  );
}
