# ERD PHASE 1

## 1. Mục tiêu

Tài liệu này chốt mô hình dữ liệu cho Phase 1 của sản phẩm bán trang phục thể thao không AI.

Mục tiêu:

- Xác định đầy đủ các bảng chính
- Chốt khóa chính, khóa ngoại, unique constraint
- Chốt quan hệ 1-1, 1-n, n-1
- Chốt các cột quan trọng phục vụ nghiệp vụ
- Làm nền để triển khai Prisma schema hoặc SQL schema

## 2. Nguyên tắc thiết kế dữ liệu

- Mỗi user chỉ có 1 role tại một thời điểm
- Sản phẩm bán ở cấp `product_variant`, không bán trực tiếp ở `product`
- Giá và tồn kho nằm ở `product_variants`
- Đơn hàng phải lưu snapshot dữ liệu sản phẩm tại thời điểm mua
- Tồn kho dùng mô hình `stock_quantity` và `reserved_quantity`
- Mọi thay đổi tồn kho phải có lịch sử trong `inventory_transactions`
- Payment callback phải hỗ trợ idempotency ở DB level
- Guest cart và user cart cùng dùng bảng `carts`

## 3. Danh sách bảng

- users
- refresh_tokens
- password_reset_tokens
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

## 4. Quan hệ tổng quát

- `users` 1-n `refresh_tokens`
- `users` 1-n `password_reset_tokens`
- `users` 1-n `addresses`
- `users` 1-1 `carts` với user cart
- `categories` 1-n `products`
- `brands` 1-n `products`
- `products` 1-n `product_variants`
- `products` 1-n `product_images`
- `carts` 1-n `cart_items`
- `users` 1-n `orders`
- `orders` 1-n `order_items`
- `orders` 1-n `payments`
- `product_variants` 1-n `cart_items`
- `product_variants` 1-n `order_items`
- `product_variants` 1-n `inventory_transactions`
- `orders` 1-n `inventory_transactions` trong các transaction liên quan reserve/release/confirm

## 5. ERD logic dạng chữ

```text
users
  ├── refresh_tokens
  ├── password_reset_tokens
  ├── addresses
  ├── carts
  └── orders

categories
  └── products

brands
  └── products

products
  ├── product_variants
  └── product_images

carts
  └── cart_items

orders
  ├── order_items
  ├── payments
  └── inventory_transactions

product_variants
  ├── cart_items
  ├── order_items
  └── inventory_transactions
```

## 6. Định nghĩa chi tiết từng bảng

### 6.1 users

Mục đích:

- Lưu tài khoản hệ thống cho customer, sales staff, warehouse staff, admin

Cột chính:

- `id` bigint hoặc uuid, PK
- `full_name` varchar(150), not null
- `email` varchar(255), not null, unique
- `phone` varchar(20), nullable
- `password_hash` varchar(255), not null
- `role` enum, not null
- `status` enum, not null, default `ACTIVE`
- `last_login_at` timestamp, nullable
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Enum đề xuất:

- `role`: `CUSTOMER`, `SALES_STAFF`, `WAREHOUSE_STAFF`, `ADMIN`
- `status`: `ACTIVE`, `LOCKED`

Constraint:

- unique(`email`)
- check email normalized lowercase ở application layer hoặc DB

Quan hệ:

- 1 user có nhiều `refresh_tokens`
- 1 user có nhiều `password_reset_tokens`
- 1 user có nhiều `addresses`
- 1 user có nhiều `orders`
- 1 user có tối đa 1 `cart` active dạng user cart

### 6.2 refresh_tokens

Mục đích:

- Lưu refresh token dạng hash để hỗ trợ logout, revoke, rotation

Cột chính:

- `id` bigint hoặc uuid, PK
- `user_id` FK -> `users.id`, not null
- `token_hash` varchar(255), not null, unique
- `expires_at` timestamp, not null
- `revoked_at` timestamp, nullable
- `created_at` timestamp, not null

Constraint:

- unique(`token_hash`)

Quan hệ:

- n-1 với `users`

### 6.3 password_reset_tokens

Mục đích:

- Lưu token reset password dạng hash

Cột chính:

- `id` bigint hoặc uuid, PK
- `user_id` FK -> `users.id`, not null
- `token_hash` varchar(255), not null, unique
- `expires_at` timestamp, not null
- `used_at` timestamp, nullable
- `created_at` timestamp, not null

Constraint:

- unique(`token_hash`)

Quan hệ:

- n-1 với `users`

### 6.4 addresses

Mục đích:

- Lưu địa chỉ giao hàng của user

Cột chính:

- `id` bigint hoặc uuid, PK
- `user_id` FK -> `users.id`, not null
- `receiver_name` varchar(150), not null
- `phone` varchar(20), not null
- `province` varchar(100), not null
- `district` varchar(100), not null
- `ward` varchar(100), not null
- `address_line` varchar(255), not null
- `is_default` boolean, not null, default false
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Quan hệ:

- n-1 với `users`

Quy tắc:

- mỗi user có thể có nhiều địa chỉ
- mỗi user chỉ nên có 1 địa chỉ mặc định tại một thời điểm

### 6.5 categories

Mục đích:

- Lưu danh mục sản phẩm

Cột chính:

- `id` bigint hoặc uuid, PK
- `name` varchar(150), not null
- `slug` varchar(180), not null, unique
- `description` text, nullable
- `status` enum, not null, default `ACTIVE`
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Enum đề xuất:

- `status`: `ACTIVE`, `INACTIVE`

Quan hệ:

- 1-n với `products`

### 6.6 brands

Mục đích:

- Lưu thương hiệu

Cột chính:

- `id` bigint hoặc uuid, PK
- `name` varchar(150), not null
- `slug` varchar(180), not null, unique
- `description` text, nullable
- `status` enum, not null, default `ACTIVE`
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Quan hệ:

- 1-n với `products`

### 6.7 products

Mục đích:

- Lưu lớp mô tả chung của sản phẩm

Cột chính:

- `id` bigint hoặc uuid, PK
- `category_id` FK -> `categories.id`, not null
- `brand_id` FK -> `brands.id`, not null
- `name` varchar(255), not null
- `slug` varchar(300), not null, unique
- `short_description` varchar(500), nullable
- `description` text, nullable
- `gender` enum, nullable
- `sport_type` enum hoặc varchar(50), nullable
- `status` enum, not null, default `DRAFT`
- `is_featured` boolean, not null, default false
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Enum đề xuất:

- `gender`: `MEN`, `WOMEN`, `UNISEX`, `KIDS`
- `status`: `DRAFT`, `ACTIVE`, `INACTIVE`

Constraint:

- unique(`slug`)

Quan hệ:

- n-1 với `categories`
- n-1 với `brands`
- 1-n với `product_variants`
- 1-n với `product_images`

### 6.8 product_variants

Mục đích:

- Lưu biến thể bán hàng thật của sản phẩm

Cột chính:

- `id` bigint hoặc uuid, PK
- `product_id` FK -> `products.id`, not null
- `sku` varchar(100), not null, unique
- `size` varchar(50), not null
- `color` varchar(50), not null
- `price` decimal(12,2), not null
- `compare_at_price` decimal(12,2), nullable
- `stock_quantity` integer, not null, default 0
- `reserved_quantity` integer, not null, default 0
- `version` integer, not null, default 1
- `status` enum, not null, default `ACTIVE`
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Enum đề xuất:

- `status`: `ACTIVE`, `OUT_OF_STOCK`, `INACTIVE`

Constraint:

- unique(`sku`)
- check(`price > 0`)
- check(`stock_quantity >= 0`)
- check(`reserved_quantity >= 0`)
- check(`reserved_quantity <= stock_quantity`)

Quan hệ:

- n-1 với `products`
- 1-n với `cart_items`
- 1-n với `order_items`
- 1-n với `inventory_transactions`

Quy tắc:

- tồn khả dụng = `stock_quantity - reserved_quantity`
- không nên xóa variant nếu đã có order

### 6.9 product_images

Mục đích:

- Lưu hình ảnh sản phẩm

Cột chính:

- `id` bigint hoặc uuid, PK
- `product_id` FK -> `products.id`, not null
- `image_url` varchar(500), not null
- `public_id` varchar(255), nullable
- `alt_text` varchar(255), nullable
- `sort_order` integer, not null, default 0
- `is_primary` boolean, not null, default false
- `created_at` timestamp, not null

Quan hệ:

- n-1 với `products`

Quy tắc:

- mỗi product nên có tối đa 1 ảnh primary

### 6.10 carts

Mục đích:

- Lưu giỏ hàng cho cả guest và user đăng nhập

Cột chính:

- `id` bigint hoặc uuid, PK
- `user_id` FK -> `users.id`, nullable
- `session_id` varchar(255), nullable
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Constraint:

- unique(`user_id`) khi `user_id` không null
- unique(`session_id`) khi `session_id` không null
- check không cho cả `user_id` và `session_id` cùng null

Quan hệ:

- n-1 với `users` cho user cart
- 1-n với `cart_items`

Quy tắc:

- guest cart dùng `session_id`
- user cart dùng `user_id`
- khi login thành công phải merge guest cart vào user cart

### 6.11 cart_items

Mục đích:

- Lưu các item trong giỏ hàng

Cột chính:

- `id` bigint hoặc uuid, PK
- `cart_id` FK -> `carts.id`, not null
- `variant_id` FK -> `product_variants.id`, not null
- `quantity` integer, not null
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Constraint:

- check(`quantity > 0`)
- unique(`cart_id`, `variant_id`)

Quan hệ:

- n-1 với `carts`
- n-1 với `product_variants`

### 6.12 orders

Mục đích:

- Lưu thông tin đơn hàng

Cột chính:

- `id` bigint hoặc uuid, PK
- `order_code` varchar(50), not null, unique
- `user_id` FK -> `users.id`, not null
- `address_snapshot_json` jsonb, not null
- `subtotal_amount` decimal(12,2), not null
- `shipping_fee` decimal(12,2), not null, default 0
- `discount_amount` decimal(12,2), not null, default 0
- `total_amount` decimal(12,2), not null
- `payment_method` enum, not null
- `order_status` enum, not null, default `PENDING_CONFIRMATION`
- `payment_status` enum, not null, default `UNPAID`
- `note` text, nullable
- `internal_note` text, nullable
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Enum đề xuất:

- `payment_method`: `COD`, `VNPAY`
- `order_status`: `PENDING_CONFIRMATION`, `CONFIRMED`, `PACKING`, `SHIPPING`, `DELIVERED`, `CANCELLED`
- `payment_status`: `UNPAID`, `PENDING`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED`

Constraint:

- unique(`order_code`)
- check(`subtotal_amount >= 0`)
- check(`shipping_fee >= 0`)
- check(`discount_amount >= 0`)
- check(`total_amount >= 0`)

Quan hệ:

- n-1 với `users`
- 1-n với `order_items`
- 1-n với `payments`
- 1-n với `inventory_transactions`

### 6.13 order_items

Mục đích:

- Lưu chi tiết đơn và snapshot dữ liệu sản phẩm

Cột chính:

- `id` bigint hoặc uuid, PK
- `order_id` FK -> `orders.id`, not null
- `product_id` FK -> `products.id`, not null
- `variant_id` FK -> `product_variants.id`, not null
- `product_name_snapshot` varchar(255), not null
- `sku_snapshot` varchar(100), not null
- `size_snapshot` varchar(50), not null
- `color_snapshot` varchar(50), not null
- `unit_price_snapshot` decimal(12,2), not null
- `quantity` integer, not null
- `line_total` decimal(12,2), not null

Constraint:

- check(`quantity > 0`)
- check(`unit_price_snapshot >= 0`)
- check(`line_total >= 0`)

Quan hệ:

- n-1 với `orders`
- n-1 với `products`
- n-1 với `product_variants`

### 6.14 payments

Mục đích:

- Lưu giao dịch thanh toán

Cột chính:

- `id` bigint hoặc uuid, PK
- `order_id` FK -> `orders.id`, not null
- `provider` enum, not null
- `transaction_ref` varchar(150), not null, unique
- `amount` decimal(12,2), not null
- `status` enum, not null
- `raw_payload_json` jsonb, nullable
- `paid_at` timestamp, nullable
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

Enum đề xuất:

- `provider`: `COD`, `VNPAY`
- `status`: `PENDING`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED`

Constraint:

- unique(`transaction_ref`)
- check(`amount >= 0`)

Quan hệ:

- n-1 với `orders`

Quy tắc:

- `transaction_ref` unique để đảm bảo idempotency
- một order có thể có nhiều bản ghi payment theo lịch sử retry hoặc tạo lại giao dịch

### 6.15 inventory_transactions

Mục đích:

- Lưu toàn bộ lịch sử thay đổi tồn kho và reserved kho

Cột chính:

- `id` bigint hoặc uuid, PK
- `variant_id` FK -> `product_variants.id`, not null
- `order_id` FK -> `orders.id`, nullable
- `type` enum, not null
- `quantity` integer, not null
- `before_stock_quantity` integer, not null
- `after_stock_quantity` integer, not null
- `before_reserved_quantity` integer, not null
- `after_reserved_quantity` integer, not null
- `note` text, nullable
- `created_by` FK -> `users.id`, nullable
- `created_at` timestamp, not null

Enum đề xuất:

- `type`: `IMPORT`, `EXPORT`, `ADJUSTMENT_UP`, `ADJUSTMENT_DOWN`, `ORDER_RESERVE`, `ORDER_RELEASE`, `ORDER_CONFIRM_DEDUCT`

Constraint:

- check(`quantity > 0`)
- check(`before_stock_quantity >= 0`)
- check(`after_stock_quantity >= 0`)
- check(`before_reserved_quantity >= 0`)
- check(`after_reserved_quantity >= 0`)

Quan hệ:

- n-1 với `product_variants`
- n-1 với `orders`
- n-1 với `users` qua `created_by`

## 7. Cardinality chi tiết

- Một `category` có nhiều `products`, một `product` thuộc đúng một `category`
- Một `brand` có nhiều `products`, một `product` thuộc đúng một `brand`
- Một `product` có nhiều `product_variants`, một `variant` thuộc đúng một `product`
- Một `product` có nhiều `product_images`, một `image` thuộc đúng một `product`
- Một `cart` có nhiều `cart_items`, một `cart_item` thuộc đúng một `cart`
- Một `variant` có nhiều `cart_items`, một `cart_item` trỏ đúng một `variant`
- Một `order` có nhiều `order_items`, một `order_item` thuộc đúng một `order`
- Một `order` có nhiều `payments`, một `payment` thuộc đúng một `order`
- Một `variant` có nhiều `inventory_transactions`, một `inventory_transaction` thuộc đúng một `variant`

## 8. Constraint quan trọng nên có ở DB

- `users.email` unique
- `categories.slug` unique
- `brands.slug` unique
- `products.slug` unique
- `product_variants.sku` unique
- `payments.transaction_ref` unique
- `cart_items (cart_id, variant_id)` unique
- partial unique cho `carts.user_id`
- partial unique cho `carts.session_id`

Nếu dùng PostgreSQL:

- nên dùng partial unique index cho `carts.user_id` và `carts.session_id` khi khác null
- nên dùng `jsonb` cho `address_snapshot_json` và `raw_payload_json`

## 9. Chỉ mục đề xuất

- index `products.slug`
- index `products.category_id`
- index `products.brand_id`
- index `products.status`
- index `product_variants.product_id`
- index `product_variants.status`
- index `orders.user_id`
- index `orders.order_code`
- index `orders.order_status`
- index `orders.payment_status`
- index `payments.order_id`
- index `inventory_transactions.variant_id`
- index `inventory_transactions.order_id`

## 10. Quy tắc xóa dữ liệu

- `users`: không hard delete nếu đã có order, chỉ khóa tài khoản
- `products`: không hard delete nếu đã có order, chỉ chuyển `INACTIVE`
- `product_variants`: không hard delete nếu đã có order
- `orders`, `order_items`, `payments`, `inventory_transactions`: không xóa
- `cart_items`: được xóa bình thường
- `carts`: guest cart có thể xóa sau khi merge hoặc hết hạn

## 11. Mapping sang Prisma

Khuyến nghị:

- Dùng `@db.VarChar`, `@db.Decimal`, `Json` cho các trường phù hợp
- Dùng enum Prisma cho:
  - `UserRole`
  - `UserStatus`
  - `ProductStatus`
  - `VariantStatus`
  - `OrderStatus`
  - `PaymentStatus`
  - `PaymentMethod`
  - `PaymentProvider`
  - `InventoryTransactionType`
- Dùng `@@unique([cartId, variantId])` cho `cart_items`
- Dùng `@@index([...])` cho các trường lọc thường xuyên

## 12. Các điểm cần khóa trước khi code schema

1. Dùng `uuid` hay `bigint` cho toàn bộ PK
2. `sport_type` dùng enum cứng hay varchar linh hoạt
3. `gender` có cần thêm giá trị khác ngoài `MEN`, `WOMEN`, `UNISEX`, `KIDS` không
4. `payment_method` có chỉ `COD` và `VNPAY` ở Phase 1 không
5. Có cần thêm `deleted_at` cho soft delete ở `products`, `variants`, `categories`, `brands` không

## 13. Kết luận

ERD này đã đủ chi tiết để chuyển sang:

- Prisma schema
- PostgreSQL DDL
- API spec request/response
- Task breakdown theo module

Bước tiếp theo hợp lý nhất là viết `PRISMA_SCHEMA_SPEC.md` hoặc `API_SPEC_PHASE1.md`.
