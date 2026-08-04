import type {
  ArticleCategoryItem,
  ArticleContentBlock,
  ArticleTagItem,
  LifestyleNavItem,
  MagazineItem,
  PromoBannerItem,
  RelatedNewsItem,
} from "@/modules/content/types";

const SITE_ORIGIN = "";
const ARTICLE_IMAGE_ROOT = "/images/ivymoda/article";

export const ARTICLE_SHARE_LABELS = [
  "Chia sẻ qua Facebook",
  "Chia sẻ qua Twitter",
  "Chia sẻ qua Pinterest",
  "Chia sẻ qua Zalo",
  "Sao chép liên kết",
] as const;

export const ARTICLE_CATEGORIES: ArticleCategoryItem[] = [
  { label: "Sự kiện thời trang", href: "/tin-tuc/danh-muc/su-kien-thoi-trang" },
  { label: "Blog chia sẻ", href: "/tin-tuc/danh-muc/blog" },
  { label: "Fashion Show", href: "/tin-tuc/danh-muc/Fashion-Show" },
  { label: "Hoạt động cộng đồng", href: "/tin-tuc/danh-muc/community-activity" },
  { label: "Tin nội bộ", href: "/tin-tuc/danh-muc/tin-noi-bo" },
];

export const LIFESTYLE_NAV_ITEMS: LifestyleNavItem[] = [
  { label: "TIN TỨC", slug: "tin-chinh", href: `${SITE_ORIGIN}/tin-tuc/tin-chinh` },
  { label: "KIẾN THỨC", slug: "kien-thuc", href: `${SITE_ORIGIN}/tin-tuc/kien-thuc` },
  { label: "XU HƯỚNG", slug: "xu-huong", href: `${SITE_ORIGIN}/tin-tuc/xu-huong` },
  { label: "PHONG CÁCH", slug: "phong-cach", href: `${SITE_ORIGIN}/tin-tuc/phong-cach` },
  { label: "BLOG CHIA SẺ", slug: "blog", href: `${SITE_ORIGIN}/tin-tuc/blog` },
];

export const MAGAZINE_ITEMS: MagazineItem[] = [
  {
    image: "/images/ivymoda/news/47b630796bec23aa195d7a59d1597231.jpg",
    caption:
      "Tối 21/10, ca sĩ Văn Mai Hương đã có mặt tại Trung tâm Hội nghị Quốc gia Hà Nội để tham dự EXPRESS_FALL/WINTER 2023 FASHION SHOW của Điểm Đến Thể Thao.",
    title: "SÀN RUNWAY EXPRESS BÙNG NỔ VỚI 2 BẢN REMIX MỚI NHẤT CỦA CA SĨ VĂN MAI HƯƠNG",
    date: "23/10/2023",
  },
  {
    image: "/images/ivymoda/news/3ac10c24b55ebbab6e1af7078643fd81.jpg",
    caption:
      "Vào ngày 21/10/2023 tại Trung tâm Hội nghị Quốc gia - 57 Phạm Hùng, Hà Nội, Điểm Đến Thể Thao ra mắt thành công show diễn thứ 22 mang tên EXPRESS.",
    title: "EXPRESS_FALL/WINTER 2023 FASHION SHOW - LỜI BÀY TỎ TỪ GIÁ TRỊ ĐÍCH THỰC",
    date: "23/10/2023",
  },
  {
    image: "/images/ivymoda/news/f50407a8b2dc3e8dce84e5aabee3b688.jpg",
    caption:
      "Tối 21/10, Kỳ Duyên - Minh Triệu thu hút nhiều ánh nhìn khi xuất hiện trong show diễn thời trang Express 22 FW2023 của Điểm Đến Thể Thao.",
    title: "KỲ DUYÊN - MINH TRIỆU DIỆN VÁY CÚP NGỰC KHOE VÓC DÁNG GỢI CẢM TẠI EXPRESS 22 FW2023",
    date: "23/10/2023",
  },
];

export const RELATED_NEWS_ITEMS: RelatedNewsItem[] = [
  {
    href: "/tin-tuc/bai-viet/top-9-mui-huong-nuoc-hoa-dior-cho-nu-thom-nhat-1547",
    image: "/images/ivymoda/article/4727bbbb0a8ba53e0b97d36c35ea5d91.webp",
    title: "Top 9 mùi hương nước hoa Dior cho nữ thơm nhất",
    date: "11/07/2025",
  },
  {
    href: "/tin-tuc/bai-viet/tim-hieu-ve-thuong-hieu-thoi-trang-xa-xi-dior-va-cac-dong-san-pham-noi-bat-cua-thuong-hieu-1546",
    image: "/images/ivymoda/article/54c15ce9d63542e090c87910a7deaf31.webp",
    title: "Tìm hiểu về thương hiệu thời trang xa xỉ Dior và các dòng sản phẩm nổi bật của thương hiệu",
    date: "10/07/2025",
  },
  {
    href: "/tin-tuc/bai-viet/tim-hieu-20-dai-su-thuong-hieu-toan-cau-cua-dior-1545",
    image: "/images/ivymoda/article/5d6c83f3593c42a63be5e000512187f9.webp",
    title: "Tìm hiểu 20+ đại sứ thương hiệu toàn cầu của Dior",
    date: "09/07/2025",
  },
  {
    href: "/tin-tuc/bai-viet/lich-su-hinh-thanh-va-hanh-trinh-phat-trien-thuong-hieu-nuoc-hoa-dior-1544",
    image: "/images/ivymoda/article/3d5f89e07422bb46124c41bbd920e9c3.webp",
    title: "Lịch sử hình thành và hành trình phát triển thương hiệu nước hoa Dior",
    date: "09/07/2025",
  },
];

export const ARTICLE_PROMO_BANNER: PromoBannerItem = {
  href: "/danh-muc/sale-all-70-0626",
  image: "/images/ivymoda/brand/3f9cd315b0280fcc5726e7c1816a233d.webp",
};

export const ARTICLE_DETAIL_BLOCKS: ArticleContentBlock[] = [
  {
    type: "text",
    text: "Bên cạnh những hình ảnh trên sàn catwalk, khoảnh khắc tại hậu trường là nơi thể hiện sống động nhất tinh thần cống hiến hết mình của toàn bộ đội ngũ ekip thực hiện.",
  },
  {
    type: "text",
    text: "Cùng ghé thăm “sau ánh hào quang” của show diễn Express và trải nghiệm sự chuyên nghiệp hoàn hảo của những người mẫu cũng như ekip chụp hình đã tạo nên những tác phẩm thời trang vô cùng đẳng cấp.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/36ac628758c67e487555251e9b8558b4.jpg` },
  {
    type: "text",
    text: "Sự chỉn chu, cẩn thận được thể hiện trong từng khâu chuẩn bị.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/620f0349a0b3afa079fe6db482c7a97a.jpg` },
  {
    type: "text",
    text: "Outfit lên kệ đầy đủ, ngăn nắp chuẩn bị cho những màn xuất hiện rực lửa trên sàn runway.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/3f5a54bcb0a5a093f27cfe22e0ded7c7.jpg` },
  {
    type: "text",
    text: "Siêu mẫu Minh Triệu bên cạnh đội ngũ makeup chuyên nghiệp.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/3e42bbcce0a9d3ffc1cbca5e6ea570d9.jpg` },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/ff98885c6855b2cf1f2ca7a088f89119.jpg` },
  {
    type: "text",
    text: "Dàn model đắt giá và tài năng của Điểm Đến Thể Thao fashion show chăm chút diện mạo trước khi xuất hiện.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/e410f84d93796e59d903150b8b47499a.jpg` },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/444aded901552aaef95be4b7107e9303.jpg` },
  {
    type: "text",
    text: "Sự hỗ trợ nhiệt tình đến từ đội ngũ ekip Điểm Đến Thể Thao.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/5ec995f5f0ddebda5c231225331c8202.jpg` },
  {
    type: "text",
    text: "Những khoảnh khắc đáng nhớ được ghi lại trong hậu trường Express 22 FW2023.",
  },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/6b425ea03e2d138db56c61d09f81c92e.jpg` },
  { type: "image", src: `${ARTICLE_IMAGE_ROOT}/dca1c2344a84fb02b2f98050269b532f.jpg` },
];

export const ARTICLE_DETAIL_TAGS: ArticleTagItem[] = [
  { label: "Áo sơ mi", href: "/danh-muc/ao-so-mi" },
  { label: "Quần jeans", href: "/danh-muc/quan-jeans" },
  { label: "Đầm", href: "/danh-muc/dam" },
];
