import { Breadcrumb } from "@/components/shared/Breadcrumb";
import { ArticleCategorySidebar } from "@/components/editorial/ArticleCategorySidebar";
import { ArticleContent } from "@/components/editorial/ArticleContent";
import { ArticleRelatedSidebar } from "@/components/editorial/ArticleRelatedSidebar";
import { ProductRail } from "@/modules/product/components/ProductRail";
import { mapProductListItem } from "@/modules/product/mappers";
import { fetchNewestProducts } from "@/modules/product/queries";
import { ARTICLE_DETAIL_BLOCKS, ARTICLE_DETAIL_TAGS } from "@/modules/content/data/editorial";

export default async function ArticleDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;

  const newestProducts = await fetchNewestProducts(8);
  const newArrivals = newestProducts.map(mapProductListItem);

  return (
    <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
      <Breadcrumb
        items={[
          { label: "Trang chủ", href: "/" },
          { label: "Lifestyle", href: "/tin-tuc/tin-chinh" },
          { label: slug },
        ]}
      />
      <div className="mx-auto max-w-[1380px] px-4 pb-16">
        <div className="grid grid-cols-1 gap-10 lg:grid-cols-[220px_1fr_280px]">
          <ArticleCategorySidebar />
          <ArticleContent
            title="ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW"
            date="25/10/2023"
            blocks={ARTICLE_DETAIL_BLOCKS}
            tags={ARTICLE_DETAIL_TAGS}
          />
          <ArticleRelatedSidebar />
        </div>
      </div>

      {newArrivals.length > 0 && (
        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <ProductRail title="NEW ARRIVAL" products={newArrivals} />
        </div>
      )}
    </main>
  );
}
