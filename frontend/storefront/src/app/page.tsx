import { Header } from "@/components/Header";
import { HomeBanner } from "@/components/HomeBanner";
import { ProductCarouselSection } from "@/components/ProductCarouselSection";
import { BrandPromoCarousel } from "@/components/BrandPromoCarousel";
import { GallerySection } from "@/components/GallerySection";
import { Footer } from "@/components/Footer";
import {
  NEW_ARRIVAL_WOMEN,
  NEW_ARRIVAL_MEN,
  FEATURED_CLASSY,
  SALE_WOMEN,
  SALE_MEN,
} from "@/data/homeProducts";

export default function Home() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <div className="mx-auto max-w-[1380px] px-4">
          <HomeBanner />

          <ProductCarouselSection
            title="NEW ARRIVAL"
            tabs={[
              { id: "ivy-moda", label: "IVY moda", products: NEW_ARRIVAL_WOMEN },
              { id: "metagent", label: "Metagent", products: NEW_ARRIVAL_MEN },
            ]}
          />

          <ProductCarouselSection
            title="THE CLASSY | BST ĐỘC QUYỀN ONLINE x HUYỀN LIZZIE"
            products={FEATURED_CLASSY}
          />

          <ProductCarouselSection
            title="GIÁ MỚI - CHẠM ĐỈNH | SALE ALL 70% CHỈ CÓ TẠI ONLINE"
            tabs={[
              { id: "ivy-moda", label: "IVY moda", products: SALE_WOMEN },
              { id: "metagent", label: "Metagent", products: SALE_MEN },
            ]}
          />

          <GallerySection />
        </div>

        <BrandPromoCarousel />
      </main>
      <Footer />
    </>
  );
}
