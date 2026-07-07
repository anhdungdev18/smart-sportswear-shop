import { MapPin } from "lucide-react";

export function StoreMapPanel() {
  return (
    <div>
      <div className="relative aspect-[4/3] overflow-hidden rounded-2xl bg-[#E8EAE6] lg:h-[500px]">
        <div
          className="absolute inset-0"
          style={{
            backgroundImage:
              "repeating-linear-gradient(0deg, rgba(0,0,0,0.04) 0px, rgba(0,0,0,0.04) 1px, transparent 1px, transparent 40px), repeating-linear-gradient(90deg, rgba(0,0,0,0.04) 0px, rgba(0,0,0,0.04) 1px, transparent 1px, transparent 40px)",
          }}
        />
        <MapPin
          className="absolute top-[30%] left-[35%] size-8 text-ivy-accent drop-shadow-md"
          fill="currentColor"
        />
        <MapPin
          className="absolute top-[45%] left-[55%] size-8 text-ivy-accent drop-shadow-md"
          fill="currentColor"
        />
        <MapPin
          className="absolute top-[60%] left-[40%] size-8 text-ivy-accent drop-shadow-md"
          fill="currentColor"
        />
        <span className="absolute bottom-2 right-2 rounded bg-black/40 px-2 py-1 text-xs text-white">
          Bản đồ minh họa
        </span>
      </div>

      <div className="mt-6">
        <h4 className="mb-2 text-[15px] font-semibold text-ivy-dark">
          Chính sách vận chuyển
        </h4>
        <ul className="list-disc pl-5">
          <li className="mb-1 text-sm leading-[22px] text-ivy-text">
            Nội thành Hà Nội, TP Hồ Chí Minh:{" "}
            <span className="text-[#E74C3C]">đồng giá 25k</span>
          </li>
          <li className="mb-1 text-sm leading-[22px] text-ivy-text">
            Đơn đi tỉnh gần (Khoảng cách đến HN/HCM &lt; 100km): đồng giá 33k
          </li>
          <li className="mb-1 text-sm leading-[22px] text-ivy-text">
            Đơn đi tỉnh gần (Khoảng cách đến HN/HCM &gt; 100 km): đồng giá 38k
          </li>
        </ul>

        <h4 className="mb-2 mt-4 text-[15px] font-semibold text-ivy-dark">
          Chính sách miễn phí vận chuyển
        </h4>
        <ul className="list-disc pl-5">
          <li className="mb-1 text-sm leading-[22px] text-ivy-text">
            <span className="text-[#E74C3C]">Miễn phí vận chuyển</span>{" "}
            cho đơn hàng giá trị từ 700,000đ
          </li>
          <li className="mb-1 text-sm leading-[22px] text-ivy-text">
            <span className="text-[#E74C3C]">Miễn phí vận chuyển</span>{" "}
            cho đơn hàng có sản phẩm nguyên giá &gt; 500k
          </li>
        </ul>
      </div>
    </div>
  );
}
