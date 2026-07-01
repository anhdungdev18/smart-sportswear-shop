import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";
import { LookbookIntro } from "@/components/LookbookIntro";
import { ProductCard } from "@/components/ProductCard";
import { DAILY_MOOD_PRODUCTS } from "@/data/lookbookProducts";

export default function DailyMoodLookbookPage() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: "DAILY MOOD" },
          ]}
        />
        <LookbookIntro
          bannerImage="/images/ivymoda/banner/6a051c7c1a148911a0f04bb13704e9e4.webp"
          eyebrow="NEW COLLECTION 2026"
          title="DAILY MOOD"
          description="Bộ sưu tập khơi nguồn cảm hứng từ những khoảnh khắc trà chiều thành thị yên bình, nơi thời gian như lắng đọng bên khung cửa gỗ để nhường chỗ cho sự thư thái. Trên tinh thần ấy, các nhà thiết kế đã lựa chọn ngôn ngữ tối giản nhưng đầy chiều sâu khi để hai gam màu Trắng - Đen kinh điển xuyên suốt các thiết kế, điểm xuyết thêm các họa tiết hoa lá thiên nhiên đơn sắc được vẽ nét mảnh vô cùng tinh tế, nhẹ nhàng mà không hề phô trương."
          editorialImages={[
            "/images/ivymoda/lookbook/565c2865e6497f2b6a1310017af86f39.webp",
            "/images/ivymoda/lookbook/164e491a614ae80c318fc4e3376b9ac5.webp",
          ]}
        />
        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <div className="grid grid-cols-2 gap-x-6 gap-y-10 sm:grid-cols-3">
            {DAILY_MOOD_PRODUCTS.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        </div>
      </main>
      <Footer />
    </>
  );
}
