import { BrandPromoCarousel } from "@/components/marketing/BrandPromoCarousel";
import { HomeBanner } from "@/components/marketing/HomeBanner";
import { ProductRail } from "@/modules/product/components/ProductRail";
import { mapProductListItem } from "@/modules/product/mappers";
import { fetchFeaturedProducts, fetchHomeBannerSlides, fetchNewestProducts } from "@/modules/product/queries";

export async function HomeScreen() {
  const [bannerSlides, newProducts, featuredProducts] = await Promise.all([
    fetchHomeBannerSlides(),
    fetchNewestProducts(20),
    fetchFeaturedProducts(12)
  ]);

  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <HomeBanner slides={bannerSlides} />
      <div className="mx-auto max-w-342 px-4 md:px-0">
        <ProductRail title="Sản phẩm mới" products={newProducts.map(mapProductListItem)} />
        {featuredProducts.length > 0 ? <ProductRail title="Sản phẩm nổi bật" products={featuredProducts.map(mapProductListItem)} /> : null}
      </div>
      <BrandPromoCarousel />
    </main>
  );
}
