Tiếp tục dự án backend Spring Boot của tôi.

Phase C đã xong và đã có test safety net cho auth.
Bây giờ bắt đầu Phase D.

Nhiệm vụ:
Triển khai module catalog cho Phase 1, gồm:
- category
- brand
- product
- product variant
- product image

Mục tiêu:
- làm được admin CRUD nền tảng cho catalog
- làm được public APIs cho product listing và product detail
- bám đúng spec Phase 1 đã chốt
- chưa làm cart/order/payment/inventory ở bước này

Phạm vi chính xác cần làm:

1. Category
- admin create
- admin update
- public list categories

2. Brand
- admin create
- admin update
- public list brands

3. Product
- admin create
- admin update
- admin get detail nếu cần để phục vụ edit
- public product listing
- public product detail

4. Product Variant
- admin create variant
- admin update variant
- validate unique SKU
- validate stock/price theo schema rule

5. Product Image
- admin add image metadata
- support primary image
- support sort order

6. Public catalog behavior
- filter sản phẩm
- sort sản phẩm
- pagination
- product detail trả variants + images

Yêu cầu bắt buộc:
- bám đúng schema DB hiện có
- không tự ý đổi stack
- không tự ý đổi flow nghiệp vụ
- không thêm AI
- không mở rộng sang cart/order
- không tự ý thêm soft delete nếu chưa thật sự cần
- response phải bám ApiResponse chuẩn
- validation phải rõ ràng
- lỗi phải dùng status code hợp lý
- admin endpoints phải được bảo vệ đúng role
- public endpoints chỉ public đúng phần catalog read

Các rule cần giữ:
- SKU unique
- product có category và brand hợp lệ
- giá > 0
- stock_quantity >= 0
- reserved_quantity không cho client admin sửa bừa ngoài nghiệp vụ inventory
- product detail phải trả dữ liệu đủ cho frontend product page
- listing phải có filter/sort/pagination tối thiểu theo spec

Gợi ý triển khai:
- tách module rõ ràng: category, brand, product
- product variant và product image có thể nằm trong product module nếu hợp lý
- tạo DTO request/response riêng
- dùng mapper nếu cần
- viết repository/query phù hợp cho listing filter

Tôi muốn đầu ra sau khi bạn làm xong:
1. Tóm tắt đã làm gì
2. Danh sách file tạo/sửa
3. API nào đã xong
4. Validation/rule nào đã implement
5. Tradeoff bạn tự quyết
6. Những gì còn thiếu trong Phase D
7. Nếu có test, nói rõ test nào đã cover

Không cần làm sang module khác.
Chỉ tập trung hoàn thành Phase D thật gọn và đúng spec.
