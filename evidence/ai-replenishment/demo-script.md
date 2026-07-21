# Kịch Bản Demo AI Replenishment (5–7 phút)

## 1. Giới thiệu (1 phút)
- **Mở đầu**: Xin chào các bạn. Tôi sẽ trình bày module AI Replenishment (Dự báo và Đề xuất Nhập hàng) vừa được tích hợp vào Smart Sportswear Shop.
- **Mục tiêu**: Giúp Admin trả lời 2 câu hỏi: Nhập mặt hàng nào? Số lượng bao nhiêu là an toàn?
- **Kiến trúc**: Hệ thống chạy với 2 Microservices (Core ở port 8082 và AI ở port 8081). Chế độ Shared-database mode trên Supabase để đảm bảo cô lập dữ liệu tính toán mà không làm chậm việc bán hàng.

## 2. Các chỉ số tổng quan (1 phút)
- **Hành động**: Đăng nhập tài khoản Admin, mở menu **Tồn kho -> AI Đề xuất nhập hàng**.
- **Trình bày**: 
  - Chỉ ra 4 con số KPI ở đầu trang: Tổng đề xuất, Cấp bách (Critical), Cao (High), và Đang xử lý (Pending).
  - Nhấn mạnh vào 1 sản phẩm đang có trạng thái CRITICAL.

## 3. Quy trình Dự báo (1.5 phút)
- **Hành động**: Bấm nút **Chạy dự báo AI**.
- **Trình bày**: 
  - Khi chạy dự báo, hệ thống tự động đồng bộ (sync) snapshot kho hàng mới nhất.
  - Sử dụng thuật toán Walk-Forward Backtest 30 ngày để đo lường 3 mô hình.
- **Hành động**: Click vào nút Xem Chi Tiết của sản phẩm CRITICAL.
- **Trình bày**:
  - Giao diện biểu đồ (Chart): Đường liền là Thực tế (Actual), đường đứt nét là Dự báo (Forecast). 
  - Chỉ số MAE/WAPE: Giải thích model nào thắng.
  - Giải thích bảng tính toán: Safety Stock, Reorder Point, Suggested Quantity.

## 4. Xử lý Đề xuất và Nhập kho (2 phút)
- **Hành động**: 
  - Bấm nút **Duyệt** (Accept).
  - Quay ra danh sách, xác minh rằng số lượng tồn kho (Stock) vẫn CHƯA THAY ĐỔI.
  - Bấm nút **Điền vào form nhập kho** trên dòng vừa duyệt.
- **Trình bày**:
  - Form nhập hàng của Core tự động điền SKU, số lượng đã duyệt và ghi chú.
- **Hành động**:
  - Bấm **Xác nhận nhập kho** (Submit).
  - Tải lại danh sách, chỉ ra rằng tồn kho (Stock) đã thực sự tăng lên, và phiếu nhập kho đã được ghi nhận.

## 5. Báo cáo Benchmark và Mô phỏng (1 phút)
- **Trình bày**:
  - Show bảng Baseline vs Proposed: Số ngày cháy hàng giảm xuống 0, Tỷ lệ đáp ứng đạt 100%.
  - Hiệu năng: 2.000 SKU tốn 1.5 giây.
- **Kết thúc**: Cảm ơn các bạn đã lắng nghe.
