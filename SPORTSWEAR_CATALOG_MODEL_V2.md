# Mô hình dữ liệu đề xuất V2 cho Smart Sportswear Shop

## 1. Mục tiêu

Tài liệu này chốt hướng mở rộng mô hình dữ liệu sản phẩm từ:

- web bán hàng thể thao nói chung
- không chỉ có quần áo
- mà còn có giày, phụ kiện và có thể mở rộng thêm nhóm khác sau này
- đồng thời hỗ trợ `bộ sưu tập` dùng chung cho nhiều loại sản phẩm

Ví dụ:

- BST Mùa hè 2026 có thể chứa áo, quần, giày, phụ kiện
- BST Chạy bộ có thể chứa áo running, quần running, giày running, bình nước
- BST Bóng đá có thể chứa áo đấu, quần đấu, giày sân cỏ, tất, bọc ống đồng

Nghĩa là hệ thống phải hỗ trợ:

- phân loại theo loại sản phẩm
- phân loại theo danh mục
- gom nhóm theo bộ sưu tập
- một sản phẩm có thể nằm trong nhiều bộ sưu tập

## 2. Đánh giá mô hình hiện tại

Hiện tại backend đã có các phần sau trong `products`:

- `category_id`
- `brand_id`
- `gender`
- `sport_type`
- `status`
- `is_featured`

Hiện tại backend **chưa có**:

- `product_type` rõ ràng
- bảng `collections`
- bảng nối `product_collections`
- category phân cấp cha-con

### 2.1 Điểm tốt của mô hình hiện tại

- `product_variants.size` là `varchar`, đủ linh hoạt cho cả size áo (`S`, `M`, `L`) và size giày (`39`, `40`, `41`)
- `sport_type` đã mở đường cho filter theo môn thể thao
- `category` và `brand` đã đủ để chạy catalog cơ bản

### 2.2 Điểm chưa đủ nếu muốn làm sản phẩm hoàn chỉnh

- Không tách được rõ `quần áo`, `giày`, `phụ kiện`
- Không hỗ trợ `một bộ sưu tập gồm nhiều loại sản phẩm`
- Không hỗ trợ tốt menu/catalog kiểu lớn như fashion-commerce
- Category hiện là phẳng, không thuận tiện cho cấu trúc kiểu:
  - Giày
  - Giày chạy bộ
  - Giày bóng đá
  - Giày training

## 3. Mô hình nghiệp vụ nên dùng

Tôi đề xuất dùng 4 lớp phân loại:

### 3.1 Product Type

Đây là lớp phân loại cấp cao nhất.

Ví dụ:

- `APPAREL`
- `FOOTWEAR`
- `ACCESSORY`
- `EQUIPMENT` (tùy chọn, để dành mở rộng)

Ý nghĩa:

- `APPAREL`: áo, quần, áo khoác, đồ bộ
- `FOOTWEAR`: giày chạy, giày bóng đá, giày training
- `ACCESSORY`: mũ, tất, túi, băng cổ tay
- `EQUIPMENT`: bóng, bình nước, thảm tập, dụng cụ nhẹ

### 3.2 Category

Category dùng để phân loại chi tiết theo nghiệp vụ bán hàng.

Ví dụ:

- Với `APPAREL`:
  - Áo thun
  - Áo khoác
  - Quần short
  - Quần legging
- Với `FOOTWEAR`:
  - Giày chạy bộ
  - Giày bóng đá
  - Giày futsal
  - Giày training
- Với `ACCESSORY`:
  - Tất
  - Mũ
  - Túi
  - Băng cổ tay

### 3.3 Collection

Collection là thực thể marketing và merchandising, không phải category.

Ví dụ:

- BST Mùa hè 2026
- BST Back To School
- BST Chạy bộ
- BST Bóng đá
- BST Training
- BST New Arrival

Một collection có thể chứa:

- nhiều category
- nhiều product type khác nhau
- nhiều brand khác nhau

### 3.4 Brand

Brand vẫn giữ nguyên vai trò hiện tại:

- Nike
- Adidas
- Puma
- Joma
- Anta

## 4. Thiết kế dữ liệu đề xuất

## 4.1 Giữ lại bảng hiện có

Giữ:

- `products`
- `product_variants`
- `product_images`
- `categories`
- `brands`

Không cần đập đi làm lại toàn bộ.

## 4.2 Sửa bảng `products`

### Cột mới nên thêm

- `product_type` varchar(30), not null
- `collection_primary_id` nullable, FK -> `collections.id` (tùy chọn)

### Vì sao cần `product_type`

`product_type` là lớp phân loại ổn định, ít thay đổi và được dùng thường xuyên trong:

- menu lớn
- filter cấp cao
- landing page riêng cho Quần áo / Giày / Phụ kiện
- rule hiển thị FE

### Giá trị đề xuất cho `product_type`

- `APPAREL`
- `FOOTWEAR`
- `ACCESSORY`
- `EQUIPMENT`

### Vì sao không dùng category thay cho product_type

Nếu chỉ dùng category:

- category sẽ phải gánh cả vai trò phân loại lớn lẫn phân loại chi tiết
- menu sẽ khó tổ chức
- query theo nhóm sản phẩm cấp cao sẽ rối

`product_type` nên là cột riêng.

## 4.3 Sửa bảng `categories`

### Cột mới nên thêm

- `parent_id` nullable, FK -> `categories.id`
- `product_type` nullable hoặc not null

### Khuyến nghị

Tôi khuyên thêm:

- `parent_id`
- `product_type`

Để category có thể phân cấp như sau:

- `FOOTWEAR`
  - Giày chạy bộ
  - Giày bóng đá
  - Giày futsal
- `APPAREL`
  - Áo
  - Quần
  - Áo khoác
- `ACCESSORY`
  - Mũ
  - Tất
  - Túi

Nếu muốn giữ scope nhỏ hơn ở vòng đầu:

- chỉ thêm `parent_id`
- chưa cần `product_type` ở category

Nhưng về lâu dài, gắn category với `product_type` sẽ sạch hơn.

## 4.4 Tạo bảng `collections`

Đây là bảng quan trọng nhất cho hướng bạn muốn.

### Mục đích

Lưu các bộ sưu tập độc lập với category.

### Cấu trúc đề xuất

- `id` uuid, PK
- `name` varchar(200), not null
- `slug` varchar(220), not null, unique
- `description` text, nullable
- `short_description` varchar(500), nullable
- `collection_type` varchar(30), not null
- `season` varchar(50), nullable
- `year` integer, nullable
- `banner_image_url` varchar(500), nullable
- `cover_image_url` varchar(500), nullable
- `status` varchar(20), not null
- `starts_at` timestamp, nullable
- `ends_at` timestamp, nullable
- `sort_order` integer, not null default 0
- `is_featured` boolean, not null default false
- `created_at` timestamp, not null
- `updated_at` timestamp, not null

### Enum gợi ý cho `collection_type`

- `SEASONAL`
- `SPORT`
- `CAMPAIGN`
- `CAPSULE`
- `NEW_ARRIVAL`

### Enum gợi ý cho `status`

- `DRAFT`
- `ACTIVE`
- `INACTIVE`
- `ARCHIVED`

## 4.5 Tạo bảng nối `product_collections`

### Mục đích

Cho phép một sản phẩm nằm trong nhiều bộ sưu tập.

### Cấu trúc đề xuất

- `id` uuid, PK
- `product_id` FK -> `products.id`, not null
- `collection_id` FK -> `collections.id`, not null
- `sort_order` integer, not null default 0
- `is_primary` boolean, not null default false
- `created_at` timestamp, not null

### Constraint

- unique(`product_id`, `collection_id`)

### Vì sao cần bảng nối

Vì một sản phẩm có thể đồng thời thuộc:

- BST Mùa hè 2026
- BST Running
- BST New Arrival

Nếu nhét collection vào 1 cột text trong `products`, bạn sẽ mất:

- tính chuẩn hóa
- filter tốt
- quản lý landing page collection
- quản trị merchandising

## 5. Quan hệ dữ liệu sau khi mở rộng

```text
product_types (logic / enum)
   |
   | 1-n
categories -- self parent_id
   |
   | 1-n
products
   | \
   |  \  n-n
   |   product_collections --- collections
   |
   | 1-n
product_variants
```

## 6. Ví dụ dữ liệu thực tế

### Ví dụ 1: Áo chạy bộ

- `product_type`: `APPAREL`
- `category`: `Áo running`
- `brand`: `Nike`
- collections:
  - `BST Mùa hè 2026`
  - `BST Running`

### Ví dụ 2: Giày bóng đá

- `product_type`: `FOOTWEAR`
- `category`: `Giày bóng đá`
- `brand`: `Adidas`
- collections:
  - `BST Sân cỏ nhân tạo`
  - `BST New Arrival`

### Ví dụ 3: Túi thể thao

- `product_type`: `ACCESSORY`
- `category`: `Túi`
- `brand`: `Puma`
- collections:
  - `BST Back To School`
  - `BST Phụ kiện tập luyện`

## 7. Những gì backend hiện tại nên sửa

## 7.1 Mức tối thiểu nên làm ngay

Nếu muốn thay đổi ít nhất nhưng đủ đi đúng hướng:

1. Thêm `product_type` vào `products`
2. Tạo bảng `collections`
3. Tạo bảng `product_collections`

Với 3 thay đổi này, hệ thống đã có thể:

- bán cả quần áo, giày, phụ kiện
- tạo page collection
- gắn nhiều sản phẩm khác loại vào cùng 1 BST

## 7.2 Mức nên làm để bền hơn

Ngoài 3 thay đổi trên, nên thêm:

4. `categories.parent_id`
5. `categories.product_type`

Lúc đó catalog sẽ sạch hơn rất nhiều.

## 7.3 Những gì chưa cần vội

Chưa cần thêm ngay:

- bảng `seasons` riêng
- bảng `campaigns` riêng
- bảng `tags` riêng
- dynamic attributes kiểu EAV

Lý do:

- sẽ làm phình scope
- giai đoạn này chưa cần
- `collections` đã giải quyết được phần lớn nhu cầu marketing

## 8. Ảnh hưởng tới API

Sau khi mở rộng dữ liệu, backend nên bổ sung các API sau:

### Public

- `GET /api/v1/collections`
- `GET /api/v1/collections/{slug}`
- `GET /api/v1/products?productType=FOOTWEAR`
- `GET /api/v1/products?collection=bst-mua-he-2026`

### Admin

- `GET /api/v1/admin/collections`
- `POST /api/v1/admin/collections`
- `PATCH /api/v1/admin/collections/{id}`
- `POST /api/v1/admin/products/{id}/collections`
- `DELETE /api/v1/admin/products/{id}/collections/{collectionId}`

## 9. Ảnh hưởng tới FE

Với mô hình mới, FE có thể làm:

- menu theo `Quần áo / Giày / Phụ kiện`
- dưới mỗi nhóm có category con
- section homepage theo collection
- landing page riêng cho từng BST
- trang listing lọc theo:
  - product type
  - category
  - brand
  - gender
  - sport type
  - collection

## 10. Khuyến nghị chốt cho dự án này

Tôi khuyên bạn chốt theo hướng sau:

### Bắt buộc

- thêm `product_type` vào `products`
- thêm `collections`
- thêm `product_collections`

### Nên làm cùng đợt

- thêm `parent_id` cho `categories`

### Tạm chưa làm

- module blog/news riêng
- tags động
- season table riêng
- campaign table riêng

## 11. Lộ trình sửa backend

### Phase 1

- migration thêm `product_type`
- migration tạo `collections`
- migration tạo `product_collections`
- entity/repository/service/controller cho collection
- cập nhật product create/update/detail/list

### Phase 2

- migration thêm `categories.parent_id`
- sửa admin category API
- sửa product filter/query theo category cây

### Phase 3

- seed data collection mẫu
- FE landing page collection
- homepage section theo collection

## 12. Kết luận

Để hỗ trợ đúng hướng sản phẩm bạn muốn, backend không cần viết lại từ đầu.

Cách đúng nhất là:

- giữ `products`, `variants`, `brands`, `categories`
- thêm `product_type`
- thêm `collections`
- thêm `product_collections`
- sau đó nếu cần thì nâng cấp category thành cây

Đây là phương án đủ chuẩn để:

- bán quần áo
- bán giày
- bán phụ kiện
- và cho phép mỗi nhóm có bộ sưu tập riêng, thậm chí một bộ sưu tập chứa nhiều nhóm sản phẩm khác nhau

