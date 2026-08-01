# Kế hoạch tìm kiếm sản phẩm bằng hình ảnh

## 1. Mục tiêu

Xây dựng chức năng cho phép khách hàng tải lên hoặc chụp một ảnh và nhận danh sách sản phẩm có hình ảnh tương tự. Hệ thống sử dụng dịch vụ AI có sẵn, không tự huấn luyện model và không vận hành GPU.

Giải pháp được tách thành module riêng để có thể kiểm soát chi phí, lỗi, hàng đợi, việc index lại dữ liệu và thay đổi nhà cung cấp AI mà không ảnh hưởng chatbot hoặc nghiệp vụ commerce chính.

## 2. Hiện trạng dữ liệu đã kiểm tra trên Supabase

| Chỉ số | Số lượng |
|---|---:|
| Tổng sản phẩm | 228 |
| Sản phẩm ACTIVE | 107 |
| Sản phẩm INACTIVE | 121 |
| Sản phẩm có ít nhất một ảnh | 202 |
| Sản phẩm chưa có ảnh | 26 |
| Tổng ảnh trong `product_images` | 676 |
| Ảnh thuộc sản phẩm ACTIVE | 576 |
| Ảnh thuộc sản phẩm INACTIVE | 100 |
| Sản phẩm ACTIVE có ít nhất một ảnh | 102 |
| Sản phẩm ACTIVE chưa có ảnh | 5 |
| Ảnh ACTIVE trên Shopify CDN | 442 |
| Ảnh ACTIVE trên Cloudinary | 134 |
| Ảnh thiếu `public_id` | 142 |
| Ảnh INACTIVE dùng đường dẫn tương đối | 100 |

Supabase lưu metadata như `product_id`, `image_url`, `public_id`, màu sắc, thứ tự và trạng thái ảnh chính. File ảnh ACTIVE hiện đến từ hai nguồn: Shopify CDN và Cloudinary; không được giả định mọi ảnh đều có `public_id` Cloudinary. Phạm vi backfill mục tiêu là 576 ảnh của sản phẩm ACTIVE, nhưng chỉ đưa ảnh qua AI sau khi URL nguồn hợp lệ và ảnh tải/giải mã thành công. Năm sản phẩm ACTIVE chưa có ảnh không thể tham gia visual search cho tới khi được bổ sung ảnh.

## 3. Yêu cầu tổng hợp

### 3.1. Yêu cầu nghiệp vụ

| Mã | Yêu cầu |
|---|---|
| BR-01 | Người dùng có thể tải ảnh hoặc chụp ảnh để tìm sản phẩm tương tự. |
| BR-02 | Chỉ trả sản phẩm ACTIVE; ưu tiên sản phẩm còn hàng. |
| BR-03 | Cho phép kết hợp bộ lọc danh mục, giới tính và khoảng giá. |
| BR-04 | Hiển thị sản phẩm và ảnh catalog khớp nhất. |
| BR-05 | Thêm ảnh cho sản phẩm phải tự động tạo embedding; tạo sản phẩm chưa có ảnh không tạo embedding. |
| BR-06 | Khi bổ sung nghiệp vụ thay thế nội dung/URL ảnh, thao tác đó phải tự động tạo lại embedding; source hiện tại chưa có replace-image. |
| BR-07 | Xóa ảnh phải xóa embedding tương ứng. |
| BR-08 | Chuyển sản phẩm sang INACTIVE phải loại khỏi kết quả mà không cần xóa vector. |
| BR-09 | Chuyển sản phẩm về ACTIVE phải kiểm tra và bổ sung embedding còn thiếu. |
| BR-10 | Có thể retry hoặc reindex một ảnh, một sản phẩm hoặc toàn bộ catalog. |
| BR-11 | Có màn hình quản trị trạng thái READY/PENDING/FAILED, coverage và chi phí. |
| BR-12 | Không lưu ảnh tìm kiếm của khách nếu chưa có sự đồng ý. |

### 3.2. Yêu cầu kỹ thuật

| Mã | Yêu cầu |
|---|---|
| TR-01 | Tạo module Python/FastAPI riêng tên `visual-search-service`. |
| TR-02 | Dùng `voyage-multimodal-3.5` mặc định; provider abstraction cho phép đổi sang Cohere. |
| TR-03 | Lưu visual vector trong Supabase pgvector, tách khỏi `product_embeddings` hiện tại. |
| TR-04 | Dùng RabbitMQ để phân phối công việc index/reindex bất đồng bộ. |
| TR-05 | Spring Boot dùng transactional outbox để không mất sự kiện. |
| TR-06 | Consumer dùng manual ACK, xử lý idempotent, retry và dead-letter queue. |
| TR-07 | RabbitMQ chỉ chứa ID và metadata sự kiện, không chứa binary ảnh hoặc vector. |
| TR-08 | Visual service hỗ trợ Cloudinary và Shopify CDN qua allowlist; mọi ảnh đều được tải, kiểm tra và normalize/resize trước khi gửi AI. |
| TR-09 | Có reconciliation job định kỳ để phát hiện vector thiếu, lỗi hoặc cũ. |
| TR-10 | Có model versioning để reindex và rollback an toàn. |
| TR-11 | Có rate limit, usage log và ngân sách tháng. |
| TR-12 | Commerce vẫn hoạt động khi RabbitMQ hoặc AI provider bị lỗi. |

## 4. Kiến trúc mục tiêu

```text
Next.js storefront
        |
        | multipart image
        v
Spring Boot public API -----------------------+
        |                                      |
        | internal request                     | product/image transaction
        v                                      v
visual-search-service                    integration_outbox
        |                                      |
        |                                      v
        |                                 RabbitMQ
        |                                      |
        |                              indexing consumer
        |                                      |
        +<-------------------------------------+
        |
        +--> Cloudinary / Shopify CDN ------> tải, validate, normalize ảnh
        +--> Voyage/Cohere -------------------> tạo embedding
        +--> Supabase pgvector --------------> lưu/tìm vector
        |
        v
Spring Boot lọc ACTIVE, tồn kho, giá --> trả kết quả cho Next.js
```

### Phân chia trách nhiệm

| Thành phần | Trách nhiệm |
|---|---|
| Cloudinary | Lưu/phân phối ảnh được upload mới và hỗ trợ transformation. |
| Shopify CDN | Nguồn của phần lớn ảnh catalog ACTIVE hiện hữu; chỉ được đọc qua hostname allowlist. |
| Supabase | Lưu sản phẩm, metadata ảnh, visual vector, trạng thái và usage. |
| Spring Boot | Nghiệp vụ commerce, public API, outbox và xác thực/rate limit. |
| RabbitMQ | Phân phối job index/reindex bất đồng bộ. |
| Visual Search Service | Kiểm tra ảnh, gọi model, index và vector search. |
| Voyage/Cohere | Chuyển ảnh thành embedding. |
| Next.js | Camera/upload/crop và hiển thị kết quả. |

## 5. Đồng bộ khi dữ liệu thay đổi

| Thao tác | Hành vi cần thực hiện |
|---|---|
| Tạo sản phẩm chưa có ảnh | Không tạo embedding. |
| Thêm ảnh | Ghi outbox, publish RabbitMQ, tạo embedding cho ảnh mới. |
| Thay nội dung ảnh | So sánh hash/version; chỉ tạo lại khi nội dung thay đổi. |
| Đổi alt text, sort order, ảnh chính | Không tạo lại visual embedding. |
| Sửa tên, giá, tồn kho | Không tạo lại visual embedding. |
| Sửa category/gender/color | Cập nhật bộ lọc; MVP không trộn metadata vào vector. |
| Xóa ảnh | Xóa vector bằng foreign key `ON DELETE CASCADE`. |
| Chuyển INACTIVE | Giữ vector nhưng loại bằng truy vấn nghiệp vụ. |
| Chuyển ACTIVE | Tạo job cho các ảnh chưa có vector READY. |
| Đổi model | Tạo model version mới, reindex song song, chuyển active rồi mới xóa bản cũ. |

Luồng thêm/cập nhật ảnh:

```text
Upload Cloudinary hoặc thêm URL catalog hợp lệ
  -> lưu product_images + integration_outbox trong cùng transaction
  -> publisher gửi message persistent và chờ publisher confirm
  -> visual consumer đọc imageId
  -> lấy URL/public_id và trạng thái product mới nhất từ Supabase
  -> ImageSourceResolver chọn Cloudinary hoặc Shopify CDN
  -> tải, kiểm tra MIME/byte/pixel, normalize và resize
  -> tính SHA-256 trên bytes đã normalize
  -> gọi model nếu hash/model thay đổi
  -> upsert embedding
  -> ghi processed_event
  -> ACK
```

## 6. RabbitMQ topology

| Loại | Tên |
|---|---|
| Topic exchange | `catalog.events` |
| Main queue | `visual-search.indexing` |
| Retry queue | `visual-search.indexing.retry.30s` |
| Retry queue | `visual-search.indexing.retry.5m` |
| Retry queue | `visual-search.indexing.retry.1h` |
| Dead-letter queue | `visual-search.indexing.dlq` |

Routing keys:

```text
product.image.created
product.image.deleted
product.activated
product.deactivated
product.reindex.requested
```

Chỉ bổ sung `product.image.updated` khi source code có nghiệp vụ thay thế nội dung hoặc URL ảnh. Đổi alt text, màu metadata, sort order hoặc primary flag không phát sự kiện reindex. Khi cập nhật sản phẩm, chỉ phát `product.activated`/`product.deactivated` nếu trạng thái thực sự chuyển đổi.

Message chỉ chứa `eventId`, `eventType`, `eventVersion`, `productId`, `imageId`, `occurredAt` và `traceId`.

## 7. API dự kiến

### Public API qua Spring Boot

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/api/v1/products/search-by-image` | Nhận JPEG/PNG/WebP và trả sản phẩm tương tự. |
| GET | `/api/v1/visual-search/status` | Trạng thái khả dụng của tính năng. |

### Internal API của visual-search-service

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/internal/v1/search` | Tạo query vector và tìm ứng viên. |
| POST | `/internal/v1/index/images/{id}` | Index/reindex một ảnh. |
| POST | `/internal/v1/index/products/{id}` | Reindex một sản phẩm. |
| POST | `/internal/v1/index/backfill` | Backfill catalog. |
| GET | `/internal/v1/index/jobs/{id}` | Theo dõi job. |
| GET | `/internal/v1/admin/coverage` | Coverage READY/PENDING/FAILED. |
| GET | `/internal/v1/admin/usage` | Usage và chi phí ước tính. |
| POST | `/internal/v1/admin/retry-failed` | Retry lỗi. |
| GET | `/health/live` | Liveness. |
| GET | `/health/ready` | Readiness. |

Internal API không được public trực tiếp trong production.

## 8. Bảo mật và vận hành

| Hạng mục | Mức đề xuất |
|---|---|
| File upload | Tối đa 5 MB; JPEG, PNG, WebP. |
| Pixel decode | Tối đa 16 triệu pixel; catalog hiện hữu đã audit có ảnh tới 4000 x 4000, sau đó resize tối đa 1024 x 1024 trước khi gửi model. |
| Ảnh gửi model | Resize khoảng 1 MP. |
| Catalog URL allowlist | Chỉ HTTPS; `res.cloudinary.com/<configured-cloud-name>/...` và `cdn.shopify.com/...`. |
| Cloudinary transformation | `c_limit,w_1024,h_1024,q_auto:good,f_jpg`. |
| Shopify CDN | Tải URL hợp lệ rồi normalize/resize trong Python; không nối Cloudinary transformation. |
| Download | Timeout 10 giây, giới hạn redirect và số byte. |
| Rate limit | Khởi điểm 10 request/phút/user hoặc IP. |
| RabbitMQ | Durable exchange/queue, persistent message, manual ACK. |
| Consumer | Prefetch 3-5; idempotent theo `eventId` và image hash. |
| Ảnh truy vấn | Không lưu mặc định. |
| Secret | Chỉ dùng biến môi trường; không đưa vào frontend/message/log. |

Retry gợi ý: 30 giây, 5 phút, 1 giờ, sau đó DLQ. Retry timeout, HTTP 429/5xx và lỗi mạng; không retry vô hạn với ảnh hỏng hoặc MIME không hợp lệ.

## 9. Chi phí dự kiến

Giá tham chiếu của Voyage tại thời điểm lập kế hoạch:

- Ảnh khoảng 1.000 x 1.000: khoảng **$0,0006/ảnh**.
- Mức tối đa sau xử lý: khoảng **$0,0012/ảnh**.
- Voyage đang công bố hạn mức miễn phí 150 tỷ pixel cho multimodal embedding.
- Nguồn: <https://docs.voyageai.com/docs/pricing>.

Giá và hạn mức có thể thay đổi; implementation phải ghi usage thực tế và cho phép đặt ngân sách.

### Backfill

| Phạm vi | Số ảnh | Khoảng 1 MP | Kịch bản tối đa |
|---|---:|---:|---:|
| Tối đa toàn bộ ảnh ACTIVE hợp lệ | 576 | $0,35 | $0,69 |
| Toàn bộ catalog | 676 | $0,41 | $0,81 |

Nếu hạn mức miễn phí còn hiệu lực và tài khoản chưa dùng hết, chi phí backfill có thể là $0.

### Lưu lượng tìm kiếm

| Lượt tìm/tháng | Khoảng 1 MP | Kịch bản tối đa |
|---:|---:|---:|
| 1.000 | $0,60 | $1,20 |
| 10.000 | $6 | $12 |
| 50.000 | $30 | $60 |
| 100.000 | $60 | $120 |
| 1.000.000 | $600 | $1.200 |

### Hạ tầng

| Thành phần | Chi phí tăng thêm ở quy mô hiện tại |
|---|---|
| 576 vector 1024 chiều | Khoảng 2,25 MB dữ liệu thô; gần như không đáng kể. |
| Supabase | Gần $0 nếu gói hiện tại còn dung lượng. |
| RabbitMQ local Docker | $0. |
| RabbitMQ và service chạy chung server | Gần $0 nếu còn CPU/RAM. |
| Cloudinary transformations | Phụ thuộc gói Cloudinary hiện tại. |
| AI API | Chủ yếu theo số lượt tìm ở bảng trên. |

Ngân sách AI nên đặt ban đầu là **$10-20/tháng** cho MVP, kèm cảnh báo ở 70%, 90% và cơ chế ngừng/cắt giảm ở 100%.

## 10. Kế hoạch thực hiện

| Giai đoạn | Công việc | Kết quả | Ước lượng |
|---|---|---|---:|
| 0 | Audit URL ảnh và POC Voyage trên mẫu Cloudinary/Shopify; định nghĩa ground truth | Xác nhận pipeline/model có chất lượng đủ dùng | 2-4 ngày |
| 1 | Chốt cấu hình Supabase, Cloudinary, Shopify allowlist, Voyage và RabbitMQ | Môi trường sẵn sàng | 0,5-1 ngày |
| 2 | Migration schema visual search, model version và outbox | Database hoàn chỉnh | 1-2 ngày |
| 3 | RabbitMQ exchange, main/retry/DLQ và message contract | Messaging hoạt động | 1-2 ngày |
| 4 | Khởi tạo FastAPI, fake provider và provider abstraction | Module độc lập, test được | 1-2 ngày |
| 5 | ImageSourceResolver, download, validation, normalize và hashing | Pipeline Cloudinary/Shopify an toàn | 2-3 ngày |
| 6 | Voyage document/query embedding và usage log | AI provider hoạt động | 1-2 ngày |
| 7 | Consumer idempotent, manual ACK, retry và DLQ | Index worker hoạt động | 2-3 ngày |
| 8 | Spring transactional outbox, publisher confirms và event production | Catalog phát sự kiện tin cậy | 2-4 ngày |
| 9 | Exact cosine search, group theo product và filter | Internal search API | 2-3 ngày |
| 10 | Backfill ảnh ACTIVE hợp lệ hoàn toàn qua RabbitMQ | Catalog được index, có report lỗi nguồn | 1-2 ngày |
| 11 | Spring public API, timeout và rate limit riêng cho visual search | API storefront | 2-3 ngày |
| 12 | Next.js camera/upload/preview/crop/results | Giao diện tìm kiếm ảnh | 3-5 ngày |
| 13 | Admin coverage nguồn ảnh/embedding, usage và retry | Kiểm soát vận hành | 3-5 ngày |
| 14 | Reconciliation và model versioning/rollback | Tự sửa và nâng cấp model | 2-3 ngày |
| 15 | Benchmark phân tầng, fault testing và hardening | Sẵn sàng nghiệm thu | 4-7 ngày |

RabbitMQ và transactional outbox là thành phần bắt buộc vì đề tài tập trung Spring Boot microservices, RabbitMQ và Python. Tổng phạm vi đầy đủ dự kiến khoảng 28-45 ngày công, phụ thuộc việc xử lý/migrate ảnh Shopify và chất lượng POC. Có thể demo theo từng phase nhưng không bỏ RabbitMQ khỏi pipeline index/backfill.

## 11. Tiêu chí nghiệm thu

| Tiêu chí | Mục tiêu |
|---|---|
| Embedding coverage | `READY / số ảnh ACTIVE có nguồn hợp lệ và decode được` tối thiểu 98%, mục tiêu 100%. |
| Product visual coverage | Báo riêng `sản phẩm ACTIVE có >= 1 READY / tổng sản phẩm ACTIVE`; chưa đặt 100% khi còn sản phẩm thiếu ảnh. |
| Invalid source coverage | Thống kê riêng URL tương đối, host ngoài allowlist, ảnh tải/decode lỗi và thiếu `public_id`. |
| Ảnh mới trở nên searchable | Không quá 1 phút trong điều kiện bình thường. |
| Search latency p95 | Không quá 3 giây. |
| Recall@5 | Tối thiểu 80% trên benchmark có ground truth, không dùng bản sao/crop trực tiếp từ catalog. |
| Category accuracy | Tối thiểu 90%, báo riêng theo áo/quần/giày/phụ kiện. |
| Message bị mất | 0. |
| Message trùng làm hỏng dữ liệu | 0. |
| Commerce hoạt động khi RabbitMQ/AI lỗi | Có. |
| Retry, DLQ, usage và budget visibility | Có. |
| Có thể đổi model và rollback | Có. |

## 12. Quyết định khuyến nghị

Sử dụng Supabase để lưu metadata và vector; Cloudinary cho ảnh upload mới; hỗ trợ đọc catalog hiện hữu từ Cloudinary và Shopify CDN qua allowlist; Spring Boot transactional outbox; RabbitMQ bắt buộc để phân phối job index/reindex/backfill; module `visual-search-service` dùng Voyage multimodal 3.5; Next.js cung cấp camera/upload/crop. Request tìm kiếm của khách vẫn gọi đồng bộ Spring Boot -> FastAPI để trả kết quả ngay. Không tự train model, không dùng bảng text embedding hiện tại cho ảnh và không đưa file ảnh vào RabbitMQ.
