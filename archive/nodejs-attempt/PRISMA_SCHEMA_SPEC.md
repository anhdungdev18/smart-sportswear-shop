# PRISMA SCHEMA SPEC

## 1. Mục tiêu

Tài liệu này chuyển `ERD_PHASE1.md` sang đặc tả phù hợp để viết `schema.prisma`.

Mục tiêu:

- Chốt danh sách Prisma model
- Chốt enum Prisma
- Chốt relation, unique, index
- Chốt kiểu dữ liệu nên dùng
- Chỉ ra các điểm cần xử lý bằng migration SQL bổ sung nếu Prisma không biểu đạt gọn

## 2. Quy ước chung

### 2.1 ID strategy

Phase 1 nên chọn một trong hai hướng và giữ thống nhất toàn hệ thống:

- `String @id @default(cuid())`
- `String @id @default(uuid())`

Khuyến nghị:

- Dùng `uuid()` nếu muốn dễ tích hợp với hệ khác về sau
- Dùng `cuid()` nếu muốn đơn giản, đủ tốt cho app web nội bộ

Tài liệu này giả định dùng:

- `String @id @default(uuid())`

### 2.2 Timestamp fields

Khuyến nghị:

- `createdAt DateTime @default(now())`
- `updatedAt DateTime @updatedAt`

### 2.3 Decimal money fields

Các trường tiền nên dùng:

- `Decimal @db.Decimal(12, 2)`

Không dùng `Float` cho tiền.

### 2.4 JSON fields

- `addressSnapshotJson Json`
- `rawPayloadJson Json?`

### 2.5 Naming convention

Khuyến nghị dùng:

- tên model PascalCase
- tên field camelCase
- map về tên bảng snake_case bằng `@@map`
- map field về cột snake_case bằng `@map`

Ví dụ:

```prisma
model User {
  id        String   @id @default(uuid())
  fullName  String   @map("full_name")
  createdAt DateTime @default(now()) @map("created_at")

  @@map("users")
}
```

## 3. Danh sách enum

### 3.1 UserRole

```prisma
enum UserRole {
  CUSTOMER
  SALES_STAFF
  WAREHOUSE_STAFF
  ADMIN
}
```

### 3.2 UserStatus

```prisma
enum UserStatus {
  ACTIVE
  LOCKED
}
```

### 3.3 CategoryStatus

```prisma
enum CategoryStatus {
  ACTIVE
  INACTIVE
}
```

### 3.4 BrandStatus

```prisma
enum BrandStatus {
  ACTIVE
  INACTIVE
}
```

### 3.5 ProductStatus

```prisma
enum ProductStatus {
  DRAFT
  ACTIVE
  INACTIVE
}
```

### 3.6 VariantStatus

```prisma
enum VariantStatus {
  ACTIVE
  OUT_OF_STOCK
  INACTIVE
}
```

### 3.7 Gender

```prisma
enum Gender {
  MEN
  WOMEN
  UNISEX
  KIDS
}
```

### 3.8 PaymentMethod

```prisma
enum PaymentMethod {
  COD
  VNPAY
}
```

### 3.9 OrderStatus

```prisma
enum OrderStatus {
  PENDING_CONFIRMATION
  CONFIRMED
  PACKING
  SHIPPING
  DELIVERED
  CANCELLED
}
```

### 3.10 PaymentStatus

```prisma
enum PaymentStatus {
  UNPAID
  PENDING
  PAID
  FAILED
  CANCELLED
  REFUNDED
}
```

### 3.11 PaymentProvider

```prisma
enum PaymentProvider {
  COD
  VNPAY
}
```

### 3.12 InventoryTransactionType

```prisma
enum InventoryTransactionType {
  IMPORT
  EXPORT
  ADJUSTMENT_UP
  ADJUSTMENT_DOWN
  ORDER_RESERVE
  ORDER_RELEASE
  ORDER_CONFIRM_DEDUCT
}
```

## 4. Danh sách model

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

## 5. Đặc tả từng model

### 5.1 User

```prisma
model User {
  id            String               @id @default(uuid())
  fullName      String               @map("full_name") @db.VarChar(150)
  email         String               @unique @db.VarChar(255)
  phone         String?              @db.VarChar(20)
  passwordHash  String               @map("password_hash") @db.VarChar(255)
  role          UserRole
  status        UserStatus           @default(ACTIVE)
  lastLoginAt   DateTime?            @map("last_login_at")
  createdAt     DateTime             @default(now()) @map("created_at")
  updatedAt     DateTime             @updatedAt @map("updated_at")

  refreshTokens RefreshToken[]
  resetTokens   PasswordResetToken[]
  addresses     Address[]
  carts         Cart[]
  orders        Order[]
  inventoryLogs InventoryTransaction[] @relation("InventoryCreatedBy")

  @@map("users")
}
```

Ghi chú:

- `carts` tồn tại vì user có thể có 1 cart active, nhưng relation vẫn là mảng ở mức Prisma

### 5.2 RefreshToken

```prisma
model RefreshToken {
  id         String   @id @default(uuid())
  userId     String   @map("user_id")
  tokenHash  String   @unique @map("token_hash") @db.VarChar(255)
  expiresAt  DateTime @map("expires_at")
  revokedAt  DateTime? @map("revoked_at")
  createdAt  DateTime @default(now()) @map("created_at")

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@index([userId])
  @@map("refresh_tokens")
}
```

### 5.3 PasswordResetToken

```prisma
model PasswordResetToken {
  id         String   @id @default(uuid())
  userId     String   @map("user_id")
  tokenHash  String   @unique @map("token_hash") @db.VarChar(255)
  expiresAt  DateTime @map("expires_at")
  usedAt     DateTime? @map("used_at")
  createdAt  DateTime @default(now()) @map("created_at")

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@index([userId])
  @@map("password_reset_tokens")
}
```

### 5.4 Address

```prisma
model Address {
  id           String   @id @default(uuid())
  userId       String   @map("user_id")
  receiverName String   @map("receiver_name") @db.VarChar(150)
  phone        String   @db.VarChar(20)
  province     String   @db.VarChar(100)
  district     String   @db.VarChar(100)
  ward         String   @db.VarChar(100)
  addressLine  String   @map("address_line") @db.VarChar(255)
  isDefault    Boolean  @default(false) @map("is_default")
  createdAt    DateTime @default(now()) @map("created_at")
  updatedAt    DateTime @updatedAt @map("updated_at")

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@index([userId])
  @@map("addresses")
}
```

### 5.5 Category

```prisma
model Category {
  id          String         @id @default(uuid())
  name        String         @db.VarChar(150)
  slug        String         @unique @db.VarChar(180)
  description String?
  status      CategoryStatus @default(ACTIVE)
  createdAt   DateTime       @default(now()) @map("created_at")
  updatedAt   DateTime       @updatedAt @map("updated_at")

  products Product[]

  @@map("categories")
}
```

### 5.6 Brand

```prisma
model Brand {
  id          String      @id @default(uuid())
  name        String      @db.VarChar(150)
  slug        String      @unique @db.VarChar(180)
  description String?
  status      BrandStatus @default(ACTIVE)
  createdAt   DateTime    @default(now()) @map("created_at")
  updatedAt   DateTime    @updatedAt @map("updated_at")

  products Product[]

  @@map("brands")
}
```

### 5.7 Product

```prisma
model Product {
  id               String        @id @default(uuid())
  categoryId       String        @map("category_id")
  brandId          String        @map("brand_id")
  name             String        @db.VarChar(255)
  slug             String        @unique @db.VarChar(300)
  shortDescription String?       @map("short_description") @db.VarChar(500)
  description      String?
  gender           Gender?
  sportType        String?       @map("sport_type") @db.VarChar(50)
  status           ProductStatus @default(DRAFT)
  isFeatured       Boolean       @default(false) @map("is_featured")
  createdAt        DateTime      @default(now()) @map("created_at")
  updatedAt        DateTime      @updatedAt @map("updated_at")

  category Category         @relation(fields: [categoryId], references: [id])
  brand    Brand            @relation(fields: [brandId], references: [id])
  variants ProductVariant[]
  images   ProductImage[]
  orderItems OrderItem[]

  @@index([categoryId])
  @@index([brandId])
  @@index([status])
  @@map("products")
}
```

Ghi chú:

- `sportType` hiện để `String` cho linh hoạt Phase 1; nếu muốn cứng hóa có thể đổi sang enum sau

### 5.8 ProductVariant

```prisma
model ProductVariant {
  id               String        @id @default(uuid())
  productId        String        @map("product_id")
  sku              String        @unique @db.VarChar(100)
  size             String        @db.VarChar(50)
  color            String        @db.VarChar(50)
  price            Decimal       @db.Decimal(12, 2)
  compareAtPrice   Decimal?      @map("compare_at_price") @db.Decimal(12, 2)
  stockQuantity    Int           @default(0) @map("stock_quantity")
  reservedQuantity Int           @default(0) @map("reserved_quantity")
  version          Int           @default(1)
  status           VariantStatus @default(ACTIVE)
  createdAt        DateTime      @default(now()) @map("created_at")
  updatedAt        DateTime      @updatedAt @map("updated_at")

  product               Product                @relation(fields: [productId], references: [id])
  cartItems             CartItem[]
  orderItems            OrderItem[]
  inventoryTransactions InventoryTransaction[]

  @@index([productId])
  @@index([status])
  @@map("product_variants")
}
```

Ghi chú:

- các check `stockQuantity >= 0`, `reservedQuantity >= 0`, `reservedQuantity <= stockQuantity` nên thêm bằng SQL migration bổ sung

### 5.9 ProductImage

```prisma
model ProductImage {
  id        String   @id @default(uuid())
  productId String   @map("product_id")
  imageUrl  String   @map("image_url") @db.VarChar(500)
  publicId  String?  @map("public_id") @db.VarChar(255)
  altText   String?  @map("alt_text") @db.VarChar(255)
  sortOrder Int      @default(0) @map("sort_order")
  isPrimary Boolean  @default(false) @map("is_primary")
  createdAt DateTime @default(now()) @map("created_at")

  product Product @relation(fields: [productId], references: [id], onDelete: Cascade)

  @@index([productId])
  @@map("product_images")
}
```

### 5.10 Cart

```prisma
model Cart {
  id        String   @id @default(uuid())
  userId    String?  @map("user_id")
  sessionId String?  @map("session_id") @db.VarChar(255)
  createdAt DateTime @default(now()) @map("created_at")
  updatedAt DateTime @updatedAt @map("updated_at")

  user  User?      @relation(fields: [userId], references: [id])
  items CartItem[]

  @@map("carts")
}
```

Ghi chú:

- partial unique index cho `user_id` và `session_id` cần thêm bằng migration SQL nếu Prisma không biểu đạt trực tiếp theo kiểu mong muốn

### 5.11 CartItem

```prisma
model CartItem {
  id        String   @id @default(uuid())
  cartId    String   @map("cart_id")
  variantId String   @map("variant_id")
  quantity  Int
  createdAt DateTime @default(now()) @map("created_at")
  updatedAt DateTime @updatedAt @map("updated_at")

  cart    Cart           @relation(fields: [cartId], references: [id], onDelete: Cascade)
  variant ProductVariant @relation(fields: [variantId], references: [id])

  @@unique([cartId, variantId])
  @@index([variantId])
  @@map("cart_items")
}
```

### 5.12 Order

```prisma
model Order {
  id                  String        @id @default(uuid())
  orderCode           String        @unique @map("order_code") @db.VarChar(50)
  userId              String        @map("user_id")
  addressSnapshotJson Json          @map("address_snapshot_json")
  subtotalAmount      Decimal       @map("subtotal_amount") @db.Decimal(12, 2)
  shippingFee         Decimal       @default(0) @map("shipping_fee") @db.Decimal(12, 2)
  discountAmount      Decimal       @default(0) @map("discount_amount") @db.Decimal(12, 2)
  totalAmount         Decimal       @map("total_amount") @db.Decimal(12, 2)
  paymentMethod       PaymentMethod @map("payment_method")
  orderStatus         OrderStatus   @default(PENDING_CONFIRMATION) @map("order_status")
  paymentStatus       PaymentStatus @default(UNPAID) @map("payment_status")
  note                String?
  internalNote        String?       @map("internal_note")
  createdAt           DateTime      @default(now()) @map("created_at")
  updatedAt           DateTime      @updatedAt @map("updated_at")

  user                  User                   @relation(fields: [userId], references: [id])
  items                 OrderItem[]
  payments              Payment[]
  inventoryTransactions InventoryTransaction[]

  @@index([userId])
  @@index([orderStatus])
  @@index([paymentStatus])
  @@map("orders")
}
```

### 5.13 OrderItem

```prisma
model OrderItem {
  id                  String   @id @default(uuid())
  orderId             String   @map("order_id")
  productId           String   @map("product_id")
  variantId           String   @map("variant_id")
  productNameSnapshot String   @map("product_name_snapshot") @db.VarChar(255)
  skuSnapshot         String   @map("sku_snapshot") @db.VarChar(100)
  sizeSnapshot        String   @map("size_snapshot") @db.VarChar(50)
  colorSnapshot       String   @map("color_snapshot") @db.VarChar(50)
  unitPriceSnapshot   Decimal  @map("unit_price_snapshot") @db.Decimal(12, 2)
  quantity            Int
  lineTotal           Decimal  @map("line_total") @db.Decimal(12, 2)

  order   Order          @relation(fields: [orderId], references: [id], onDelete: Cascade)
  product Product        @relation(fields: [productId], references: [id])
  variant ProductVariant @relation(fields: [variantId], references: [id])

  @@index([orderId])
  @@index([productId])
  @@index([variantId])
  @@map("order_items")
}
```

### 5.14 Payment

```prisma
model Payment {
  id             String          @id @default(uuid())
  orderId        String          @map("order_id")
  provider       PaymentProvider
  transactionRef String          @unique @map("transaction_ref") @db.VarChar(150)
  amount         Decimal         @db.Decimal(12, 2)
  status         PaymentStatus
  rawPayloadJson Json?           @map("raw_payload_json")
  paidAt         DateTime?       @map("paid_at")
  createdAt      DateTime        @default(now()) @map("created_at")
  updatedAt      DateTime        @updatedAt @map("updated_at")

  order Order @relation(fields: [orderId], references: [id], onDelete: Cascade)

  @@index([orderId])
  @@map("payments")
}
```

### 5.15 InventoryTransaction

```prisma
model InventoryTransaction {
  id                     String                   @id @default(uuid())
  variantId              String                   @map("variant_id")
  orderId                String?                  @map("order_id")
  type                   InventoryTransactionType
  quantity               Int
  beforeStockQuantity    Int                      @map("before_stock_quantity")
  afterStockQuantity     Int                      @map("after_stock_quantity")
  beforeReservedQuantity Int                      @map("before_reserved_quantity")
  afterReservedQuantity  Int                      @map("after_reserved_quantity")
  note                   String?
  createdBy              String?                  @map("created_by")
  createdAt              DateTime                 @default(now()) @map("created_at")

  variant ProductVariant @relation(fields: [variantId], references: [id])
  order   Order?         @relation(fields: [orderId], references: [id])
  creator User?          @relation("InventoryCreatedBy", fields: [createdBy], references: [id])

  @@index([variantId])
  @@index([orderId])
  @@index([createdBy])
  @@map("inventory_transactions")
}
```

## 6. Quan hệ cần đặt tên rõ trong Prisma

Nên đặt tên relation ở các chỗ dễ mơ hồ:

- `User.inventoryLogs` <-> `InventoryTransaction.creator`
- `User.carts` nếu giữ nhiều cart lịch sử

Nếu muốn enforce đúng "mỗi user chỉ có 1 active cart", vẫn dùng relation thường nhưng khóa bằng unique index DB.

## 7. Index và unique cần có

### 7.1 Có thể khai báo trực tiếp trong Prisma

- `@unique` cho:
  - `User.email`
  - `RefreshToken.tokenHash`
  - `PasswordResetToken.tokenHash`
  - `Category.slug`
  - `Brand.slug`
  - `Product.slug`
  - `ProductVariant.sku`
  - `Order.orderCode`
  - `Payment.transactionRef`

- `@@unique([cartId, variantId])` cho `CartItem`

- `@@index` cho:
  - `Product.categoryId`
  - `Product.brandId`
  - `Product.status`
  - `ProductVariant.productId`
  - `ProductVariant.status`
  - `Order.userId`
  - `Order.orderStatus`
  - `Order.paymentStatus`
  - `Payment.orderId`
  - `InventoryTransaction.variantId`
  - `InventoryTransaction.orderId`

### 7.2 Nên thêm bằng SQL migration

- partial unique index cho `carts.user_id where user_id is not null`
- partial unique index cho `carts.session_id where session_id is not null`
- check constraint:
  - `stock_quantity >= 0`
  - `reserved_quantity >= 0`
  - `reserved_quantity <= stock_quantity`
  - `quantity > 0` ở các bảng phù hợp
  - các amount không âm

## 8. Hành vi onDelete khuyến nghị

- `User -> RefreshToken`: `Cascade`
- `User -> PasswordResetToken`: `Cascade`
- `User -> Address`: `Cascade`
- `Cart -> CartItem`: `Cascade`
- `Order -> OrderItem`: `Cascade`
- `Order -> Payment`: `Cascade`
- `Product -> ProductVariant`: không nên cascade xóa trong nghiệp vụ thật; nên tránh xóa product đã dùng
- `Product -> ProductImage`: có thể `Cascade`

Khuyến nghị chung:

- không xóa hard delete các bản ghi đã tham gia nghiệp vụ
- dù Prisma cho khai báo quan hệ xóa, application vẫn nên tránh gọi delete bừa

## 9. Các field nên cân nhắc thêm sau

- `deletedAt` cho soft delete ở `Category`, `Brand`, `Product`, `ProductVariant`
- `emailVerifiedAt` cho `User`
- `expiresAt` cho `Cart` nếu muốn dọn guest cart định kỳ

## 10. Mẫu thứ tự model trong schema.prisma

Khuyến nghị sắp thứ tự:

1. enum
2. `User`
3. `RefreshToken`
4. `PasswordResetToken`
5. `Address`
6. `Category`
7. `Brand`
8. `Product`
9. `ProductVariant`
10. `ProductImage`
11. `Cart`
12. `CartItem`
13. `Order`
14. `OrderItem`
15. `Payment`
16. `InventoryTransaction`

## 11. Những điểm cần khóa trước khi viết schema thật

1. Chốt `uuid()` hay `cuid()`
2. Chốt `sportType` dùng `String` hay enum
3. Có thêm `deletedAt` ngay từ đầu không
4. Có cần model `AuditLog` riêng không, hay Phase 1 chỉ log ở application layer

## 12. Kết luận

Tài liệu này đã đủ để viết:

- `schema.prisma`
- migration đầu tiên
- seed script

Bước tiếp theo hợp lý nhất là:

1. viết `TASK_BREAKDOWN_PHASE1.md`, hoặc
2. viết thẳng `schema.prisma` skeleton
