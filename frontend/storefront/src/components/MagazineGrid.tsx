import Image from "next/image";

interface MagazineItem {
  image: string;
  caption: string;
  title: string;
  date: string;
}

const MAGAZINE_ITEMS: MagazineItem[] = [
  {
    image: "/images/ivymoda/news/47b630796bec23aa195d7a59d1597231.jpg",
    caption:
      "Tối 21/10 , ca sĩ Văn Mai Hương đã có mặt tại Trung tâm Hội nghị Quốc Gia Hà Nội để tham dự EXPRESS_FALL/WINTER 2023 FASHION SHOW của IVY moda.",
    title:
      "SÀN RUNWAY EXPRESS BÙNG NỔ VỚI 2 BẢN REMIX MỚI NHẤT CỦA CA SỸ VĂN MAI HƯƠNG",
    date: "23/10/2023",
  },
  {
    image: "/images/ivymoda/news/3ac10c24b55ebbab6e1af7078643fd81.jpg",
    caption:
      "Vào ngày 21.10.2023 tại Trung tâm Hội nghị Quốc Gia - 57 Phạm Hùng, Hà Nội, IVY moda ra mắt thành công show diễn thứ 22 mang tên EXPRESS.",
    title:
      "EXPRESS_FALL/WINTER 2023 FASHION SHOW - LỜI BÀY TỎ TỪ GIÁ TRỊ ĐÍCH THỰC",
    date: "23/10/2023",
  },
  {
    image: "/images/ivymoda/news/f50407a8b2dc3e8dce84e5aabee3b688.jpg",
    caption:
      "Tối 21/10, Kỳ Duyên - Minh Triệu thu hút nhiều ánh nhìn khi xuất hiện trong show diễn thời trang Express 22 FW2023 của IVY moda",
    title:
      "Kỳ Duyên - Minh Triệu diện váy cúp ngực khoe vóc dáng gợi cảm tại EXPRESS 22 FW2023",
    date: "23/10/2023",
  },
];

export function MagazineGrid() {
  return (
    <div className="grid grid-cols-1 gap-6 mb-12 sm:grid-cols-2 lg:grid-cols-3">
      {MAGAZINE_ITEMS.map((item) => (
        <a key={item.image} href="#" className="block">
          <div className="relative mb-4 aspect-4/3">
            <Image
              src={item.image}
              alt={item.title}
              fill
              className="object-cover"
            />
          </div>
          <div>
            <p className="mb-2 text-[13px] leading-5 text-ivy-text">
              {item.caption}
            </p>
            <p className="mb-2 text-[15px] leading-[22px] font-bold text-ivy-dark">
              {item.title}
            </p>
            <p className="text-xs text-ivy-price-old">{item.date}</p>
          </div>
        </a>
      ))}
    </div>
  );
}
