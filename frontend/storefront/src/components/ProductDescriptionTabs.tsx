"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";

const TABS = ["GIỚI THIỆU", "CHI TIẾT SẢN PHẨM", "BẢO QUẢN"] as const;

type TabIndex = 0 | 1 | 2;

const PRODUCT_DETAILS: Array<{ field: string; value: string }> = [
  { field: "Dòng sản phẩm", value: "You" },
  { field: "Nhóm sản phẩm", value: "Áo" },
  { field: "Cổ áo", value: "Cổ tròn" },
  { field: "Tay áo", value: "Tay ngắn" },
  { field: "Kiểu dáng", value: "Xuông" },
  { field: "Độ dài", value: "Croptop" },
  { field: "Họa tiết", value: "Hoa" },
  { field: "Chất liệu", value: "Vải bề mặt" },
];

const CARE_INSTRUCTIONS = [
  "Các sản phẩm thuộc dòng cao cấp (Senora) và áo khoác (dạ, tweed...) nên giặt khô chuyên nghiệp để giữ form dáng và chất liệu vải tốt nhất.",
  "Giặt tay nhẹ nhàng với nước lạnh hoặc nước ấm dưới 30°C đối với các sản phẩm vải thông thường.",
  "Không ngâm sản phẩm quá lâu trong nước giặt, không vắt xoắn mạnh.",
  "Phơi sản phẩm ở nơi thoáng mát, tránh ánh nắng trực tiếp để giữ màu vải bền đẹp.",
  "Ủi ở nhiệt độ phù hợp với từng loại chất liệu, tránh ủi trực tiếp lên các chi tiết cúc, đính kết.",
];

const FALLBACK_INTRO = (
  <>
    <p className="mb-3 text-sm leading-6 text-ivy-text">
      Thiết kế mang tinh thần thanh lịch hiện đại với gam trắng tinh khôi cùng
      bề mặt vải dệt nổi họa tiết lông vũ mềm mại. Phom áo dáng ngắn gọn gàng
      giúp tôn tỷ lệ cơ thể, đồng thời tạo điểm nhấn tinh tế khi phối cùng
      chân váy hoặc quần cạp cao.
    </p>
    <p className="mb-3 text-sm leading-6 text-ivy-text">
      Hàng cúc kim loại ánh bạc được sắp xếp tối giản trên thân trước, kết hợp
      cổ tròn thanh lịch, một vẻ ngoài trang nhã và chỉn chu. Thiết kế tay ngắn
      giúp tổng thể trở nên trẻ trung, phù hợp cho nhiều hoàn cảnh từ công sở
      đến những buổi gặp gỡ nhẹ nhàng.
    </p>
    <p className="mb-3 text-sm leading-6 text-ivy-text">
      Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do
      điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.
    </p>
  </>
);

export function ProductDescriptionTabs({ description }: { description?: string }) {
  const [activeTab, setActiveTab] = useState<TabIndex>(0);

  return (
    <div>
      <div className="flex overflow-x-auto whitespace-nowrap">
        {TABS.map((label, index) => (
          <button
            key={label}
            type="button"
            onClick={() => setActiveTab(index as TabIndex)}
            className={cn(
              "mr-8 cursor-pointer pb-3 text-sm font-semibold uppercase tracking-[1px] text-ivy-text-muted",
              activeTab === index && "border-b-2 border-ivy-dark text-ivy-dark",
            )}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="border-t border-ivy-hairline pt-5">
        {activeTab === 0 && (
          <div>
            {description ? (
              <div
                className="prose prose-sm max-w-none text-ivy-text [&_p]:mb-3 [&_p]:leading-6"
                dangerouslySetInnerHTML={{ __html: description }}
              />
            ) : (
              FALLBACK_INTRO
            )}
          </div>
        )}

        {activeTab === 1 && (
          <table className="w-full">
            <tbody>
              {PRODUCT_DETAILS.map((row) => (
                <tr key={row.field}>
                  <td className="w-1/3 pb-[5px] text-sm leading-6 text-ivy-text">
                    {row.field}
                  </td>
                  <td className="pb-[5px] text-sm leading-6 text-ivy-text">
                    {row.value}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {activeTab === 2 && (
          <div>
            <p className="mb-3 text-sm leading-6 text-ivy-text">
              Chi tiết bảo quản sản phẩm:
            </p>
            <ul className="list-disc pl-5">
              {CARE_INSTRUCTIONS.map((item) => (
                <li key={item} className="mb-3 text-sm leading-6 text-ivy-text">
                  {item}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}
