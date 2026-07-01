import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Breadcrumb } from "@/components/Breadcrumb";
import { ArticleCategorySidebar } from "@/components/ArticleCategorySidebar";
import { ArticleContent } from "@/components/ArticleContent";
import { ArticleRelatedSidebar } from "@/components/ArticleRelatedSidebar";
import { ProductCarouselSection } from "@/components/ProductCarouselSection";
import { NEW_ARRIVAL_WOMEN, NEW_ARRIVAL_MEN } from "@/data/homeProducts";

const IMG = "/images/ivymoda/article";

const BLOCKS = [
  { type: "text" as const, text: "Bên cạnh những hình ảnh trên sàn catwalk, khoảnh khắc tại hậu trường là nơi thể hiện sống động nhất tinh thần cống hiến hết mình của toàn bộ đội ngũ ekip thực hiện." },
  { type: "text" as const, text: "Cùng ghé thăm \"sau ánh hào quang\" của show diễn Express và trải nghiệm sự chuyên nghiệp hoàn hảo của những người mẫu cũng như ekip chụp hình đã tạo nên những tác phẩm thời trang vô cùng đẳng cấp!" },
  { type: "image" as const, src: `${IMG}/36ac628758c67e487555251e9b8558b4.jpg` },
  { type: "text" as const, text: "Sự chỉn chu, cẩn thận được thể hiện trong từng khâu chuẩn bị." },
  { type: "image" as const, src: `${IMG}/620f0349a0b3afa079fe6db482c7a97a.jpg` },
  { type: "text" as const, text: "Outfits lên kệ đầy đủ, ngăn nắp chuẩn bị cho những màn xuất hiện nảy lửa trên sàn Runway." },
  { type: "image" as const, src: `${IMG}/3f5a54bcb0a5a093f27cfe22e0ded7c7.jpg` },
  { type: "text" as const, text: "Siêu mẫu Minh Triệu bên cạnh đội ngũ makeup chuyên nghiệp." },
  { type: "image" as const, src: `${IMG}/3e42bbcce0a9d3ffc1cbca5e6ea570d9.jpg` },
  { type: "image" as const, src: `${IMG}/ff98885c6855b2cf1f2ca7a088f89119.jpg` },
  { type: "text" as const, text: "Dàn model đắt giá và tài năng của IVY moda fashion show chăm chút diện mạo trước khi xuất hiện." },
  { type: "image" as const, src: `${IMG}/e410f84d93796e59d903150b8b47499a.jpg` },
  { type: "image" as const, src: `${IMG}/444aded901552aaef95be4b7107e9303.jpg` },
  { type: "text" as const, text: "Sự hỗ trợ nhiệt tình đến từ đội ngũ Ekip IVY moda." },
  { type: "image" as const, src: `${IMG}/5ec995f5f0ddebda5c231225331c8202.jpg` },
  { type: "text" as const, text: "Những khoảnh khắc đáng nhớ được ghi lại trong hậu trường Express 22 FW2023." },
  { type: "image" as const, src: `${IMG}/6b425ea03e2d138db56c61d09f81c92e.jpg` },
  { type: "image" as const, src: `${IMG}/dca1c2344a84fb02b2f98050269b532f.jpg` },
  { type: "image" as const, src: `${IMG}/0142aa10c737d5369b8ebe058ce73b61.jpg` },
  { type: "image" as const, src: `${IMG}/6f33e64f346bc9f9ec8299e59293c857.jpg` },
  { type: "image" as const, src: `${IMG}/9e2926c0190b4ba3760d2861f9c89345.jpg` },
  { type: "image" as const, src: `${IMG}/db67e715bbe17999c15513a5bb5fd0f6.jpg` },
  { type: "image" as const, src: `${IMG}/522322a61672d0db9de8873e82bda97d.jpg` },
  { type: "text" as const, text: "BST EXPRESS đã có mặt trên tất cả hệ thống showroom IVY moda toàn quốc. Hãy đến để chạm và tự cảm nhận sự cao cấp qua từng thiết kế IVY moda gửi tới nàng nhé!" },
];

const TAGS = [
  { label: "áo sơ mi nam", href: "/danh-muc/ao-so-mi-nam" },
  { label: "Quần jeans nữ", href: "/danh-muc/quan-jean-nu" },
  { label: "Đầm", href: "/danh-muc/dam" },
  { label: "Quần bé gái", href: "/danh-muc/quan-be-gai" },
  { label: "Quần bé trai", href: "/danh-muc/quan-be-trai" },
];

export default function ArticleDetailPage() {
  return (
    <>
      <Header />
      <main className="site-main flex-1 border-b border-ivy-hairline pt-16 md:pt-20">
        <Breadcrumb
          items={[
            { label: "Trang chủ", href: "/" },
            { label: "ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW" },
          ]}
        />
        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <div className="grid grid-cols-1 gap-10 lg:grid-cols-[220px_1fr_280px]">
            <ArticleCategorySidebar />
            <ArticleContent
              title="ĐỘT NHẬP HẬU TRƯỜNG CỦA SHOW DIỄN EXPRESS_FW23 FASHION SHOW"
              date="25/10/2023"
              blocks={BLOCKS}
              tags={TAGS}
            />
            <ArticleRelatedSidebar />
          </div>
        </div>

        <div className="mx-auto max-w-[1380px] px-4 pb-16">
          <ProductCarouselSection
            title="NEW ARRIVAL"
            tabs={[
              { id: "ivy-moda", label: "IVY moda", products: NEW_ARRIVAL_WOMEN },
              { id: "metagent", label: "Metagent", products: NEW_ARRIVAL_MEN },
            ]}
          />
        </div>
      </main>
      <Footer />
    </>
  );
}
