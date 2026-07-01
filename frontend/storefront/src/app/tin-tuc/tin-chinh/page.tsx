import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";
import { LifestyleHeader } from "@/components/LifestyleHeader";
import { FeaturedStoryCard } from "@/components/FeaturedStoryCard";
import { MagazineGrid } from "@/components/MagazineGrid";
import { ProductCard } from "@/components/ProductCard";
import type { Product } from "@/types/ivy";

const IMG = "/images/ivymoda/products";

const TRENDING_PRODUCTS: Product[] = [
  {
    id: "38735",
    name: "Basic Caro Tee",
    href: "/sanpham/basic-caro-tee-ms-57m8608-38735",
    image: `${IMG}/708815ca100964edf6068baf88e0a6d1.webp`,
    hoverImage: `${IMG}/708815ca100964edf6068baf88e0a6d1.webp`,
    price: 650000,
    colors: [{ id: "38735", image: "/images/ivymoda/colors/001.png", label: "default", active: true }],
    sizes: [{ id: "s", label: "s" }, { id: "m", label: "m" }, { id: "l", label: "l" }],
  },
  {
    id: "38883",
    name: "Shirt Dress - Đầm cổ đức",
    href: "/sanpham/shirt-dress-dam-co-duc-ms-48m8584-38883",
    image: `${IMG}/86e4822d0199db1c3f1e0cb46d6f5d19.webp`,
    hoverImage: `${IMG}/86e4822d0199db1c3f1e0cb46d6f5d19.webp`,
    price: 916000,
    oldPrice: 2290000,
    ribbon: "bestseller",
    colors: [{ id: "38883", image: "/images/ivymoda/colors/001.png", label: "default", active: true }],
    sizes: [{ id: "s", label: "s" }, { id: "m", label: "m" }, { id: "l", label: "l" }],
  },
  {
    id: "38789",
    name: "Calista Dress - Đầm dạ hội thêu hoa",
    href: "/sanpham/calista-dress-dam-da-hoi-theu-hoa-ms-45s2802-38789",
    image: `${IMG}/005b1d8bcd067343383e7477f47ee74a.webp`,
    hoverImage: `${IMG}/005b1d8bcd067343383e7477f47ee74a.webp`,
    price: 5990000,
    colors: [{ id: "38789", image: "/images/ivymoda/colors/001.png", label: "default", active: true }],
    sizes: [{ id: "s", label: "s" }, { id: "m", label: "m" }, { id: "l", label: "l" }],
  },
  {
    id: "38674",
    name: "Áo sơ mi phối bèo",
    href: "/sanpham/ao-so-mi-phoi-beo-ms-17m8642-38674",
    image: `${IMG}/20dff4ffed5fafd3613bd8258e64d03e.webp`,
    hoverImage: `${IMG}/20dff4ffed5fafd3613bd8258e64d03e.webp`,
    price: 360000,
    oldPrice: 1200000,
    colors: [{ id: "38674", image: "/images/ivymoda/colors/001.png", label: "default", active: true }],
    sizes: [{ id: "s", label: "s" }, { id: "m", label: "m" }, { id: "l", label: "l" }],
  },
];

export default function LifestyleNewsPage() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
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

          <section>
            <h2 className="mb-6 text-center text-2xl font-semibold tracking-[2px] text-ivy-dark uppercase">
              Modern Trending
            </h2>
            <div className="grid grid-cols-2 gap-6 md:grid-cols-4">
              {TRENDING_PRODUCTS.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          </section>
        </div>
      </main>
      <Footer />
    </>
  );
}
