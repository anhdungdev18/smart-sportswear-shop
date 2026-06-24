# PHASE 1 SPEC

## 1. Mục tiêu

Phase 1 là phiên bản sản phẩm hoàn chỉnh không AI, tập trung vào một hệ thống bán trang phục thể thao có thể vận hành được từ đầu đến cuối.

Sản phẩm phải đáp ứng được 4 mục tiêu:

- Khách hàng có thể tìm, chọn, đặt mua sản phẩm
- Admin có thể quản lý được sản phẩm, đơn hàng, tồn kho, người dùng
- Hệ thống có phân quyền, lưu trữ dữ liệu thật, xử lý lỗi cơ bản
- Có thể demo local hoặc deploy staging để chạy end-to-end

## 2. Định nghĩa "hoàn chỉnh" cho Phase 1

Phase 1 được xem là hoàn chỉnh khi:

- Có frontend cho khách hàng
- Có admin dashboard cho vận hành
- Có backend API hoạt động thật
- Có PostgreSQL lưu dữ liệu thật
- Có luồng mua hàng end-to-end
- Có thanh toán online mức demo
- Có quản lý tồn kho theo biến thể
- Có phân quyền theo vai trò
- Có tài liệu cài đặt và seed data
- Có test tối thiểu cho các luồng chính

## 3. Đối tượng người dùng

### 3.1 Customer

- Đăng ký, đăng nhập
- Xem và tìm sản phẩm
- Thêm giỏ hàng
- Đặt hàng
- Thanh toán
- Theo dõi đơn hàng

### 3.2 Admin

- Toàn quyền hệ thống
- Quản lý sản phẩm, tồn kho, đơn hàng, người dùng
- Xem báo cáo cơ bản

### 3.3 Sales staff

- Xem danh sách đơn
- Xem chi tiết đơn
- Xác nhận, đóng gói, cập nhật giao hàng

### 3.4 Warehouse staff

- Xem tồn kho
- Nhập kho
- Xuất kho
- Điều chỉnh tồn kho

## 4. Phạm vi Phase 1

### 4.1 Có trong Phase 1

- Auth và phân quyền
- Quản lý người dùng
- Quản lý địa chỉ giao hàng
- Quản lý danh mục
- Quản lý thương hiệu
- Quản lý sản phẩm
- Quản lý biến thể sản phẩm
- Quản lý hình ảnh sản phẩm
- Tìm kiếm và lọc sản phẩm cơ bản
- Giỏ hàng
- Đặt hàng
- Thanh toán online mức demo
- Quản lý đơn hàng
- Quản lý tồn kho
- Báo cáo cơ bản
- Upload ảnh
- Logging cơ bản

### 4.2 Không có trong Phase 1

- AI chatbot
- AI size advisor
- AI semantic search
- AI recommendation
- AI demand forecasting
- Image search
- Review tóm tắt bằng AI
- Notification event-driven nâng cao
- Microservices
- Đa kênh bán hàng

## 5. Kiến trúc đề xuất

### 5.1 Hướng chốt

Phase 1 nên sử dụng `modular monolith`.

### 5.2 Stack đề xuất

- Frontend: Next.js
- Backend: Node.js + NestJS
- Database: PostgreSQL
- ORM: Prisma
- Cache: Redis
- Auth: JWT access token + refresh token
- Upload ảnh: Cloudinary
- Payment demo: VNPay sandbox
- Deployment local: Docker Compose

### 5.3 Lý do

- Dễ phát triển nhanh
- Dễ test và deploy
- Giảm độ phức tạp so với microservices
- Dễ tách thành service sau này nếu cần

## 6. Module chi tiết

### 6.1 Auth module

Chức năng:

- Đăng ký
- Đăng nhập
- Đăng xuất
- Refresh token
- Quên mật khẩu
- Đổi mật khẩu

Business rules:

- Email là duy nhất
- Password phải đủ độ mạnh tối thiểu
- Tài khoản bị khóa không được đăng nhập
- Refresh token phải có cơ chế revoke. Phase 1 lưu refresh token (dạng hash) trong bảng `refresh_tokens`; có thể dùng Redis làm cache/blacklist cho việc revoke nhanh, nhưng `refresh_tokens` ở Postgres là nguồn dữ liệu chính

### 6.2 User module

Chức năng customer:

- Xem profile
- Sửa profile
- Thêm, sửa, xóa địa chỉ
- Đặt địa chỉ mặc định

Chức năng admin:

- Xem danh sách user
- Xem chi tiết user
- Khóa/mở khóa user
- Gán role

### 6.3 Catalog module

Phân thành:

- Category
- Brand
- Product
- Product variant
- Product image

Product level:

- Tên sản phẩm
- Slug
- Mô tả ngắn
- Mô tả chi tiết
- Category
- Brand
- Giới tính
- Môn thể thao
- Trạng thái

Variant level:

- SKU
- Size
- Màu
- Giá
- Giá khuyến mãi nếu có
- Số lượng tồn
- Trạng thái

Rules:

- SKU là duy nhất
- Sản phẩm có ít nhất 1 variant mới được bán
- Giá và tồn kho quản lý ở variant
- Không xóa hard delete nếu đã phát sinh đơn, chỉ soft delete/hide

### 6.4 Search and listing module

Chức năng:

- Tìm theo tên
- Lọc theo category
- Lọc theo brand
- Lọc theo khoảng giá
- Lọc theo size
- Lọc theo màu
- Lọc theo giới tính
- Lọc theo môn thể thao
- Sắp xếp theo mới nhất, giá tăng, giá giảm
- Phân trang

### 6.5 Cart module

Chức năng:

- Xem giỏ hàng
- Thêm sản phẩm vào giỏ
- Cập nhật số lượng
- Xóa item
- Tính tạm tính

Rules:

- Mỗi item trong giỏ phải gắn với variant
- Số lượng trong giỏ không được vượt tồn khả dụng
- Khi giá thay đổi, giỏ hàng phải cập nhật lại thông tin khi checkout
- Hỗ trợ **guest cart**: khách chưa đăng nhập vẫn thêm được vào giỏ, giỏ được định danh bằng `session_id` (cookie/local session), không gắn `user_id`
- Khi guest đăng nhập hoặc đăng ký thành công, hệ thống phải **merge guest cart vào cart của user**: nếu cùng variant thì cộng dồn số lượng (giới hạn theo tồn khả dụng), nếu khác variant thì thêm mới; sau khi merge, xóa guest cart
- Bảng `carts` cần có cột `session_id` (nullable) bên cạnh `user_id` (nullable) để hỗ trợ cả 2 trường hợp

### 6.6 Order module

Chức năng customer:

- Tạo đơn hàng từ giỏ
- Xem lịch sử đơn
- Xem chi tiết đơn
- Hủy đơn nếu hợp lệ

Chức năng admin/sales:

- Xem danh sách đơn
- Lọc đơn theo trạng thái
- Tìm đơn theo mã đơn, tên, số điện thoại
- Cập nhật trạng thái đơn
- Ghi chú nội bộ

Rules:

- Mỗi đơn phải có mã đơn duy nhất
- Đơn hàng phải lưu snapshot tên sản phẩm, giá, size, màu tại thời điểm đặt
- Hủy đơn chỉ được phép khi chưa giao

### 6.7 Payment module

Chức năng:

- Chọn COD hoặc thanh toán online demo
- Tạo giao dịch thanh toán
- Nhận callback từ cổng thanh toán
- Cập nhật payment status
- Đồng bộ order status

Rules:

- Không được ghi nhận thanh toán thành công 2 lần
- `transaction_ref` phải có unique constraint ở DB level để đảm bảo idempotency, không chỉ check ở application layer
- Callback có thể bị cổng thanh toán gọi lại (retry) nhiều lần với cùng `transaction_ref`; xử lý theo kiểu idempotent — nếu đã xử lý rồi thì trả về thành công ngay mà không update lại trạng thái/order
- Callback phải verify checksum
- Lịch sử thanh toán phải được lưu

### 6.8 Inventory module

Chức năng:

- Xem tồn kho theo variant
- Nhập kho
- Xuất kho
- Điều chỉnh tồn
- Xem lịch sử biến động tồn
- Cảnh báo sắp hết hàng

Rules:

- Tồn kho được quản lý tại variant
- Mọi thay đổi tồn kho phải có transaction log
- Không cho đơn hàng vượt tồn khả dụng

### 6.9 Report module

Báo cáo tối thiểu:

- Tổng doanh thu
- Tổng số đơn
- Đơn theo trạng thái
- Sản phẩm bán chạy
- Tồn kho hiện tại
- Sản phẩm sắp hết hàng

## 7. User flow bắt buộc

### 7.1 Customer mua hàng

1. Khách vào trang chủ
2. Xem danh sách sản phẩm
3. Lọc/tìm sản phẩm
4. Xem chi tiết sản phẩm
5. Chọn variant
6. Thêm vào giỏ
7. Đăng nhập hoặc đăng ký
8. Chọn địa chỉ giao hàng
9. Chọn phương thức thanh toán
10. Tạo đơn
11. Nếu online thì chuyển qua cổng thanh toán demo
12. Quay về trang kết quả
13. Xem chi tiết đơn

### 7.2 Admin quản lý sản phẩm

1. Đăng nhập admin
2. Tạo category và brand
3. Tạo sản phẩm
4. Tạo variant
5. Upload ảnh
6. Cập nhật giá, tồn, trạng thái
7. Kiểm tra sản phẩm hiển thị bên customer

### 7.3 Sales xử lý đơn

1. Đăng nhập staff
2. Mở danh sách đơn
3. Xác nhận đơn
4. Chuyển sang đóng gói
5. Chuyển sang đang giao
6. Chuyển sang giao thành công hoặc hủy

### 7.4 Warehouse vận hành tồn

1. Đăng nhập warehouse staff
2. Mở màn hình tồn kho
3. Chọn variant
4. Nhập kho/xuất kho/điều chỉnh
5. Ghi lý do
6. Hệ thống tạo lịch sử biến động

## 8. Trạng thái nghiệp vụ

### 8.1 User status

- ACTIVE
- LOCKED

### 8.2 Product status

- DRAFT
- ACTIVE
- INACTIVE

### 8.3 Variant status

- ACTIVE
- OUT_OF_STOCK
- INACTIVE

### 8.4 Order status

- PENDING_CONFIRMATION
- CONFIRMED
- PACKING
- SHIPPING
- DELIVERED
- CANCELLED

### 8.5 Payment status

- UNPAID
- PENDING
- PAID
- FAILED
- CANCELLED
- REFUNDED

### 8.6 Inventory transaction type

- IMPORT
- EXPORT
- ADJUSTMENT_UP
- ADJUSTMENT_DOWN
- ORDER_RESERVE
- ORDER_RELEASE

## 9. Quy tắc nghiệp vụ cần chốt

### 9.1 Sản phẩm

- Product là lớp mô tả chung
- Variant mới là thứ được bán
- Giá, SKU, tồn kho nằm ở variant
- Ảnh có thể gắn product hoặc variant, nhưng Phase 1 có thể ưu tiên product image

### 9.2 Tồn kho

- Tồn khả dụng = `stock_quantity - reserved_quantity`
- Cơ chế chốt cho Phase 1 là **reserve khi tạo đơn, trừ thật khi xác nhận**, khớp với `reserved_quantity` ở bảng `product_variants` và `ORDER_RESERVE`/`ORDER_RELEASE` ở mục 8.6:
  1. Khi đơn được tạo (`PENDING_CONFIRMATION`): tăng `reserved_quantity` theo số lượng đặt, ghi `inventory_transactions` với type `ORDER_RESERVE`
  2. Khi đơn được xác nhận (`CONFIRMED`): giảm `stock_quantity` và `reserved_quantity` tương ứng (trừ thật)
  3. Khi đơn bị hủy (`CANCELLED`) ở trạng thái còn đang giữ chỗ: giảm `reserved_quantity`, ghi `inventory_transactions` với type `ORDER_RELEASE`
  4. Tồn khả dụng dùng để validate khi thêm giỏ hàng và khi tạo đơn là `stock_quantity - reserved_quantity`
- Mọi thay đổi `stock_quantity`/`reserved_quantity` đều phải nằm trong 1 DB transaction và phải có dòng tương ứng trong `inventory_transactions`
- Để tránh race condition khi nhiều người mua cùng variant cùng lúc, bắt buộc dùng row-level locking (`SELECT ... FOR UPDATE` trong transaction) hoặc optimistic locking bằng cột `version` khi cập nhật tồn kho — không chỉ kiểm tra ở application layer

### 9.3 Đơn hàng

- Customer không được xem đơn của người khác
- Hủy đơn chỉ hợp lệ khi chưa SHIPPING
- Order item lưu snapshot sản phẩm

### 9.4 Thanh toán

- COD được hỗ trợ trong Phase 1
- Online payment là demo, không cần hoàn tiền tự động trong Phase 1

## 10. Mô hình dữ liệu đề xuất

### 10.1 Bảng chính

- users
- refresh_tokens
- addresses
- categories
- brands
- products
- product_variants
- product_images
- carts
- cart_items
- orders
- order_items
- payments
- inventory_transactions

### 10.2 Cột quan trọng

users:

- id
- full_name
- email
- phone
- password_hash
- role _(enum: CUSTOMER, SALES_STAFF, WAREHOUSE_STAFF, ADMIN — Phase 1 chỉ có 4 role cố định nên dùng enum trên `users` thay vì bảng `roles`/`user_roles` many-to-many; mô hình many-to-many để dành khi cần multi-role hoặc permission động ở Phase sau)_
- status
- created_at

refresh_tokens:

- id
- user_id
- token_hash
- expires_at
- revoked_at
- created_at

addresses:

- id
- user_id
- receiver_name
- phone
- province
- district
- ward
- address_line
- is_default

products:

- id
- name
- slug
- short_description
- description
- category_id
- brand_id
- gender
- sport_type
- status

product_variants:

- id
- product_id
- sku
- size
- color
- price
- compare_at_price
- stock_quantity
- reserved_quantity
- status

orders:

- id
- order_code
- user_id
- address_snapshot_json
- subtotal_amount
- shipping_fee
- discount_amount
- total_amount
- payment_method
- order_status
- payment_status
- note
- created_at

order_items:

- id
- order_id
- product_id
- variant_id
- product_name_snapshot
- sku_snapshot
- size_snapshot
- color_snapshot
- unit_price_snapshot
- quantity
- line_total

payments:

- id
- order_id
- provider
- transaction_ref
- amount
- status
- raw_payload_json
- paid_at

inventory_transactions:

- id
- variant_id
- type
- quantity
- before_quantity
- after_quantity
- note
- created_by
- created_at

## 11. API scope bắt buộc

### 11.1 Auth APIs

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`

### 11.2 User APIs

- `GET /api/v1/me`
- `PATCH /api/v1/me`
- `GET /api/v1/me/addresses`
- `POST /api/v1/me/addresses`
- `PATCH /api/v1/me/addresses/:id`
- `DELETE /api/v1/me/addresses/:id`

### 11.3 Catalog APIs

- `GET /api/v1/categories`
- `GET /api/v1/brands`
- `GET /api/v1/products`
- `GET /api/v1/products/:slugOrId`

Admin:

- `POST /api/v1/admin/categories`
- `PATCH /api/v1/admin/categories/:id`
- `POST /api/v1/admin/brands`
- `PATCH /api/v1/admin/brands/:id`
- `POST /api/v1/admin/products`
- `PATCH /api/v1/admin/products/:id`
- `POST /api/v1/admin/products/:id/variants`
- `PATCH /api/v1/admin/variants/:id`
- `POST /api/v1/admin/products/:id/images`

### 11.4 Cart APIs

- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `PATCH /api/v1/cart/items/:id`
- `DELETE /api/v1/cart/items/:id`

### 11.5 Order APIs

- `POST /api/v1/orders`
- `GET /api/v1/orders/me`
- `GET /api/v1/orders/:id`
- `POST /api/v1/orders/:id/cancel`

Admin/Sales:

- `GET /api/v1/admin/orders`
- `GET /api/v1/admin/orders/:id`
- `PATCH /api/v1/admin/orders/:id/status`

### 11.6 Payment APIs

- `POST /api/v1/payments/create`
- `POST /api/v1/payments/callback`
- `GET /api/v1/payments/:orderId`

### 11.7 Inventory APIs

- `GET /api/v1/admin/inventory`
- `POST /api/v1/admin/inventory/import`
- `POST /api/v1/admin/inventory/export`
- `POST /api/v1/admin/inventory/adjust`
- `GET /api/v1/admin/inventory/transactions`

### 11.8 Report APIs

- `GET /api/v1/admin/reports/overview`
- `GET /api/v1/admin/reports/orders`
- `GET /api/v1/admin/reports/products`
- `GET /api/v1/admin/reports/inventory`

## 12. Response và error convention

### 12.1 Success response

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "meta": {}
}
```

### 12.2 Error response

```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    {
      "field": "quantity",
      "message": "Quantity exceeds available stock"
    }
  ]
}
```

## 13. Màn hình frontend bắt buộc

### 13.1 Customer app

- Home page
- Product listing page
- Product detail page
- Cart page
- Checkout page
- Login page
- Register page
- Profile page
- Address management page
- Order history page
- Order detail page
- Payment result page

### 13.2 Thành phần UI bắt buộc

- Header
- Search bar
- Category menu
- Footer
- Product card
- Filter sidebar
- Pagination
- Toast/alert
- Empty state
- Loading state

## 14. Màn hình admin bắt buộc

- Admin login
- Dashboard
- Category management
- Brand management
- Product management
- Variant management
- Inventory management
- Order management
- User management
- Report overview

## 15. Phân quyền

### 15.1 CUSTOMER

- Thao tác trên tài khoản của mình
- Xem sản phẩm
- Quản lý giỏ hàng
- Tạo và xem đơn của mình

### 15.2 SALES_STAFF

- Xem danh sách đơn
- Xem chi tiết đơn
- Cập nhật order status

### 15.3 WAREHOUSE_STAFF

- Xem tồn kho
- Tạo inventory transaction

### 15.4 ADMIN

- Toàn quyền

## 16. Validation quan trọng

- Email đúng format
- Password tối thiểu 8 ký tự
- Price > 0
- Quantity > 0 với import/export
- Category và brand phải tồn tại khi tạo product
- Variant phải có SKU, size, color, price
- Không tạo order nếu giỏ hàng rỗng
- Không thanh toán order đã PAID

## 17. Logging và audit tối thiểu

Bắt buộc log:

- Đăng nhập thất bại
- Đăng nhập thành công
- Tạo đơn hàng
- Hủy đơn hàng
- Callback payment
- Điều chỉnh tồn kho
- Hành động admin quan trọng

## 18. Bảo mật tối thiểu

- Password hash bằng bcrypt/argon2
- JWT access token ngắn hạn
- Refresh token có revoke
- Route protection theo role
- Validate input server-side
- Sanitize dữ liệu đầu vào cần thiết
- Rate limit cho login nếu có thể

## 19. Hiệu năng tối thiểu

- Danh sách sản phẩm phải phân trang
- Tìm kiếm có index cho name/slug
- Trang chi tiết sản phẩm phải tải nhanh với dữ liệu đã tối ưu
- Dashboard report có thể dùng truy vấn tổng hợp có index

## 20. Deploy và vận hành

### 20.1 Local setup

- frontend
- backend
- postgres
- redis

Chạy bằng Docker Compose hoặc script rõ ràng.

### 20.2 Seed data

Phải có dữ liệu demo tối thiểu:

- 1 admin
- 1 sales staff
- 1 warehouse staff
- 10 categories/brands cơ bản
- 20-30 products
- 50+ variants
- Một vài đơn hàng demo

## 21. Testing scope

### 21.1 Backend

Bắt buộc test:

- Đăng ký/đăng nhập
- Thêm vào giỏ
- Tạo đơn
- Callback payment
- Cập nhật order status
- Inventory transaction

### 21.2 Frontend

Bắt buộc test tay:

- Luồng mua hàng end-to-end
- Luồng admin tạo sản phẩm
- Luồng sales xử lý đơn
- Luồng warehouse cập nhật tồn

## 22. Tiêu chí nghiệm thu

Phase 1 được nghiệm thu khi tất cả điều kiện sau đúng:

- Customer đăng ký, đăng nhập, đặt hàng được
- Giỏ hàng và checkout ổn định
- COD và online payment demo đều chạy
- Admin tạo/sửa/ẩn sản phẩm được
- Admin quản lý variant và tồn kho được
- Staff xử lý đơn được
- Báo cáo overview hiển thị đúng
- Không có lỗi phân quyền nghiêm trọng
- Database dữ liệu nhất quán
- Có hướng dẫn cài đặt

## 23. Thứ tự implementation khuyến nghị

1. Auth và roles
2. Categories, brands
3. Products, variants, images
4. Product listing và detail
5. Cart
6. Orders
7. Payments
8. Inventory
9. Admin dashboard và reports
10. Hardening, seed, test, docs

## 24. Ranh giới sang Phase 2

Chỉ sau khi Phase 1 đã ổn định mới làm:

- Chatbot tư vấn
- Size advisor
- Semantic search
- Recommendation
- Forecasting
- Notification nâng cao

Nếu chưa có Phase 1 chạy ổn định, không được chèn AI vào giữa.
