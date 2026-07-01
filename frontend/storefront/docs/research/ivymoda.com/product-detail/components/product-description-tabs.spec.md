# ProductDescriptionTabs Specification

## Overview
- **Target file:** `src/components/ProductDescriptionTabs.tsx`
- **Screenshot:** `docs/design-references/ivymoda.com/product-detail-desktop-full.png` (below the gallery/info row)
- **Interaction model:** click-driven tabs (3 tabs, only one panel visible at a time, instant switch — no animation on the source site).
- **Props:** none — hardcode the real content below directly in this file (it's specific to the one product page being cloned).

## DOM Structure
```
div (root, full-width below the gallery+info row)
  div.product-detail__tab-header (flex row, centered or left-aligned per screenshot — left-aligned under the price column)
    div.tab-item(.active) × 3 — "GIỚI THIỆU", "CHI TIẾT SẢN PHẨM", "BẢO QUẢN"
  div.product-detail__tab-body
    div.tab-content (only the active tab's content is rendered/visible)
```

## Computed Styles (faithful approximation matching screenshot)
```css
.tab-item { font-size: 14px; font-weight: 600; text-transform: uppercase; color: #6C6D70; padding-bottom: 12px; margin-right: 32px; cursor: pointer; letter-spacing: 1px; }
.tab-item.active { color: #221F20; border-bottom: 2px solid #221F20; }
.product-detail__tab-body { border-top: 1px solid #E7E8E9; padding-top: 20px; }
.tab-content p { font-size: 14px; line-height: 24px; color: #57585A; margin-bottom: 12px; }
.tab-content table td { font-size: 14px; line-height: 24px; color: #57585A; padding-bottom: 5px; }
```

## Text Content (verbatim, real content for "Áo kiểu Day Dream")

### Tab 1: "GIỚI THIỆU" (active by default)
```
Thiết kế mang tinh thần thanh lịch hiện đại với gam trắng tinh khôi cùng bề mặt vải dệt nổi họa tiết lông vũ mềm mại. Phom áo dáng ngắn gọn gàng giúp tôn tỷ lệ cơ thể, đồng thời tạo điểm nhấn tinh tế khi phối cùng chân váy hoặc quần cạp cao.

Hàng cúc kim loại ánh bạc được sắp xếp tối giản trên thân trước, kết hợp cổ tròn thanh lịch, một vẻ ngoài trang nhã và chỉn chu. Thiết kế tay ngắn giúp tổng thể trở nên trẻ trung, phù hợp cho nhiều hoàn cảnh từ công sở đến những buổi gặp gỡ nhẹ nhàng.

Phom dáng: Cropped suông nhẹ, gọn gàng
Chi tiết: Cổ tròn, cúc kim loại nổi bật, bề mặt vải họa tiết lông vũ dệt nổi
Chất liệu: Vải jacquard cao cấp tạo hiệu ứng bề mặt mềm mại, đứng phom nhẹ
Màu sắc: Trắng thanh lịch
Phù hợp: Công sở, sự kiện nhẹ, gặp gỡ hoặc phối đồng bộ cùng chân váy để tạo set thanh lịch hiện đại

Thông tin mẫu:
Chiều cao: 165 cm
Cân nặng: 49 kg
Số đo 3 vòng: 81-63-90 cm
Mẫu mặc size S

Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.
```
(Render each blank-line-separated block as its own `<p>`; the "Phom dáng / Chi tiết / Chất liệu / Màu sắc / Phù hợp" block is one paragraph with `<br>` between lines, each line's first phrase bold via `<strong>`.)

### Tab 2: "CHI TIẾT SẢN PHẨM" (a 2-column key/value table)
| Field | Value |
|---|---|
| Dòng sản phẩm | You |
| Nhóm sản phẩm | Áo |
| Cổ áo | Cổ tròn |
| Tay áo | Tay ngắn |
| Kiểu dáng | Xuông |
| Độ dài | Croptop |
| Họa tiết | Hoa |
| Chất liệu | Vải bề mặt |

### Tab 3: "BẢO QUẢN" (care instructions — representative excerpt, real site continues further but this is sufficient real content)
```
Chi tiết bảo quản sản phẩm:

* Các sản phẩm thuộc dòng cao cấp (Senora) và áo khoác (dạ, tweed...) nên giặt khô chuyên nghiệp để giữ form dáng và chất liệu vải tốt nhất.
* Giặt tay nhẹ nhàng với nước lạnh hoặc nước ấm dưới 30°C đối với các sản phẩm vải thông thường.
* Không ngâm sản phẩm quá lâu trong nước giặt, không vắt xoắn mạnh.
* Phơi sản phẩm ở nơi thoáng mát, tránh ánh nắng trực tiếp để giữ màu vải bền đẹp.
* Ủi ở nhiệt độ phù hợp với từng loại chất liệu, tránh ủi trực tiếp lên các chi tiết cúc, đính kết.
```

## States & Behaviors
### Tab switch
- **Trigger:** click on `.tab-item`
- Local `useState<number>` for `activeTab` (0/1/2), only the matching `.tab-content` renders. Instant switch, no transition needed (matches source).

## Responsive Behavior
Same content and interaction at all breakpoints; tab labels may need `text-xs` sizing or wrap to a scrollable row on narrow mobile widths if they don't fit (`overflow-x-auto` on the tab header row is an acceptable mobile accommodation).
