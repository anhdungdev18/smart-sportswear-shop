# Chuyển AI Replenishment thành service Admin độc lập

## 1. Mục tiêu

Tách dự báo nhu cầu và đề xuất nhập hàng khỏi Core Backend thành một Spring Boot service độc lập. Core không còn chứa controller, thuật toán, entity hoặc repository của AI.

- Core Backend (`:8082` khi chạy local; production tùy cấu hình) sở hữu sản phẩm, đơn hàng, người dùng và mọi thay đổi tồn kho.
- AI Forecasting Service (`:8081`) sở hữu thuật toán, chính sách tồn kho, forecast run và đề xuất nhập hàng.
- Admin Frontend dùng hai API client riêng.
- Hai service tạm dùng chung PostgreSQL nhưng có lịch sử Flyway riêng.

## 2. Kiến trúc sau chuyển đổi

```text
Admin Frontend
  |-- Core API (:8082 local) -> product, order, inventory, user
  `-- AI API   (:8081) -> forecast, policy, recommendation, admin action

Core tables (AI chỉ đọc)
  products, product_variants, orders, order_items, users

AI tables (AI sở hữu và ghi)
  inventory_policies, forecast_runs, replenishment_recommendations
```

AI đọc snapshot SKU/tồn kho và lịch sử bán bằng `JdbcClient`. AI không map hoặc lưu JPA entity Product, Order, User của Core. `acted_by` lấy từ subject của JWT, không lấy từ request body.

## 3. Các thay đổi đã thực hiện

### AI Forecasting Service

- Giảm bản sao backend từ hơn 500 file xuống 47 Java source chính.
- Xóa toàn bộ module Core sao chép: auth CRUD, cart, checkout, payment, shipping, notification, product/order/user CRUD và các module không liên quan.
- Thay liên kết JPA tới Core entity bằng `variantId`, `actedBy` và read-model JDBC.
- Chỉ giữ API `/api/v1/admin/replenishment/**` cùng health/OpenAPI.
- Xác minh access JWT do Core phát hành bằng cùng `JWT_ACCESS_SECRET`; không truy vấn `UserRepository`.
- Chạy tại `${SERVER_PORT:8081}`.
- Hibernate dùng `ddl-auto: validate`, không tự sửa schema.

### Flyway

- AI chỉ quét `classpath:db/ai-migration`.
- AI dùng bảng `flyway_ai_schema_history`.
- Migration AI bắt đầu tại `V1__replenishment_forecasting.sql` và dùng `IF NOT EXISTS` để tiếp quản database đã có bảng.
- Core không còn migration replenishment mới và không còn runtime AI.

### Core Backend

- Đã xóa `modules/replenishment` và test tương ứng.
- Core tiếp tục là nơi duy nhất thay đổi tồn kho.
- Các migration Core cũ đã bị thay thế/xóa theo kế hoạch làm sạch hiện tại; cần kiểm tra Flyway history của môi trường trước lần deploy đầu tiên.

### Admin Frontend

- Client Core vẫn dùng `NEXT_PUBLIC_API_BASE_URL` / `SERVER_API_BASE_URL`.
- Client AI mới dùng `NEXT_PUBLIC_AI_API_BASE_URL` / `SERVER_AI_API_BASE_URL`.
- Chỉ module `replenishment` dùng AI client.
- Đã sửa response phân trang Spring từ `data` sang `content`.
- Đã xóa component replenishment placeholder không được import.

## 4. Cấu hình chạy local

```env
# frontend/admin
NEXT_PUBLIC_API_BASE_URL=http://localhost:8082
SERVER_API_BASE_URL=http://localhost:8082
NEXT_PUBLIC_AI_API_BASE_URL=http://localhost:8081
SERVER_AI_API_BASE_URL=http://localhost:8081

# dùng chung giữa Core và AI
JWT_ACCESS_SECRET=<same-secret>
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dunghaiquyen
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

Thứ tự chạy: PostgreSQL, Core Backend, AI Forecasting Service, Admin Frontend.

## 5. Kiểm tra đã chạy

- AI service: `./mvnw clean test` — 12 test, 0 lỗi.
- Core Backend: `./mvnw clean package -Dmaven.test.skip=true` — build thành công, 390 Java source.
- Admin Frontend: `npm run build` — build production thành công, 22 route.
- Bộ integration test Core trên máy hiện tại bị biến môi trường datasource ngoài project ghi đè bằng URL không bắt đầu bằng `jdbc`; đây không phải lỗi biên dịch do chuyển module.

## 6. Kiểm tra trước deploy

1. Sao lưu `flyway_schema_history`, `flyway_ai_schema_history` và ba bảng AI.
2. Kiểm tra các migration Core V13-V17 từng được áp dụng trên database đích; không deploy mù khi history còn tham chiếu migration đã xóa.
3. Chạy AI service trước ở môi trường staging và xác nhận `flyway_ai_schema_history` baseline version 0 rồi áp dụng V1.
4. Smoke test: list, detail, generate, accept, adjust, dismiss qua `:8081`.
5. Xác nhận nhập/xuất/điều chỉnh kho vẫn chỉ qua Core `:8082`.

## 7. Rollback

- Không xóa ba bảng AI hoặc dữ liệu hiện có.
- Có thể dừng AI service mà không ảnh hưởng nghiệp vụ bán hàng Core.
- Khi cần rollback FE tạm thời, ẩn phần AI replenishment; không đưa quyền ghi tồn kho sang AI.
- Không trả migration AI về Core nếu chưa có kế hoạch hợp nhất hai bảng Flyway history.