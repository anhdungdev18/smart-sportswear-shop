# API SPEC PHASE 1

## 1. Mục tiêu

Tài liệu này mô tả API Phase 1 cho sản phẩm bán trang phục thể thao không AI.

Mục tiêu:

- Chốt contract giữa frontend và backend
- Chốt endpoint, method, auth requirement
- Chốt request, response, query params
- Chốt lỗi phổ biến và quyền truy cập

## 2. Quy ước chung

### 2.1 Base URL

- Base path: `/api/v1`

### 2.2 Content type

- Request: `application/json`
- Response: `application/json`

### 2.3 Auth

- Access token gửi qua header:

```http
Authorization: Bearer <access_token>
```

- Guest cart dùng `session_id` qua cookie

### 2.4 Response chuẩn

Success:

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "meta": {}
}
```

Error:

```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    {
      "field": "email",
      "message": "Email is invalid"
    }
  ]
}
```

### 2.5 HTTP status code

- `200` OK
- `201` Created
- `400` Bad Request
- `401` Unauthorized
- `403` Forbidden
- `404` Not Found
- `409` Conflict
- `422` Unprocessable Entity
- `500` Internal Server Error

### 2.6 Phân trang

Query chuẩn:

- `page`
- `limit`
- `sortBy`
- `sortOrder`

Meta mẫu:

```json
{
  "page": 1,
  "limit": 20,
  "total": 120,
  "totalPages": 6
}
```

## 3. Auth APIs

### 3.1 Đăng ký

- `POST /api/v1/auth/register`
- Auth: không cần

Request:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "password": "Password123",
  "phone": "0900000000"
}
```

Response:

```json
{
  "success": true,
  "message": "Register successful",
  "data": {
    "user": {
      "id": "usr_001",
      "fullName": "Nguyen Van A",
      "email": "a@example.com",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    },
    "tokens": {
      "accessToken": "jwt_access",
      "refreshToken": "jwt_refresh"
    }
  }
}
```

Lỗi:

- `409`: email đã tồn tại
- `422`: dữ liệu không hợp lệ

### 3.2 Đăng nhập

- `POST /api/v1/auth/login`
- Auth: không cần

Request:

```json
{
  "email": "a@example.com",
  "password": "Password123"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "id": "usr_001",
      "fullName": "Nguyen Van A",
      "email": "a@example.com",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    },
    "tokens": {
      "accessToken": "jwt_access",
      "refreshToken": "jwt_refresh"
    }
  }
}
```

Ghi chú:

- nếu có guest cart theo `session_id`, backend phải merge vào user cart trước khi trả response thành công

### 3.3 Refresh token

- `POST /api/v1/auth/refresh`
- Auth: không cần access token

Request:

```json
{
  "refreshToken": "jwt_refresh"
}
```

Response:

```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token"
  }
}
```

### 3.4 Đăng xuất

- `POST /api/v1/auth/logout`
- Auth: cần access token

Request:

```json
{
  "refreshToken": "jwt_refresh"
}
```

Response:

```json
{
  "success": true,
  "message": "Logout successful",
  "data": {}
}
```

### 3.5 Quên mật khẩu

- `POST /api/v1/auth/forgot-password`
- Auth: không cần

Request:

```json
{
  "email": "a@example.com"
}
```

Response:

```json
{
  "success": true,
  "message": "If the email exists, a reset instruction has been sent",
  "data": {}
}
```

Ghi chú:

- luôn trả response giống nhau dù email có tồn tại hay không

### 3.6 Đặt lại mật khẩu

- `POST /api/v1/auth/reset-password`
- Auth: không cần

Request:

```json
{
  "token": "reset_token",
  "newPassword": "NewPassword123"
}
```

Response:

```json
{
  "success": true,
  "message": "Password reset successful",
  "data": {}
}
```

## 4. User APIs

### 4.1 Lấy thông tin tài khoản

- `GET /api/v1/me`
- Auth: `CUSTOMER`, `SALES_STAFF`, `WAREHOUSE_STAFF`, `ADMIN`

Response:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "usr_001",
    "fullName": "Nguyen Van A",
    "email": "a@example.com",
    "phone": "0900000000",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

### 4.2 Cập nhật tài khoản

- `PATCH /api/v1/me`
- Auth: đăng nhập

Request:

```json
{
  "fullName": "Nguyen Van B",
  "phone": "0911111111"
}
```

### 4.3 Danh sách địa chỉ

- `GET /api/v1/me/addresses`
- Auth: đăng nhập

### 4.4 Tạo địa chỉ

- `POST /api/v1/me/addresses`
- Auth: đăng nhập

Request:

```json
{
  "receiverName": "Nguyen Van A",
  "phone": "0900000000",
  "province": "Ho Chi Minh",
  "district": "Thu Duc",
  "ward": "Linh Trung",
  "addressLine": "123 Street",
  "isDefault": true
}
```

### 4.5 Cập nhật địa chỉ

- `PATCH /api/v1/me/addresses/:id`
- Auth: đăng nhập

### 4.6 Xóa địa chỉ

- `DELETE /api/v1/me/addresses/:id`
- Auth: đăng nhập

## 5. Catalog APIs

### 5.1 Danh sách category

- `GET /api/v1/categories`
- Auth: không cần

### 5.2 Danh sách brand

- `GET /api/v1/brands`
- Auth: không cần

### 5.3 Danh sách sản phẩm

- `GET /api/v1/products`
- Auth: không cần

Query params:

- `page`
- `limit`
- `keyword`
- `categoryId`
- `brandId`
- `minPrice`
- `maxPrice`
- `size`
- `color`
- `gender`
- `sportType`
- `sortBy`
- `sortOrder`

Response mẫu:

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": "prd_001",
      "name": "Áo chạy bộ nam",
      "slug": "ao-chay-bo-nam",
      "shortDescription": "Áo thể thao nhẹ",
      "brand": {
        "id": "br_001",
        "name": "Nike"
      },
      "category": {
        "id": "cat_001",
        "name": "Áo"
      },
      "thumbnail": "https://...",
      "minPrice": 250000,
      "maxPrice": 350000,
      "status": "ACTIVE"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

### 5.4 Chi tiết sản phẩm

- `GET /api/v1/products/:slugOrId`
- Auth: không cần

Response mẫu:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "prd_001",
    "name": "Áo chạy bộ nam",
    "slug": "ao-chay-bo-nam",
    "shortDescription": "Áo thể thao nhẹ",
    "description": "Mô tả chi tiết",
    "gender": "MEN",
    "sportType": "RUNNING",
    "brand": {
      "id": "br_001",
      "name": "Nike"
    },
    "category": {
      "id": "cat_001",
      "name": "Áo"
    },
    "images": [
      {
        "id": "img_001",
        "imageUrl": "https://...",
        "isPrimary": true,
        "sortOrder": 0
      }
    ],
    "variants": [
      {
        "id": "var_001",
        "sku": "SKU001",
        "size": "M",
        "color": "Black",
        "price": 299000,
        "compareAtPrice": 399000,
        "availableQuantity": 10,
        "status": "ACTIVE"
      }
    ]
  }
}
```

## 6. Cart APIs

### 6.1 Lấy giỏ hàng

- `GET /api/v1/cart`
- Auth: không cần

Ghi chú:

- nếu chưa đăng nhập thì lấy giỏ theo `session_id`
- nếu đã đăng nhập thì lấy giỏ theo `user_id`

Response mẫu:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "cart_001",
    "items": [
      {
        "id": "item_001",
        "variantId": "var_001",
        "productId": "prd_001",
        "productName": "Áo chạy bộ nam",
        "sku": "SKU001",
        "size": "M",
        "color": "Black",
        "price": 299000,
        "quantity": 2,
        "lineTotal": 598000,
        "thumbnail": "https://..."
      }
    ],
    "subtotal": 598000
  }
}
```

### 6.2 Thêm item vào giỏ

- `POST /api/v1/cart/items`
- Auth: không cần

Request:

```json
{
  "variantId": "var_001",
  "quantity": 2
}
```

Lỗi:

- `404`: variant không tồn tại
- `422`: quantity vượt tồn khả dụng

### 6.3 Cập nhật số lượng item

- `PATCH /api/v1/cart/items/:id`
- Auth: không cần

Request:

```json
{
  "quantity": 3
}
```

### 6.4 Xóa item

- `DELETE /api/v1/cart/items/:id`
- Auth: không cần

## 7. Order APIs

### 7.1 Tạo đơn hàng

- `POST /api/v1/orders`
- Auth: `CUSTOMER`

Request:

```json
{
  "addressId": "addr_001",
  "paymentMethod": "COD",
  "note": "Giao giờ hành chính"
}
```

Response mẫu:

```json
{
  "success": true,
  "message": "Order created",
  "data": {
    "id": "ord_001",
    "orderCode": "ORD20260620001",
    "orderStatus": "PENDING_CONFIRMATION",
    "paymentStatus": "UNPAID",
    "paymentMethod": "COD",
    "subtotalAmount": 598000,
    "shippingFee": 30000,
    "discountAmount": 0,
    "totalAmount": 628000
  }
}
```

Ghi chú:

- khi tạo đơn phải reserve tồn
- nếu cart rỗng thì trả `422`

### 7.2 Lịch sử đơn của tôi

- `GET /api/v1/orders/me`
- Auth: `CUSTOMER`

Query params:

- `page`
- `limit`
- `status`

### 7.3 Chi tiết đơn hàng

- `GET /api/v1/orders/:id`
- Auth: `CUSTOMER`, `ADMIN`, `SALES_STAFF`

Ghi chú:

- customer chỉ xem được đơn của chính mình

### 7.4 Hủy đơn

- `POST /api/v1/orders/:id/cancel`
- Auth: `CUSTOMER`

Request:

```json
{
  "reason": "Thay đổi nhu cầu"
}
```

Lỗi:

- `409`: không thể hủy ở trạng thái hiện tại

## 8. Payment APIs

### 8.1 Tạo giao dịch thanh toán

- `POST /api/v1/payments/create`
- Auth: `CUSTOMER`

Request:

```json
{
  "orderId": "ord_001"
}
```

Response:

```json
{
  "success": true,
  "message": "Payment session created",
  "data": {
    "paymentUrl": "https://sandbox.vnpay.vn/...",
    "transactionRef": "txn_001"
  }
}
```

### 8.2 Callback từ cổng thanh toán

- `POST /api/v1/payments/callback`
- Auth: không cần

Ghi chú:

- endpoint này nhận payload từ cổng thanh toán
- phải verify checksum
- phải xử lý idempotent theo `transaction_ref`

Response:

```json
{
  "success": true,
  "message": "Callback processed",
  "data": {}
}
```

### 8.3 Xem payment theo order

- `GET /api/v1/payments/:orderId`
- Auth: `CUSTOMER`, `ADMIN`, `SALES_STAFF`

## 9. Admin Catalog APIs

### 9.1 Tạo category

- `POST /api/v1/admin/categories`
- Auth: `ADMIN`

Request:

```json
{
  "name": "Áo",
  "slug": "ao",
  "description": "Danh mục áo",
  "status": "ACTIVE"
}
```

### 9.2 Cập nhật category

- `PATCH /api/v1/admin/categories/:id`
- Auth: `ADMIN`

### 9.3 Tạo brand

- `POST /api/v1/admin/brands`
- Auth: `ADMIN`

### 9.4 Cập nhật brand

- `PATCH /api/v1/admin/brands/:id`
- Auth: `ADMIN`

### 9.5 Tạo product

- `POST /api/v1/admin/products`
- Auth: `ADMIN`

Request:

```json
{
  "name": "Áo chạy bộ nam",
  "slug": "ao-chay-bo-nam",
  "shortDescription": "Áo thể thao nhẹ",
  "description": "Mô tả",
  "categoryId": "cat_001",
  "brandId": "br_001",
  "gender": "MEN",
  "sportType": "RUNNING",
  "status": "DRAFT"
}
```

### 9.6 Cập nhật product

- `PATCH /api/v1/admin/products/:id`
- Auth: `ADMIN`

### 9.7 Tạo variant

- `POST /api/v1/admin/products/:id/variants`
- Auth: `ADMIN`

Request:

```json
{
  "sku": "SKU001",
  "size": "M",
  "color": "Black",
  "price": 299000,
  "compareAtPrice": 399000,
  "stockQuantity": 20,
  "status": "ACTIVE"
}
```

### 9.8 Cập nhật variant

- `PATCH /api/v1/admin/variants/:id`
- Auth: `ADMIN`

### 9.9 Upload ảnh sản phẩm

- `POST /api/v1/admin/products/:id/images`
- Auth: `ADMIN`

Request:

```json
{
  "imageUrl": "https://...",
  "publicId": "cloudinary_public_id",
  "altText": "Áo chạy bộ nam",
  "sortOrder": 0,
  "isPrimary": true
}
```

## 10. Admin Order APIs

### 10.1 Danh sách đơn hàng

- `GET /api/v1/admin/orders`
- Auth: `ADMIN`, `SALES_STAFF`

Query params:

- `page`
- `limit`
- `keyword`
- `status`
- `paymentStatus`
- `paymentMethod`
- `dateFrom`
- `dateTo`

### 10.2 Chi tiết đơn hàng

- `GET /api/v1/admin/orders/:id`
- Auth: `ADMIN`, `SALES_STAFF`

### 10.3 Cập nhật trạng thái đơn

- `PATCH /api/v1/admin/orders/:id/status`
- Auth: `ADMIN`, `SALES_STAFF`

Request:

```json
{
  "status": "CONFIRMED",
  "note": "Đã xác nhận đơn"
}
```

Ghi chú:

- `CONFIRMED` phải trừ tồn thật từ reserved
- `CANCELLED` phải release tồn nếu đang reserve

## 11. Inventory APIs

### 11.1 Danh sách tồn kho

- `GET /api/v1/admin/inventory`
- Auth: `ADMIN`, `WAREHOUSE_STAFF`

Query params:

- `page`
- `limit`
- `keyword`
- `productId`
- `categoryId`
- `brandId`
- `status`

### 11.2 Nhập kho

- `POST /api/v1/admin/inventory/import`
- Auth: `ADMIN`, `WAREHOUSE_STAFF`

Request:

```json
{
  "variantId": "var_001",
  "quantity": 50,
  "note": "Nhập lô mới"
}
```

### 11.3 Xuất kho

- `POST /api/v1/admin/inventory/export`
- Auth: `ADMIN`, `WAREHOUSE_STAFF`

Request:

```json
{
  "variantId": "var_001",
  "quantity": 5,
  "note": "Xuất nội bộ"
}
```

### 11.4 Điều chỉnh kho

- `POST /api/v1/admin/inventory/adjust`
- Auth: `ADMIN`, `WAREHOUSE_STAFF`

Request:

```json
{
  "variantId": "var_001",
  "type": "ADJUSTMENT_DOWN",
  "quantity": 2,
  "note": "Hàng lỗi"
}
```

### 11.5 Lịch sử biến động kho

- `GET /api/v1/admin/inventory/transactions`
- Auth: `ADMIN`, `WAREHOUSE_STAFF`

Query params:

- `page`
- `limit`
- `variantId`
- `type`
- `dateFrom`
- `dateTo`

## 12. Report APIs

### 12.1 Dashboard overview

- `GET /api/v1/admin/reports/overview`
- Auth: `ADMIN`

Response mẫu:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "grossRevenue": 10000000,
    "realizedRevenue": 8000000,
    "totalOrders": 120,
    "pendingOrders": 10,
    "lowStockCount": 8
  }
}
```

### 12.2 Report đơn hàng

- `GET /api/v1/admin/reports/orders`
- Auth: `ADMIN`

### 12.3 Report sản phẩm

- `GET /api/v1/admin/reports/products`
- Auth: `ADMIN`

### 12.4 Report tồn kho

- `GET /api/v1/admin/reports/inventory`
- Auth: `ADMIN`

## 13. Ma trận quyền truy cập

- Guest:
  - `GET /categories`
  - `GET /brands`
  - `GET /products`
  - `GET /products/:slugOrId`
  - `GET /cart`
  - `POST /cart/items`
  - `PATCH /cart/items/:id`
  - `DELETE /cart/items/:id`
  - auth endpoints public

- Customer:
  - toàn bộ API guest
  - `GET /me`
  - `PATCH /me`
  - địa chỉ cá nhân
  - order của mình
  - payment của order của mình

- Sales staff:
  - admin order APIs

- Warehouse staff:
  - inventory APIs

- Admin:
  - toàn bộ admin APIs

## 14. Lỗi nghiệp vụ quan trọng

- `EMAIL_ALREADY_EXISTS`
- `INVALID_CREDENTIALS`
- `ACCOUNT_LOCKED`
- `ADDRESS_NOT_FOUND`
- `PRODUCT_NOT_FOUND`
- `VARIANT_NOT_FOUND`
- `INSUFFICIENT_STOCK`
- `CART_EMPTY`
- `ORDER_NOT_FOUND`
- `ORDER_STATUS_INVALID`
- `PAYMENT_ALREADY_COMPLETED`
- `PAYMENT_CALLBACK_INVALID`
- `FORBIDDEN_RESOURCE`

## 15. Các điểm cần khóa trước khi code

1. Có dùng cookie httpOnly cho refresh token hay trả thẳng JSON
2. `sportType` dùng enum cứng hay string
3. `inventory/import/export/adjust` có cần batch nhiều variant một lần không
4. `products/:slugOrId` ưu tiên lookup theo slug trước hay id trước
5. `payments/callback` nhận `POST` hay cần hỗ trợ cả `GET` theo nhà cung cấp

## 16. Kết luận

API spec này đã đủ để:

- frontend bắt đầu tách page và form
- backend bắt đầu tạo controller/service
- QA viết test case cơ bản

Bước tiếp theo hợp lý nhất là viết `Prisma schema` hoặc `task breakdown theo module`.
