# TASK BREAKDOWN PHASE 1

## 1. Mục tiêu

Tài liệu này chuyển toàn bộ thiết kế Phase 1 thành danh sách công việc triển khai thực tế.

Mục tiêu:

- Chia nhỏ công việc theo module
- Xác định thứ tự thực hiện
- Chỉ rõ dependency giữa các phần
- Giúp bắt đầu code mà không bị lan man

## 2. Nguyên tắc triển khai

- Làm từ nền tảng hệ thống trước, rồi đến module nghiệp vụ
- Chốt backend core trước khi làm giao diện phức tạp
- Ưu tiên luồng end-to-end chạy được sớm
- Không chen AI vào Phase 1
- Mỗi module phải có API, test và dữ liệu demo tối thiểu

## 3. Thứ tự triển khai tổng thể

1. Khởi tạo project và môi trường
2. Database, Prisma, migration
3. Auth và user
4. Category, brand, product, variant, image
5. Product listing và product detail
6. Cart và guest cart
7. Order
8. Payment
9. Inventory
10. Admin
11. Report
12. Forgot password
13. Seed data
14. Test end-to-end
15. Hardening và tài liệu

## 4. Phase A - Khởi tạo nền tảng

### A1. Khởi tạo repository backend

- Tạo project NestJS
- Thiết lập cấu trúc module theo `modular monolith`
- Tạo thư mục:
  - `src/modules`
  - `src/common`
  - `src/config`
  - `src/database`
  - `src/lib`

### A2. Thiết lập môi trường

- Tạo `.env.example`
- Khai báo:
  - `DATABASE_URL`
  - `REDIS_URL`
  - `JWT_ACCESS_SECRET`
  - `JWT_REFRESH_SECRET`
  - `CLOUDINARY_URL`
  - `MAIL_FROM`
  - `SMTP_HOST`
  - `SMTP_PORT`
  - `SMTP_USER`
  - `SMTP_PASS`
  - `VNPAY_*`

### A3. Docker Compose

- Tạo `docker-compose.yml`
- Chạy:
  - postgres
  - redis
- Thêm volume và healthcheck cơ bản

### A4. Thiết lập common foundation

- Global exception filter
- Validation pipe
- Response interceptor
- Config module
- Logger cơ bản
- Guard xác thực JWT
- Guard phân quyền role

## 5. Phase B - Database và Prisma

### B1. Khởi tạo Prisma

- Cài Prisma
- Tạo `schema.prisma`
- Khai báo datasource và generator

### B2. Viết schema theo spec

- Thêm enum
- Thêm models:
  - User
  - RefreshToken
  - PasswordResetToken
  - Address
  - Category
  - Brand
  - Product
  - ProductVariant
  - ProductImage
  - Cart
  - CartItem
  - Order
  - OrderItem
  - Payment
  - InventoryTransaction

### B3. Migration đầu tiên

- Generate migration
- Kiểm tra bảng và quan hệ

### B4. SQL migration bổ sung

- Partial unique index cho `carts.user_id`
- Partial unique index cho `carts.session_id`
- Check constraint cho:
  - stock_quantity
  - reserved_quantity
  - quantity
  - amount

### B5. Prisma service

- Tạo PrismaModule
- Tạo PrismaService
- Kết nối shutdown hook

## 6. Phase C - Auth và User

### C1. Auth module

- Register
- Login
- Refresh token
- Logout
- Password hashing
- Refresh token hashing
- Save refresh token
- Revoke refresh token

### C2. JWT strategy

- Access token strategy
- Guard cho route private

### C3. Role guard

- Decorator role
- Guard kiểm tra role

### C4. User profile

- `GET /me`
- `PATCH /me`

### C5. Address module

- List addresses
- Create address
- Update address
- Delete address
- Set default address logic

### C6. Admin user management

- List users
- View user detail
- Lock or unlock user
- Change role

### C7. Test auth và user

- Register success
- Login success
- Wrong password
- Locked account
- Refresh success
- Logout success
- CRUD address

## 7. Phase D - Category, Brand, Product, Variant, Image

### D1. Category module

- Create category
- Update category
- List categories
- Disable category

### D2. Brand module

- Create brand
- Update brand
- List brands
- Disable brand

### D3. Product module

- Create product
- Update product
- Get product detail admin
- Hide product

### D4. Variant module

- Create variant
- Update variant
- Validate unique SKU
- Validate stock and price

### D5. Product image module

- Upload image metadata
- Mark primary image
- Sort images
- Delete image

### D6. Public catalog read APIs

- Product listing
- Product detail
- Filter and sort
- Pagination

### D7. Test catalog

- Create product flow
- Product with variants
- Filter by category/brand/price
- Product detail response đúng structure

## 8. Phase E - Customer storefront read flow

### E1. Home page data APIs

- Featured products
- New products
- Categories overview nếu cần

### E2. Product listing page integration

- Search
- Filter
- Sort
- Pagination

### E3. Product detail page integration

- Product info
- Images
- Variants
- Available stock

## 9. Phase F - Cart và Guest Cart

### F1. Cart persistence design

- Nếu login: dùng `user_id`
- Nếu guest: dùng `session_id`

### F2. Cart APIs

- Get cart
- Add item
- Update item quantity
- Delete item

### F3. Guest cart logic

- Đọc `session_id` từ cookie
- Tạo cart nếu chưa có
- Duy trì cart theo session

### F4. Merge cart sau login/register

- Merge guest cart vào user cart
- Cộng dồn quantity nếu cùng variant
- Không vượt tồn khả dụng
- Xóa guest cart sau merge

### F5. Cart pricing validation

- Recalculate tại thời điểm checkout
- Báo lỗi nếu item không còn đủ tồn

### F6. Test cart

- Guest add cart
- Guest update cart
- Login merge cart
- User cart fetch
- Quantity over stock

## 10. Phase G - Order

### G1. Order creation service

- Validate user
- Validate address
- Validate cart không rỗng
- Validate stock khả dụng
- Snapshot order items
- Tính subtotal
- Tính shipping fee
- Tính total
- Tạo order

### G2. Reserve stock khi tạo order

- Tăng `reserved_quantity`
- Ghi `inventory_transactions` type `ORDER_RESERVE`
- Dùng DB transaction
- Dùng row locking hoặc optimistic locking

### G3. Order read APIs

- List my orders
- Order detail

### G4. Customer cancel order

- Chỉ cho phép khi chưa SHIPPING
- Nếu còn reserve thì release tồn

### G5. Admin/sales order APIs

- List orders
- Filter orders
- Search by code, name, phone
- Update status
- Internal note

### G6. Test order

- Create order success
- Empty cart fail
- Insufficient stock fail
- Cancel before shipping success
- Cancel after shipping fail

## 11. Phase H - Payment

### H1. Payment entity logic

- Tạo payment record
- Sinh `transaction_ref`
- Unique constraint

### H2. VNPay integration

- Build payment URL
- Save pending transaction

### H3. Payment callback

- Receive callback payload
- Verify checksum
- Find payment by `transaction_ref`
- Idempotent xử lý callback
- Update payment status
- Update order payment status

### H4. Payment query API

- Get payments by order

### H5. Test payment

- Create payment URL
- Callback success
- Callback fail
- Duplicate callback idempotent

## 12. Phase I - Inventory

### I1. Inventory read API

- List inventory by variant
- Filter inventory

### I2. Manual import

- Increase stock
- Write inventory transaction

### I3. Manual export

- Decrease stock
- Validate đủ stock
- Write inventory transaction

### I4. Manual adjustment

- Adjustment up
- Adjustment down
- Write inventory transaction

### I5. Confirm order deduct flow

- Khi order chuyển `CONFIRMED`
- Giảm `stock_quantity`
- Giảm `reserved_quantity`
- Ghi `ORDER_CONFIRM_DEDUCT`

### I6. Cancel order release flow

- Khi order `CANCELLED` và còn reserve
- Giảm `reserved_quantity`
- Ghi `ORDER_RELEASE`

### I7. Inventory history API

- List inventory transactions
- Filter by variant, type, date

### I8. Test inventory

- Import success
- Export success
- Export insufficient stock fail
- Reserve stock success
- Confirm deduct success
- Cancel release success

## 13. Phase J - Admin

### J1. Admin auth flow

- Login admin
- Protect admin routes

### J2. Admin product management pages

- Category management
- Brand management
- Product management
- Variant management
- Image management

### J3. Admin order management pages

- Orders list
- Order detail
- Status update

### J4. Admin inventory pages

- Inventory list
- Inventory transaction create
- Inventory history

### J5. Admin user management pages

- Users list
- User detail
- Lock/unlock
- Role change

## 14. Phase K - Report

### K1. Report overview API

- Gross revenue
- Realized revenue
- Total orders
- Pending orders
- Low stock count

### K2. Order report API

- Orders by status
- Orders by time range

### K3. Product report API

- Best selling products
- Slow moving products nếu muốn

### K4. Inventory report API

- Current inventory
- Low stock

### K5. Test report

- Revenue numbers đúng rule
- Report query không lỗi

## 15. Phase L - Forgot Password

### L1. Mail service

- Tạo mail provider abstraction
- Gửi mail reset password

### L2. Forgot password API

- Generate token
- Hash token
- Save DB
- Send email
- Return generic success message

### L3. Reset password API

- Validate token
- Check expiration
- Check used_at
- Update password
- Mark token used

### L4. Test forgot password

- Request reset success
- Invalid token fail
- Expired token fail
- Reuse token fail

## 16. Phase M - Seed data

### M1. Seed users

- 1 admin
- 1 sales staff
- 1 warehouse staff
- 2-3 customer demo

### M2. Seed catalog

- categories
- brands
- products
- variants
- product images

### M3. Seed orders

- vài đơn ở nhiều trạng thái

### M4. Seed inventory transactions

- import cơ bản

## 17. Phase N - Frontend customer

### N1. Public pages

- Home
- Product listing
- Product detail

### N2. Auth pages

- Register
- Login
- Forgot password
- Reset password

### N3. Cart pages

- Cart
- Checkout

### N4. Customer account pages

- Profile
- Address management
- Order history
- Order detail

### N5. Payment result page

- Success
- Failed
- Cancelled

## 18. Phase O - Frontend admin

### O1. Admin layout

- Sidebar
- Header
- Route guard

### O2. Dashboard

- Overview cards
- Revenue summary

### O3. CRUD pages

- Category
- Brand
- Product
- Variant
- Inventory
- Orders
- Users

## 19. Phase P - Testing

### P1. Backend automated tests

- Auth
- Cart
- Order
- Payment callback
- Inventory
- Forgot password

### P2. Frontend manual test checklist

- Guest browsing
- Guest cart
- Login merge cart
- Checkout COD
- Checkout VNPAY demo
- Order history
- Admin create product
- Staff confirm order
- Warehouse adjust inventory

### P3. Data integrity test

- No negative stock
- No duplicate SKU
- No duplicate transaction_ref
- Reserved flow đúng

## 20. Phase Q - Hardening

### Q1. Validation hardening

- DTO validation
- Enum validation
- Number range validation

### Q2. Security hardening

- Rate limit login
- Rate limit forgot password
- Secure JWT secrets
- Sanitize inputs cần thiết

### Q3. Logging

- Auth logs
- Order logs
- Payment logs
- Inventory logs

### Q4. Error handling

- Standard error response
- Business exception mapping

## 21. Phase R - Docs

### R1. README backend

- Cài đặt
- Chạy local
- Migration
- Seed
- Test

### R2. API documentation

- Swagger nếu dùng
- Hoặc Postman collection

### R3. Deployment note

- Env vars
- Docker compose
- Local demo flow

## 22. Milestone đề xuất

### Milestone 1

- Project setup
- Prisma schema
- Auth
- User

### Milestone 2

- Category
- Brand
- Product
- Variant
- Image
- Public product APIs

### Milestone 3

- Cart
- Guest cart
- Order

### Milestone 4

- Payment
- Inventory
- Admin order flow

### Milestone 5

- Report
- Forgot password
- Seed
- Test
- Docs

## 23. Definition of Done cho mỗi module

Một module chỉ được xem là xong khi có đủ:

- DB schema hoặc model liên quan
- Service logic
- API/controller
- Validation
- Error handling
- Test tối thiểu
- Seed hoặc dữ liệu demo nếu cần

## 24. Công việc nên làm ngay bây giờ

1. Tạo backend project
2. Viết `schema.prisma`
3. Tạo migration đầu tiên
4. Làm auth
5. Làm category, brand, product, variant

## 25. Kết luận

File này là backlog triển khai thực tế cho toàn bộ Phase 1.

Sau file này, bạn có thể bắt đầu theo 2 hướng:

1. Tôi viết tiếp `schema.prisma` skeleton để bạn code backend ngay
2. Tôi viết tiếp `README_IMPLEMENTATION_ORDER.md` dạng checklist ngắn gọn để đội dev bám theo từng ngày
