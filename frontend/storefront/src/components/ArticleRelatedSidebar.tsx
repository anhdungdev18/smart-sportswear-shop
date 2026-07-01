import Image from "next/image";

interface RelatedNewsItem {
  href: string;
  image: string;
  title: string;
  date: string;
}

const RELATED_NEWS: RelatedNewsItem[] = [
  {
    href: "/tin-tuc/bai-viet/top-9-mui-huong-nuoc-hoa-dior-cho-nu-thom-nhat-1547",
    image: "/images/ivymoda/article/4727bbbb0a8ba53e0b97d36c35ea5d91.webp",
    title: "Top 9 mùi hương nước hoa Dior cho nữ thơm nhất",
    date: "11/07/2025",
  },
  {
    href: "/tin-tuc/bai-viet/tim-hieu-ve-thuong-hieu-thoi-trang-xa-xi-dior-va-cac-dong-san-pham-noi-bat-cua-thuong-hieu-1546",
    image: "/images/ivymoda/article/54c15ce9d63542e090c87910a7deaf31.webp",
    title:
      "Tìm hiểu về thương hiệu thời trang xa xỉ Dior và các dòng sản phẩm nổi bật của thương hiệu",
    date: "10/07/2025",
  },
  {
    href: "/tin-tuc/bai-viet/tim-hieu-20-dai-su-thuong-hieu-toan-cau-cua-dior-1545",
    image: "/images/ivymoda/article/5d6c83f3593c42a63be5e000512187f9.webp",
    title: "Tìm Hiểu 20+ Đại Sứ Thương Hiệu Toàn Cầu Của Dior",
    date: "09/07/2025",
  },
  {
    href: "/tin-tuc/bai-viet/lich-su-hinh-thanh-va-hanh-trinh-phat-trien-thuong-hieu-nuoc-hoa-dior-1544",
    image: "/images/ivymoda/article/3d5f89e07422bb46124c41bbd920e9c3.webp",
    title: "Lịch Sử Hình Thành Và Hành Trình Phát Triển Thương Hiệu Nước Hoa Dior",
    date: "09/07/2025",
  },
];

const PROMO_BANNER = {
  href: "/danh-muc/sale-all-70-0626",
  image: "/images/ivymoda/brand/3f9cd315b0280fcc5726e7c1816a233d.webp",
};

export function ArticleRelatedSidebar() {
  return (
    <aside className="flex flex-col gap-8 lg:sticky lg:top-24">
      <div>
        <div className="mb-5 text-lg font-semibold text-ivy-dark">Tin mới nhất</div>
        <ul className="flex flex-col gap-4">
          {RELATED_NEWS.map((item) => (
            <li key={item.href}>
              <a href={item.href} className="flex gap-3">
                <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg">
                  <Image
                    src={item.image}
                    alt={item.title}
                    fill
                    sizes="80px"
                    className="object-cover"
                  />
                </div>
                <div className="min-w-0">
                  <h4 className="mb-1 line-clamp-2 text-[13px] leading-[18px] font-semibold text-ivy-dark">
                    {item.title}
                  </h4>
                  <p className="text-xs text-[#A8A9AD]">{item.date}</p>
                </div>
              </a>
            </li>
          ))}
        </ul>
      </div>

      <section className="overflow-hidden rounded-tl-none rounded-tr-3xl rounded-br-none rounded-bl-3xl">
        <a href={PROMO_BANNER.href} className="block">
          <Image
            src={PROMO_BANNER.image}
            alt="Khuyến mãi"
            width={400}
            height={500}
            sizes="(min-width: 1024px) 320px, 100vw"
            className="h-auto w-full"
          />
        </a>
      </section>
    </aside>
  );
}
