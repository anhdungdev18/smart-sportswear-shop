import { BrandPromoCarousel } from "@/components/marketing/BrandPromoCarousel";
import { FeaturedCollections } from "@/components/marketing/FeaturedCollections";
import { HomeBanner } from "@/components/marketing/HomeBanner";
import { HomeCategoryGrid } from "@/components/marketing/HomeCategoryGrid";
import { HomeFlashSale } from "@/components/marketing/HomeFlashSale";
import { HomeTeamSelector } from "@/components/marketing/HomeTeamSelector";
import { HomeUspStrip } from "@/components/marketing/HomeUspStrip";
import { HomeVisualSearchBanner } from "@/components/marketing/HomeVisualSearchBanner";
import { fetchCollections } from "@/modules/category/queries";
import { ProductRail } from "@/modules/product/components/ProductRail";
import { mapProductListItem } from "@/modules/product/mappers";
import {
  fetchBestSellingProducts,
  fetchFeaturedProducts,
  fetchHomeBannerSlides,
  fetchNewestProducts,
  fetchSaleProducts,
} from "@/modules/product/queries";

export async function HomeScreen() {
  const [bannerSlides, newProducts, featuredProducts, collections, saleProducts, bestSellingProducts] =
    await Promise.all([
      fetchHomeBannerSlides(),
      fetchNewestProducts(20),
      fetchFeaturedProducts(12),
      fetchCollections(),
      fetchSaleProducts(12),
      fetchBestSellingProducts(12),
    ]);

  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <HomeBanner slides={bannerSlides} />
      <HomeUspStrip />
      <div className="mx-auto max-w-342 px-4 md:px-0">
        <HomeCategoryGrid />
        <HomeFlashSale
          products={saleProducts.map((product) => ({ ...mapProductListItem(product), ribbon: "sale" }))}
        />
        <HomeTeamSelector />
        {bestSellingProducts.length > 0 || featuredProducts.length > 0 ? (
          <ProductRail
            title="Sản phẩm được yêu thích"
            tabs={[
              {
                id: "best-selling",
                label: "Bán chạy",
                products: bestSellingProducts.map((product) => ({
                  ...mapProductListItem(product),
                  ribbon: "bestseller" as const,
                })),
              },
              {
                id: "featured",
                label: "Nổi bật",
                products: featuredProducts.map(mapProductListItem),
              },
            ].filter((tab) => tab.products.length > 0)}
          />
        ) : null}
        <FeaturedCollections collections={collections} />
        <ProductRail title="Sản phẩm mới" products={newProducts.map(mapProductListItem)} />
      </div>
      <HomeVisualSearchBanner />
      <BrandPromoCarousel />
    </main>
  );
}
