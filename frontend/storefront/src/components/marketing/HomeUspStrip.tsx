import { ShieldCheck, RefreshCw, Truck, Camera } from "lucide-react";

const USP_ITEMS = [
  { icon: ShieldCheck, title: "Chính hãng 100%", desc: "Cam kết hàng chính hãng" },
  { icon: RefreshCw, title: "Đổi trả 7 ngày", desc: "Miễn phí đổi size" },
  { icon: Truck, title: "Giao nhanh toàn quốc", desc: "Nhận hàng 2 - 4 ngày" },
  { icon: Camera, title: "Tìm bằng hình ảnh", desc: "Chụp ảnh, ra sản phẩm" },
];

export function HomeUspStrip() {
  return (
    <section className="border-b border-ivy-hairline bg-[#fafafa]">
      <div className="mx-auto grid max-w-342 grid-cols-2 px-4 md:grid-cols-4 md:px-0">
        {USP_ITEMS.map(({ icon: Icon, title, desc }) => (
          <div
            key={title}
            className="flex items-center gap-3 py-5 md:justify-center md:py-6"
          >
            <Icon className="size-7 shrink-0 text-ivy-dark" strokeWidth={1.5} />
            <div>
              <p className="text-[13px] font-semibold uppercase tracking-wide text-ivy-dark md:text-[14px]">
                {title}
              </p>
              <p className="text-[12px] text-ivy-text-muted">{desc}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
