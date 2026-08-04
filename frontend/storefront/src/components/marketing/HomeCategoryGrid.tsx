import Link from "next/link";
import { Shirt, Footprints, Wind, PersonStanding, Sparkles, Trophy } from "lucide-react";

const CATEGORIES = [
  { label: "Áo Đá Bóng", href: "/danh-muc/ao-da-bong", icon: Trophy },
  { label: "Giày Đá Bóng", href: "/danh-muc/giay-da-bong-fg", icon: Footprints },
  { label: "Áo Chạy Bộ", href: "/danh-muc/ao-chay-bo", icon: Shirt },
  { label: "Quần Chạy Bộ", href: "/danh-muc/quan-chay-bo", icon: PersonStanding },
  { label: "Giày Sân Cỏ Nhân Tạo", href: "/danh-muc/giay-da-bong-tf", icon: Wind },
  { label: "Bộ Sưu Tập", href: "/bo-suu-tap", icon: Sparkles },
];

export function HomeCategoryGrid() {
  return (
    <section className="mb-16 mt-14">
      <div className="mb-8 text-center">
        <h2 className="inline-block border-b border-ivy-dark pb-2 font-[Montserrat,sans-serif] text-[18px] font-light uppercase tracking-wide text-ivy-dark md:text-[28px]">
          Mua theo danh mục
        </h2>
      </div>

      <div className="grid grid-cols-3 gap-4 md:grid-cols-6 md:gap-6">
        {CATEGORIES.map(({ label, href, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            className="group flex flex-col items-center gap-3 rounded-xl border border-ivy-hairline p-4 text-center transition-colors hover:border-ivy-dark hover:bg-[#fafafa]"
          >
            <span className="flex size-16 items-center justify-center rounded-full bg-[#f3f3f3] transition-colors group-hover:bg-ivy-dark">
              <Icon
                className="size-7 text-ivy-dark transition-colors group-hover:text-white"
                strokeWidth={1.5}
              />
            </span>
            <span className="text-[13px] font-medium leading-tight text-ivy-dark">
              {label}
            </span>
          </Link>
        ))}
      </div>
    </section>
  );
}
