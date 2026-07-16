import { Breadcrumb } from "@/components/shared/Breadcrumb";
import { LifestyleHeader } from "@/components/editorial/LifestyleHeader";
import { FeaturedStoryCard } from "@/components/editorial/FeaturedStoryCard";
import { MagazineGrid } from "@/components/editorial/MagazineGrid";
import { ProductRail } from "@/modules/product/components/ProductRail";
import { mapProductListItem } from "@/modules/product/mappers";
import { fetchNewestProducts } from "@/modules/product/queries";

export default async function LifestyleNewsPage() {
  const newestProducts = await fetchNewestProducts(8).catch(() => []);
  const trending = newestProducts.map(mapProductListItem);

  return (
    <main className="site-main page-below-header flex-1 border-b border-ivy-hairline">
      <Breadcrumb
        items={[
          { label: "Trang chủ", href: "/" },
          { label: "Tin tức" },
        ]}
      />
      <div className="mx-auto max-w-[1380px] px-4 pb-16">
        <LifestyleHeader activeSlug="tin-chinh" />

        <FeaturedStoryCard
          image="/images/ivymoda/news/47b630796bec23aa195d7a59d1597231.jpg"
          eyebrow="STORY"
          title="ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW"
          excerpt="Bên cạnh những hình ảnh trên sàn catwalk, khoảnh khắc tại hậu trường là nơi thể hiện sống động nhất tinh thần cống hiến hết mình của toàn bộ đội ngũ ekip thực hiện."
          date="25/10/2023"
          href="#"
        />
        <FeaturedStoryCard
          image="/images/ivymoda/news/5aaf578c14a70d76a45c36de9e77a037.jpg"
          eyebrow="STORY"
          title="QUIETLUXURY: KHI SỰ KHIÊM NHƯỜNG ẨN CHỨA NÉT CAO SANG"
          excerpt="Quietluxury của IVY moda mang đến vẻ đẹp riêng biệt một cách thầm lặng. Giống như người phụ nữ an tĩnh và sâu sắc, họ vẫn luôn dùng trái tim yêu để đối diện với khó khăn, theo đuổi lối sống tinh tế, tao nhã và chẳng cần chưng diện những họa tiết logo để khẳng định mình là ai."
          date="19/10/2023"
          href="#"
          reverse
        />

        <MagazineGrid />

        {trending.length > 0 && (
          <section className="mt-8">
            <ProductRail title="MODERN TRENDING" products={trending} />
          </section>
        )}
      </div>
    </main>
  );
}
