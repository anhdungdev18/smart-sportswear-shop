"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";

const TABS = ["GIỚI THIỆU", "CHI TIẾT SẢN PHẨM", "BẢO QUẢN"] as const;
type TabIndex = 0 | 1 | 2;

const CARE_INSTRUCTIONS = [
  "Giặt máy ở nhiệt độ tối đa 30°C với chu trình nhẹ nhàng để bảo vệ sợi vải kỹ thuật.",
  "Không dùng thuốc tẩy hoặc chất tẩy rửa mạnh; chọn nước giặt dịu nhẹ dành cho đồ thể thao.",
  "Phơi tự nhiên, tránh ánh nắng trực tiếp để giữ màu vải và độ đàn hồi.",
  "Không sấy máy ở nhiệt độ cao — nhiệt làm hỏng lớp thoáng khí và công nghệ co giãn.",
  "Ủi ở nhiệt độ thấp nếu cần; tránh ủi trực tiếp lên logo hoặc họa tiết in nhiệt.",
];

const FALLBACK_INTRO = (
  <>
    <p className="mb-3 text-[15px] leading-7 text-ivy-text">
      Được chế tác từ chất liệu kỹ thuật cao cấp, sản phẩm mang lại sự thoải mái và hiệu suất tối ưu trong mọi hoạt động thể thao.
      Thiết kế công thái học bám sát đường nét cơ thể, hỗ trợ chuyển động tự nhiên và linh hoạt.
    </p>
    <p className="mb-3 text-[15px] leading-7 text-ivy-text">
      Công nghệ thoáng khí tiên tiến giúp điều hòa thân nhiệt, thấm hút mồ hôi nhanh chóng, giữ cơ thể luôn khô thoáng và mát mẻ suốt buổi tập.
    </p>
    <p className="mb-3 text-[15px] leading-7 text-ivy-text">
      Lưu ý: Màu sắc thực tế có thể chênh lệch nhỏ so với ảnh do điều kiện ánh sáng chụp và màn hình hiển thị.
    </p>
  </>
);

interface Props {
  description?: string;
  attributes?: Record<string, string>;
}

export function ProductDescriptionTabs({ description, attributes }: Props) {
  const [activeTab, setActiveTab] = useState<TabIndex>(0);

  const attributeRows = attributes ? Object.entries(attributes) : [];

  return (
    <div className="border-t border-ivy-hairline pt-6">
      <div className="mb-6 flex overflow-x-auto whitespace-nowrap border-b border-ivy-hairline">
        {TABS.map((label, index) => (
          <button
            key={label}
            type="button"
            onClick={() => setActiveTab(index as TabIndex)}
            className={cn(
              "mr-8 cursor-pointer pb-4 text-[14px] font-semibold uppercase tracking-[0.08em] text-ivy-text-muted",
              activeTab === index && "border-b-2 border-ivy-dark text-ivy-dark",
            )}
          >
            {label}
          </button>
        ))}
      </div>

      <div>
        {activeTab === 0 && (
          <div>
            {description ? (
              <div
                className="prose prose-sm max-w-none text-ivy-text [&_p]:mb-3 [&_p]:leading-7"
                dangerouslySetInnerHTML={{ __html: description }}
              />
            ) : (
              FALLBACK_INTRO
            )}
          </div>
        )}

        {activeTab === 1 && (
          <table className="w-full max-w-190">
            <tbody>
              {attributeRows.length > 0 ? (
                attributeRows.map(([field, value]) => (
                  <tr key={field} className="border-b border-[#f1f2f4] last:border-b-0">
                    <td className="w-1/3 py-3 text-[15px] leading-7 text-ivy-text">{field}</td>
                    <td className="py-3 text-[15px] leading-7 text-ivy-text">{value}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td className="py-3 text-[15px] text-ivy-text-muted" colSpan={2}>
                    Chưa có thông tin chi tiết cho sản phẩm này.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}

        {activeTab === 2 && (
          <div>
            <p className="mb-3 text-[15px] leading-7 text-ivy-text">Chi tiết bảo quản sản phẩm:</p>
            <ul className="list-disc pl-5">
              {CARE_INSTRUCTIONS.map((item) => (
                <li key={item} className="mb-3 text-[15px] leading-7 text-ivy-text">
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
