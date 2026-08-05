# Codex implementation brief: Product Hybrid Search

## 1. Mục tiêu

Triển khai hoàn chỉnh chức năng tìm kiếm sản phẩm trên storefront, hỗ trợ đồng thời:

1. Tìm theo từ khóa chính xác và gần đúng.
2. Hiểu câu truy vấn mua sắm bằng tiếng Việt và tách thành các bộ lọc có cấu trúc.
3. Tìm kiếm theo ngữ nghĩa bằng text embedding.
4. Kết hợp keyword search và semantic search thành một danh sách có thứ tự liên quan tốt.
5. Tích hợp autocomplete, submit search, filter chips, sidebar filter, phân trang và trạng thái lỗi vào thanh tìm kiếm hiện tại.
6. Vẫn tìm kiếm được bằng keyword/structured filter nếu OpenAI hoặc `chatbot-service` tạm thời không khả dụng.

Ví dụ truy vấn phải hỗ trợ:

- `giày Nike nam màu đen sân cỏ nhân tạo dưới 2 triệu size 42`
- `áo chạy bộ nữ thoáng khí màu hồng dưới 1 triệu size M`
- `giày đá banh tốc độ cho tiền đạo`
- `áo MU sân nhà`
- `đồ chạy bộ mặc trời nóng`
- `giay co nhan tao nike den`

Không triển khai hoặc tạo phụ thuộc mới tới:

- `ai_forecasting_service`
- `chatbot-admin-service`

Đọc toàn bộ tài liệu này trước khi sửa code. Thực hiện theo từng phase nhỏ, chạy kiểm thử sau mỗi phase và không làm hỏng commerce, visual search, chatbot khách hàng, authentication, cart hoặc checkout hiện tại.

---

## 2. Hiện trạng đã xác minh

### 2.1. Kiến trúc

- Storefront: Next.js trong `frontend/storefront/`.
- Backend public API: Spring Boot trong `backend/`, cấu hình local ở cổng `8082`.
- Chatbot khách hàng: FastAPI/Python trong `chatbot-service/`, cấu hình local ở cổng `8002`.
- Database: Supabase PostgreSQL, đã có extension `pgvector`.
- Redis: cổng local `6379`.
- RabbitMQ: cổng local `5672`.
- Visual search là hệ thống riêng, không được trộn text vector vào schema `visual_search`.

### 2.2. Text search hiện tại

- Header hiện submit form `GET /tim-kiem?q=...`.
- Storefront gọi `GET /api/v1/products?q=...`.
- Backend dùng `ILIKE '%keyword%'` trên tên và mô tả ngắn.
- Autocomplete hiện có endpoint:

```http
GET /api/v1/products/search-suggestions?q=...
```

- `ProductListQuery` đã có các filter:
  - `q`, `keyword`
  - `categoryId`, `categorySlug`
  - `brandId`, `brandSlug`
  - `minPrice`, `maxPrice`
  - `size`, `color`
  - `discount`
  - `gender`
  - `sportType`
  - `surface`
  - `productType`
  - `collection`
  - `featured`

### 2.3. Hybrid search hiện có trong chatbot-service

`chatbot-service` đã có các thành phần có thể tái sử dụng:

- `app/services/product_search_service.py`
- Structured query parser.
- Product filters.
- Keyword retriever.
- OpenAI embedder.
- Supabase pgvector retriever.
- Reciprocal Rank Fusion.
- Heuristic reranker.
- Rule-based synonym rewrite.
- LLM rewrite fallback.
- Fail-safe keyword-only khi vector retrieval lỗi.

Không viết lại toàn bộ pipeline này trong Java nếu không có lý do kỹ thuật được chứng minh bằng test/benchmark.

### 2.4. Dữ liệu Supabase tại thời điểm audit

- `products` ACTIVE: 102.
- `products` INACTIVE: 126.
- `product_embeddings`: 103 vector, 1536 chiều.
- Sản phẩm ACTIVE có text embedding: **0/102**.
- Toàn bộ 103 text embedding hiện thuộc sản phẩm INACTIVE.
- 52/102 sản phẩm ACTIVE có `attributes`, nhưng các key chủ yếu là metadata import:
  - `group`
  - `importedColor`
  - `seedType`
  - `source`
  - `sourceUrl`
- `product_type`:
  - 52 sản phẩm ACTIVE là `APPAREL`.
  - 50 sản phẩm ACTIVE là `NULL`.
- `sport_type`:
  - 52 `running`.
  - 50 `Bong da`.
- Variant color chứa cả màu và edition/kit type, ví dụ:
  - màu thật: `Black`, `Blue`, `White`, `Pink`
  - mã màu: `R009 All Black`
  - không phải màu: `Sân nhà`, `Sân khách`, `Event Pack`, `Shadow`, `MIJ`
- Khoảng giá variant ACTIVE hiện khoảng 230.000 đến 8.490.000 VND.

Text semantic search chưa thể hoạt động đúng trước khi backfill embedding cho sản phẩm ACTIVE.

---

## 3. Kiến trúc bắt buộc

Triển khai theo mô hình:

```text
Storefront
   |
   | GET /api/v1/products/hybrid-search?q=...
   v
Spring Boot Backend :8082
   |
   |-- validate, rate limit, pagination, cache
   |-- enrich dữ liệu commerce mới nhất
   |-- keyword/structured fallback
   |
   v
chatbot-service :8002
   |
   |-- structured query parser
   |-- keyword retrieval
   |-- semantic retrieval
   |-- RRF fusion
   |-- heuristic/business reranking
   |
   v
Supabase PostgreSQL + pgvector
```

Nguyên tắc:

1. Browser chỉ gọi Spring Boot, không gọi trực tiếp `chatbot-service`.
2. OpenAI API key và database credentials không được đưa ra frontend.
3. Spring Boot là nguồn sự thật cuối cùng cho status, giá, tồn kho, ảnh và quyền hiển thị sản phẩm.
4. Nếu semantic search lỗi, backend tự fallback sang keyword/structured search.
5. Search không được làm checkout/cart phụ thuộc vào `chatbot-service`.
6. Structured filters là điều kiện cứng; semantic similarity không được vượt qua điều kiện sai giá, size, giới tính hoặc trạng thái.
7. Không dùng visual image embeddings cho text search.

---

## 4. API contract

### 4.1. Internal hybrid search API

Thêm vào `chatbot-service`:

```http
POST /internal/v1/product-search
X-Internal-Token: <shared-secret>
Content-Type: application/json
```

Request:

```json
{
  "query": "giày Nike nam màu đen sân cỏ nhân tạo dưới 2 triệu size 42",
  "page": 1,
  "limit": 20,
  "filters": {
    "categoryId": null,
    "categorySlug": null,
    "brandId": null,
    "brandSlug": null,
    "gender": null,
    "sportType": null,
    "productType": null,
    "surface": null,
    "color": null,
    "size": null,
    "minPrice": null,
    "maxPrice": null,
    "discount": null,
    "inStockOnly": true
  }
}
```

Response:

```json
{
  "items": [
    {
      "productId": "uuid",
      "keywordRank": 1,
      "semanticRank": 3,
      "semanticScore": 0.81,
      "fusionScore": 0.032,
      "matchedReasons": ["BRAND", "CATEGORY", "SEMANTIC"]
    }
  ],
  "total": 12,
  "parsedQuery": {
    "normalized": "giay nike nam mau den san co nhan tao duoi 2 trieu size 42",
    "semanticText": "giày đá bóng",
    "category": "Giày Đá Bóng Cỏ Nhân Tạo",
    "brand": "Nike",
    "gender": "MEN",
    "sportType": "Bong da",
    "productType": "FOOTWEAR",
    "surface": "TF",
    "colorFamily": "BLACK",
    "size": "42",
    "minPrice": null,
    "maxPrice": 2000000,
    "featureHints": []
  },
  "searchMode": "HYBRID",
  "processingTimeMs": 180
}
```

Yêu cầu:

- Internal token dùng constant-time comparison.
- Không log token, API key, full vector hoặc database URL.
- `query` phải trim, giới hạn độ dài hợp lý, từ chối control characters.
- `page >= 1`.
- `1 <= limit <= 100`, public storefront mặc định 20.
- Timeout phải cấu hình bằng env.
- Internal response không cần trả toàn bộ dữ liệu product; chỉ trả ID, rank, score và matched reasons.

### 4.2. Public Spring API

Thêm:

```http
GET /api/v1/products/hybrid-search
```

Query params:

```text
q
page
limit
categoryId
categorySlug
brandId
brandSlug
gender
sportType
productType
surface
color
size
minPrice
maxPrice
discount
sort
```

Response tiếp tục dùng product list DTO hiện tại để storefront không cần duy trì hai kiểu Product Card. Bổ sung metadata:

```json
{
  "data": [],
  "meta": {
    "page": 1,
    "size": 20,
    "total": 12,
    "totalPages": 1,
    "searchMode": "HYBRID",
    "parsedQuery": {},
    "fallbackReason": null,
    "processingTimeMs": 210
  }
}
```

Spring Boot phải:

1. Gọi internal search.
2. Lấy IDs theo đúng thứ tự rank.
3. Query lại sản phẩm ACTIVE cùng brand/category/variant/image.
4. Loại sản phẩm không còn ACTIVE.
5. Áp dụng lại giá, size, màu, tồn kho và filter commerce ở backend.
6. Giữ nguyên thứ tự rank sau khi enrich.
7. Không trả variant INACTIVE.
8. Không tin giá, tồn kho hoặc status từ internal service.

Fallback:

```text
internal timeout/5xx/invalid response
    -> ProductService.listPublic(...)
    -> searchMode = KEYWORD_FALLBACK
    -> response vẫn là 200 nếu fallback thành công
```

Không trả chi tiết lỗi upstream cho browser.

### 4.3. Suggestions API

Giữ endpoint hiện tại:

```http
GET /api/v1/products/search-suggestions?q=...
```

Mở rộng response để hỗ trợ:

- keyword suggestion
- category suggestion
- brand suggestion
- product suggestion có thumbnail và min price

Không gọi OpenAI cho autocomplete.

Giới hạn tối đa khoảng 8–10 mục, debounce ở frontend 250–300 ms.

---

## 5. Structured query parser

Parser phải ưu tiên deterministic/rule-based, không gọi LLM cho truy vấn thông thường.

### 5.1. Nguồn từ điển

Đọc brand và category hợp lệ từ database/cache, không hardcode toàn bộ catalog.

Từ điển tĩnh chỉ dùng cho:

- gender
- price expressions
- size expressions
- surface
- color family
- sport aliases
- product type aliases
- Vietnamese stop words
- Vietnamese không dấu/có dấu

### 5.2. Các thuộc tính cần hiểu

#### Gender

```text
nam, men, male        -> MEN
nữ, nu, women, female -> WOMEN
unisex                -> UNISEX
```

Khi tìm `MEN` hoặc `WOMEN`, có thể bao gồm `UNISEX` theo rule nghiệp vụ hiện tại.

#### Price

Phải hiểu tối thiểu:

```text
dưới 2 triệu
duoi 2tr
không quá 1.500.000
trên 500k
từ 1 triệu đến 2 triệu
1tr5
1.5 triệu
```

Chuẩn hóa sang VND integer/decimal. Không để LLM tự tạo giá.

#### Size

```text
size M
cỡ M
size 42
cỡ 42
```

Không nhầm `size` sản phẩm với `limit` phân trang.

#### Surface

```text
cỏ thật, san co that -> FG
cỏ nhân tạo, san co nhan tao -> TF/AG tùy taxonomy
futsal, sân trong nhà -> IC
```

Không ép `TF` và `AG` thành một giá trị nếu dữ liệu catalog đã phân biệt.

#### Product type

```text
áo, quần, đồ mặc -> APPAREL
giày, boots      -> FOOTWEAR
phụ kiện         -> ACCESSORY
thiết bị         -> EQUIPMENT
```

#### Color family

Chuẩn hóa tối thiểu:

```text
đen/black
trắng/white
xanh dương/blue/navy
xanh lá/green
đỏ/red
hồng/pink
vàng/yellow
xám/gray/grey
nâu/brown
cam/orange
tím/purple/violet
be/beige
```

Không coi `Sân nhà`, `Sân khách`, `Event Pack`, `Shadow`, `MIJ` là color family.

### 5.3. Quy tắc kết hợp

- Filter do người dùng chọn trực tiếp trên UI có độ ưu tiên cao hơn filter parser suy ra.
- Parser phải trả lại phần text còn lại làm `semanticText`.
- SKU, tên model, brand và tên sản phẩm chính xác không được loại khỏi semantic/keyword text một cách làm mất nghĩa.
- Structured filter phải áp dụng ở cả keyword branch và vector branch.
- Parser lỗi phải fallback về toàn bộ query như keyword/semantic text, không làm request thất bại.

### 5.4. LLM fallback

Không bật LLM parser mặc định ở phase đầu.

Nếu triển khai LLM fallback:

- Chỉ gọi khi deterministic parser đánh dấu query mơ hồ hoặc pipeline không có kết quả.
- Dùng structured JSON output với enum/schema chặt.
- Chỉ cho phép các trường filter đã định nghĩa.
- Không cho model tạo SQL.
- Validate mọi giá trị với catalog/database.
- Có timeout, rate limit, cache và fail-safe.

---

## 6. Search document và embedding

### 6.1. Model

Giữ cấu hình hiện tại để tiết kiệm migration và code:

```env
EMBEDDING_MODEL=text-embedding-3-small
EMBEDDING_DIMS=1536
```

Không trộn embeddings từ model khác nhau trong cùng một cột/index.

### 6.2. Search document

Mỗi sản phẩm có một canonical text document, ví dụ:

```text
Tên: Nike Mercurial Vapor 16
Danh mục: Giày Đá Bóng Cỏ Nhân Tạo
Thương hiệu: Nike
Loại sản phẩm: FOOTWEAR
Môn thể thao: Bóng đá
Giới tính: MEN UNISEX
Mặt sân: TF cỏ nhân tạo
Màu: Đen Black R009 All Black
Size: 39 40 41 42 43 44
Mô tả: Giày tốc độ nhẹ phù hợp cầu thủ chạy cánh
Thuộc tính: lightweight speed breathable
```

Không đưa các giá trị thay đổi thường xuyên vào embedding:

- price
- compare-at price
- stock
- reserved quantity
- sales count

Những giá trị này phải được filter/rerank trực tiếp từ database.

### 6.3. Schema

Ưu tiên mở rộng `product_embeddings` theo cách tương thích:

```text
product_id
embedding vector(1536)
document_text
embedding_model
embedding_dimensions
content_hash
status
last_error
updated_at
```

Hoặc tạo bảng versioned mới nếu migration an toàn hơn:

```text
product_search_documents
```

Quyết định phải:

- giữ được rollback
- không truncate dữ liệu trong migration
- không sửa `visual_search.image_embeddings`
- có unique key theo product/model nếu hỗ trợ nhiều model
- có foreign key `ON DELETE CASCADE`

Với catalog khoảng 102 ACTIVE products, exact cosine search đủ nhanh. Không thêm ANN index mới nếu `EXPLAIN ANALYZE` và benchmark không chứng minh cần thiết. Index IVFFlat hiện có không được giả định là luôn tốt khi dữ liệu quá nhỏ hoặc filter mạnh.

### 6.4. Backfill

Sửa `chatbot-service/scripts/generate_embeddings.py` hoặc tạo script idempotent mới:

```text
chatbot-service/scripts/backfill_product_search_embeddings.py
```

Yêu cầu:

- Mặc định dry-run.
- Chỉ index sản phẩm ACTIVE.
- Batch embedding.
- Upsert idempotent.
- Skip khi `content_hash` không đổi.
- Ghi model/dimensions.
- Báo counts:
  - active products
  - already fresh
  - missing
  - updated
  - failed
  - inactive stale rows
- Không in API key, DB password hoặc vector.
- Không tự xóa stale rows trừ khi có cờ explicit.
- Có chế độ kiểm tra coverage không ghi dữ liệu.

Acceptance:

```text
ACTIVE embedding coverage >= 98%
dimension = 1536
model = text-embedding-3-small
0 embedding trỏ tới product không tồn tại
```

---

## 7. Keyword và full-text retrieval

Không chỉ dựa vào `ILIKE '%whole query%'`.

Triển khai theo thứ tự:

1. Exact match/normalized match cho SKU, product name, brand, category.
2. PostgreSQL full-text search cho canonical search document.
3. Trigram/unaccent fallback cho typo và tiếng Việt không dấu.
4. Existing `ILIKE` fallback nếu extension/index chưa khả dụng.

Nếu thêm extension:

```sql
create extension if not exists unaccent;
create extension if not exists pg_trgm;
```

Mọi extension/migration phải được kiểm tra trên Supabase và Testcontainers trước khi áp dụng production.

Search document FTS nên có trọng số:

- A: product name, SKU
- B: brand, category
- C: sport, product type, surface, color family
- D: description và attributes

Không nối SQL từ input. Mọi query phải parameterized.

---

## 8. Hybrid fusion và ranking

Chạy keyword và vector retrieval song song khi có thể.

Candidate pool ban đầu:

```text
keyword candidates: khoảng 2x–4x page size
semantic candidates: khoảng 2x–4x page size
```

Dùng Reciprocal Rank Fusion:

```text
score =
    keywordWeight  / (rrfK + keywordRank)
  + semanticWeight / (rrfK + semanticRank)
```

Giá trị khởi đầu:

```text
rrfK = 60
keywordWeight = 1.2
semanticWeight = 1.0
```

Business reranking sau fusion:

1. exact SKU
2. exact product name
3. exact brand/model token
4. exact category
5. satisfies all structured filters
6. in stock
7. hybrid fusion score
8. best-selling/featured làm tie-breaker
9. product ID làm deterministic final tie-breaker

Không cộng trực tiếp raw FTS score với cosine similarity.

Có similarity threshold cấu hình được, nhưng không đặt quá cao trước benchmark tiếng Việt. Semantic-only candidates dưới threshold phải bị loại.

---

## 9. Chuẩn hóa dữ liệu

### 9.1. Product type

Backfill 50 ACTIVE products đang `product_type IS NULL`.

Không suy luận chỉ dựa trên tên một cách mù quáng. Dùng category mapping có kiểm tra:

```text
Áo/Quần -> APPAREL
Giày -> FOOTWEAR
Phụ kiện -> ACCESSORY
Thiết bị -> EQUIPMENT
```

Tạo report dry-run trước khi update.

### 9.2. Color family

Bổ sung trường chuẩn hóa, ưu tiên:

```text
product_variants.color_family
product_variants.edition
```

Giữ nguyên `color` làm display value để không phá UI/catalog.

Migration/backfill phải idempotent và có report giá trị chưa map được.

### 9.3. Searchable attributes

Không đưa `sourceUrl` hoặc metadata import không liên quan vào semantic document.

Chỉ whitelist các attribute nghiệp vụ:

```text
material
fit
weather
features
surface
position
team
technology
```

Không index tùy ý toàn bộ JSONB vì có thể chứa dữ liệu rác hoặc URL.

---

## 10. Cache, timeout và rate limit

### 10.1. Redis cache

Cache query embedding:

```text
search:embedding:<model>:<normalized-query-hash>
TTL khoảng 7 ngày
```

Cache suggestions:

```text
search:suggest:<normalized-prefix>
TTL khoảng 10 phút
```

Cache hybrid candidate IDs:

```text
search:hybrid:<query-hash>:<filter-hash>:<page>
TTL khoảng 2–5 phút
```

Không cache giá/tồn kho trong thời gian dài. Spring Boot luôn enrich commerce data mới nhất trước khi trả response.

Không đưa raw user query có dữ liệu nhạy cảm vào Redis key; dùng hash.

### 10.2. Timeout

Giá trị khởi đầu:

```text
internal search timeout: 2–3 giây
OpenAI embedding timeout: cấu hình, tối đa hợp lý
```

Nếu timeout, fallback keyword ngay; không retry đồng bộ nhiều lần làm người dùng chờ.

### 10.3. Rate limit

Áp dụng rate limit riêng cho public hybrid search theo:

```text
authenticated user ID
hoặc storefront session ID
```

Không dùng IP làm khóa duy nhất khi session đã tồn tại.

---

## 11. Đồng bộ embedding

### 11.1. MVP

Trong phase đầu:

- Chạy backfill idempotent.
- Khi product/brand/category/variant searchable fields thay đổi, enqueue reindex bất đồng bộ.
- Không chặn transaction lưu sản phẩm để chờ OpenAI.

### 11.2. Outbox/RabbitMQ

Tận dụng pattern transactional outbox hiện có:

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_STATUS_CHANGED
VARIANT_UPDATED
BRAND_UPDATED
CATEGORY_UPDATED
    -> integration_outbox
    -> RabbitMQ product-search.indexing
    -> worker
    -> rebuild canonical document
    -> upsert embedding
```

Chỉ re-embed khi `content_hash` thay đổi.

Thay đổi chỉ liên quan:

- price
- stock
- reserved quantity

không re-embed, nhưng phải làm cache kết quả ngắn hạn được refresh/invalidate phù hợp.

Event chỉ chứa ID và metadata cần thiết, không chứa full vector, API key hoặc database credential.

---

## 12. Storefront UX

### 12.1. Header search

Tách search input hiện tại trong `Header.tsx` thành client component có:

- controlled input
- debounce 250–300 ms
- suggestions dropdown
- keyboard navigation
- `ArrowUp`, `ArrowDown`, `Enter`, `Escape`
- focus management
- loading state
- click outside
- screen-reader labels
- giữ camera/visual search button hiện tại

Không làm thay đổi hoặc phá `VisualSearchDialog`.

### 12.2. Suggestions dropdown

Hiển thị các nhóm phù hợp:

```text
Từ khóa
Danh mục
Thương hiệu
Sản phẩm
```

Product suggestion gồm:

- thumbnail
- name
- brand/category ngắn
- min price

Không hiển thị sản phẩm INACTIVE hoặc hết hàng nếu policy storefront là in-stock only.

### 12.3. Search results page

Mở rộng `/tim-kiem` để giữ tất cả filter trong URL.

Hiển thị:

- query người dùng
- tổng số kết quả
- parsed filter chips
- sidebar filters
- sort
- grid sản phẩm hiện tại
- pagination
- loading skeleton
- empty state
- fallback state nhẹ nhàng

Ví dụ chips:

```text
Nike x
Nam x
Cỏ nhân tạo x
Màu đen x
Dưới 2.000.000đ x
Size 42 x
```

Khi xóa chip:

- cập nhật URL
- giữ query text
- chạy lại search

Filter người dùng chọn trực tiếp phải override filter parser suy ra.

### 12.4. Empty state

Nếu không có kết quả:

- cho biết filter nào đang áp dụng
- đề xuất bỏ bớt filter
- có thể thử query đã rewrite
- không tự hiển thị sản phẩm không liên quan như thể là kết quả chính xác

---

## 13. Observability và analytics

Ghi log có cấu trúc, không lưu dữ liệu nhạy cảm:

```text
request_id
normalized_query_hash
search_mode
parsed_filter_names
keyword_hit_count
semantic_hit_count
result_count
latency_ms
fallback_reason
embedding_cache_hit
```

Không log:

- API key
- JWT
- DB URL/password
- full embedding vector
- raw query nếu chưa có chính sách privacy phù hợp

Analytics tối thiểu:

- search submitted
- result clicked
- add-to-cart after search
- zero-result query
- fallback rate

Chỉ dùng analytics để đánh giá relevance; không làm request tìm kiếm phụ thuộc analytics.

---

## 14. Security

- Internal endpoint bắt buộc internal token.
- Không expose `OPENAI_API_KEY`.
- Không expose `DB_READ_URL` hoặc `DB_WRITE_URL`.
- Parameterized SQL.
- Validate enum/filter values.
- Giới hạn query length và pagination.
- Rate limit public endpoint.
- Timeout mọi external call.
- Không trả stack trace/upstream response cho client.
- Không log secrets/vector.
- Không cho LLM sinh hoặc thực thi SQL.
- Chỉ trả sản phẩm ACTIVE được backend xác nhận.
- Không tin product IDs từ internal service nếu không query/enrich lại.

---

## 15. Environment variables

### Backend

Thêm vào `.env.example`, không commit secret thật:

```env
PRODUCT_HYBRID_SEARCH_ENABLED=false
PRODUCT_SEARCH_SERVICE_URL=http://127.0.0.1:8002
PRODUCT_SEARCH_INTERNAL_TOKEN=change-me
PRODUCT_SEARCH_TIMEOUT_SECONDS=3
PRODUCT_SEARCH_RATE_LIMIT_PER_MINUTE=30
```

### chatbot-service

Thêm/chuẩn hóa:

```env
PRODUCT_SEARCH_INTERNAL_TOKEN=change-me
EMBEDDING_MODEL=text-embedding-3-small
EMBEDDING_DIMS=1536
PRODUCT_SEARCH_SEMANTIC_ENABLED=true
PRODUCT_SEARCH_LLM_REWRITE_ENABLED=false
PRODUCT_SEARCH_RRF_K=60
PRODUCT_SEARCH_KEYWORD_WEIGHT=1.2
PRODUCT_SEARCH_SEMANTIC_WEIGHT=1.0
PRODUCT_SEARCH_MIN_SIMILARITY=0.0
PRODUCT_SEARCH_QUERY_CACHE_TTL_SECONDS=604800
```

Backend và chatbot internal token phải khớp.

Feature flag mặc định `false` cho checkout mới; chỉ bật sau khi migrations, backfill và acceptance tests đạt.

---

## 16. Testing

### 16.1. Parser unit tests

Tối thiểu:

```text
giày Nike nam dưới 2 triệu size 42
giay nike nam duoi 2tr size 42
áo chạy bộ nữ màu hồng
giày cỏ thật
giày cỏ nhân tạo
giày futsal
áo MU sân nhà
từ 1 triệu đến 2 triệu
trên 500k
không quá 1tr5
```

Kiểm tra:

- normalized text
- parsed filters
- remaining semantic text
- no false price/size parsing
- explicit UI filters override parser

### 16.2. Repository tests

- Structured filters không trả sai gender/price/size/status.
- Keyword exact match ưu tiên đúng.
- Semantic query chỉ tìm ACTIVE products.
- Không trả out-of-stock nếu `inStockOnly=true`.
- Color family không nhầm edition.
- Pagination deterministic.
- Parameterized SQL chống injection.

### 16.3. Fusion/ranking tests

- Exact SKU/name đứng trên semantic-only match.
- Sản phẩm có mặt ở cả hai branch được boost.
- Keyword-only và vector-only vẫn hoạt động.
- Stable tie-breaker.
- Threshold loại semantic result quá yếu.

### 16.4. Internal API tests

- Missing/wrong token -> 401/403.
- Invalid input -> 422.
- OpenAI error -> keyword-only response.
- DB error -> controlled 503.
- Không leak exception/secrets.

### 16.5. Spring tests

- Internal success -> enrich đúng thứ tự.
- Internal trả INACTIVE ID -> bị loại.
- Internal timeout -> keyword fallback 200.
- Filter commerce được áp lại.
- Rate limit.
- Invalid query.
- Response metadata.

### 16.6. Storefront tests

- Suggestions debounce.
- Keyboard navigation.
- Submit URL.
- Chips parse/render/remove.
- Pagination giữ query/filter.
- Loading/error/empty/fallback.
- Visual search button vẫn hoạt động.
- Mobile layout không tràn.

### 16.7. Integration tests

- Supabase/Testcontainers có pgvector.
- Backfill -> query embedding -> hybrid search.
- Product chuyển ACTIVE -> được index.
- Product chuyển INACTIVE -> biến mất khỏi kết quả.
- Product searchable content đổi -> content hash đổi và reindex.
- OpenAI mocked, không gọi API thật trong test mặc định.

---

## 17. Benchmark và acceptance dataset

Tạo bộ benchmark versioned tối thiểu 60 truy vấn:

- 15 exact product/brand/SKU queries.
- 15 structured attribute queries.
- 15 semantic/need-based queries.
- 10 typo/không dấu/synonym queries.
- 5 zero-result/invalid queries.

Mỗi query có:

```json
{
  "query": "giày tốc độ cho tiền đạo sân cỏ nhân tạo",
  "expectedProductIds": [],
  "expectedCategory": "Giày Đá Bóng Cỏ Nhân Tạo",
  "requiredFilters": {
    "surface": "TF"
  }
}
```

Acceptance targets ban đầu:

- ACTIVE embedding coverage >= 98%.
- Structured filter precision = 100% trên benchmark bắt buộc.
- Exact name/SKU Recall@5 >= 95%.
- Hybrid semantic Recall@5 >= 80%.
- Zero-result queries không trả kết quả giả là exact match.
- p95 autocomplete backend <= 300 ms local, không gọi OpenAI.
- p95 hybrid search <= 1.5 giây khi embedding cache hit.
- p95 hybrid search <= 3 giây khi embedding cache miss.
- Keyword fallback success = 100% khi semantic provider bị mock down.
- Không có regression storefront build, backend tests hoặc chatbot tests.

Không điều chỉnh benchmark ground truth để hợp thức hóa kết quả model.

---

## 18. Thứ tự triển khai

### Phase 0 — Audit và baseline

- [x] Đọc code search hiện tại ở backend, chatbot-service và storefront.
- [x] Chạy tests hiện có.
- [x] Audit lại Supabase counts/coverage ở chế độ read-only.
- [x] Tạo benchmark dataset ban đầu.
- [x] Ghi baseline keyword search trước khi thay đổi.

### Phase 1 — Data normalization

- [x] Migration an toàn cho search metadata/model version/content hash.
- [x] Dry-run report cho `product_type`.
- [x] Backfill `product_type`.
- [x] Thiết kế/backfill `color_family` và `edition`.
- [x] Không phá display color hiện tại.

### Phase 2 — Embedding backfill

- [x] Sửa/tạo idempotent backfill script.
- [x] Mocked unit tests.
- [x] Dry-run Supabase.
- [x] Backfill ACTIVE products.
- [x] Xác nhận coverage >= 98%.

### Phase 3 — Structured parser

- [x] Parser giá, size, gender, surface, type, color.
- [x] Dynamic brand/category dictionaries.
- [x] Vietnamese accents/no-accents.
- [x] Explicit filter override.
- [x] Unit tests.

### Phase 4 — Internal hybrid API

- [x] Request/response schemas.
- [x] Internal token.
- [x] Structured + keyword + vector pipeline.
- [x] RRF and rerank.
- [x] Cache.
- [x] Fail-safe.
- [x] API tests.

### Phase 5 — Spring public API

- [x] Config properties và feature flag.
- [x] Internal REST client.
- [x] Timeout/rate limit.
- [x] Commerce enrichment.
- [x] Keyword fallback.
- [x] Response metadata.
- [x] Spring tests.

### Phase 6 — Autocomplete

- [x] Mở rộng suggestions backend.
- [x] Client search input.
- [x] Debounce.
- [x] Keyboard/accessibility.
- [x] Không gọi OpenAI.

### Phase 7 — Search results UI

- [x] Gọi hybrid endpoint.
- [x] Parsed filter chips.
- [x] Sidebar/filter URL integration.
- [x] Sorting/pagination.
- [x] Loading/error/empty/fallback.
- [x] Responsive QA.

### Phase 8 — Async indexing

- [x] Outbox events.
- [x] RabbitMQ topology.
- [x] Worker.
- [x] Content hash skip.
- [x] Retry/DLQ/reconciliation.

### Phase 9 — Benchmark và rollout

- [x] Chạy benchmark.
- [x] Tune parser/RRF/threshold.
- [x] Verify latency/cache/fallback.
- [x] Feature flag rollout.
- [x] Runbook rollback.

### Implementation notes (cập nhật 2026-08-05)

Các ghi chú “chưa đạt/chưa xác nhận” bên dưới là lịch sử của các lượt chạy trung gian; trạng thái cuối
được chốt trong các bullet “Hoàn tất” và “Hybrid acceptance cuối” ở cuối mục này.

- Hoàn thiện runtime 2026-08-05: đồng bộ `PRODUCT_SEARCH_INTERNAL_TOKEN` giữa hai file `.env`
  local, khai báo đầy đủ URL/timeout/rate limit, bật product-search indexing và bổ sung launcher
  cho chatbot/indexer. RabbitMQ đã import topology mới; `product-search.indexing` có `1` consumer,
  ba retry queue và DLQ đều tồn tại. Một event `PRODUCT_REINDEX_REQUESTED` thật đã được consumer
  xử lý idempotent và ghi vào `product_search_processed_events`.
- Kiểm thử end-to-end local xác nhận internal API và public Spring API đều trả `searchMode=HYBRID`;
  public response được Spring enrich thành công với 5 product cards. Khi cold upstream vượt timeout
  3 giây, public API trả `KEYWORD_FALLBACK` đúng thiết kế; request cache-hit tiếp theo trả `HYBRID`.
- Runtime benchmark 60 query cuối ở
  `evidence/product-hybrid-search/benchmark-final-runtime.json`: structured filter `100%`, exact
  Recall@5 `100%`, semantic Recall@5 `93.33%`, zero-result precision `100%`, báo cáo acceptance
  `passed=true`. Verification cuối: backend `385/385`, chatbot `228/228`, storefront lint,
  typecheck và production build đều pass.
- Storefront đã bổ sung error state riêng thay vì biến lỗi upstream thành zero-result, đồng thời
  hiển thị các structured filter do parser suy ra bên cạnh các filter URL có thể xóa.
- Backfill script và async worker hiện dùng chung duy nhất canonical document/hash implementation.
  Audit trước khi sửa phát hiện 49 embedding có thể bị worker và backfill luân phiên coi là stale
  do khác biệt whitespace; backfill idempotent đã cập nhật 49/49, failed 0. Sau một reindex event
  thật tiếp theo, coverage vẫn giữ `102/102`, `needs_update=0`, chứng minh hash đã ổn định.

- Phase 0 (lần chạy tiếp theo khi Docker đã mở): full Spring suite đã khởi động thành công với
  Testcontainers/pgvector và chạy 373 tests. Suite phát hiện các regression thật thay vì lỗi môi
  trường: search suggestion 1 test, cùng các lỗi ngoài phạm vi hybrid search ở seed, collection,
  notification và payment. Regression search suggestion đã được sửa theo hướng tương thích ngược
  (`id` và `value` cùng tồn tại; sản phẩm ACTIVE chưa có variant vẫn được gợi ý) và toàn bộ
  `ProductSearchIntegrationTest` pass `14/14`. Vì full suite vẫn còn lỗi ngoài search nên
  Ở thời điểm chạy trung gian này, hai checklist backend/Spring chưa được đánh dấu hoàn thành.
- Supabase coverage được kiểm tra read-only lại sau khi Docker mở: `102/102` ACTIVE fresh,
  `missing=0`, `needs_update=0`, coverage `100%`; 103 inactive stale rows không bị xóa.
- Phase 0: audit Supabase read-only xác nhận 102 ACTIVE, 126 INACTIVE, 0/102 ACTIVE embedding,
  103 inactive embedding, không có orphan; storefront lint baseline pass. Backend full suite bị chặn
  bởi môi trường không có Docker/Testcontainers (332 lỗi khởi tạo, 0 assertion failure).
- Phase 1: `V40__product_search_metadata.sql` đã pass transaction rollback test rồi được Flyway
  apply thành công (`version=40`). Dry-run và apply normalization: 50/50 product type được map
  (`30 APPAREL`, `20 FOOTWEAR`); 614 variants được report/backfill `color_family`/`edition`;
  trường display `color` không bị sửa.
- Phase 2: backfill ACTIVE embeddings cập nhật 102, failed 0. Coverage check sau apply:
  `102/102 = 100%`, model `text-embedding-3-small`, dimensions `1536`; 103 inactive stale rows
  được giữ nguyên theo chính sách không tự xóa.
- Phase 3–4: parser và internal API có unit/API tests; toàn bộ chatbot-service:
  `221 passed` (1 deprecation warning từ TestClient).
- Phase 5: backend `mvnw -DskipTests compile` pass; timeout và rate limit theo user/session đã
  triển khai. Đây là trạng thái trung gian trước khi Docker/Testcontainers khả dụng.
- Phase 6–7: storefront `lint`, `typecheck`, production `build` đều pass. Build dùng dist dir tạm
  để không đụng `.next` đang bị dev server khóa; thư mục build tạm đã được dọn sau kiểm tra.
- Phase 8: thêm queue `product-search.indexing` tách biệt visual search, ba tầng retry và DLQ;
  worker validate contract, xử lý idempotent theo event ID, chỉ ghi embedding khi content hash/model
  thay đổi, đánh dấu inactive là `STALE`, và reconciliation định kỳ. Targeted tests `7 passed`,
  full chatbot-service `224 passed`; backend compile pass. Ở lượt trung gian này full Spring suite bị chặn bởi
  Docker/Testcontainers (`373 tests`, `332` lỗi khởi tạo, `0` assertion failure).
- Phase 9: smoke benchmark 6 truy vấn bắt buộc đã chạy trên Supabase/embedding provider hiện tại.
  Kết quả 4/6 truy vấn có kết quả; latency 2.22–4.86 giây, nên **chưa đạt rollout gate**.
  Bằng chứng ở `evidence/product-hybrid-search/smoke-benchmark-2026-08-05.md`; runbook ở
  `docs/product-hybrid-search-rollout.md`. Feature flag tiếp tục giữ `false`.
- Hoàn tất 2026-08-05: full Spring suite pass `385/385`; `ProductSearchIntegrationTest` pass
  `14/14`; chatbot-service pass `228/228`; storefront lint, typecheck và production build pass.
  Flyway trên Supabase xác nhận V40, V41 và V42 đều `success=true`; ACTIVE embedding coverage
  `102/102 = 100%`.
- Benchmark versioned 60 truy vấn ở `chatbot-service/benchmarks/product_hybrid_search_v1.json`.
  Keyword-only baseline: structured filter `100%`, exact recall@5 `100%`, semantic recall@5 `60%`,
  zero-result precision `100%` (`evidence/product-hybrid-search/benchmark-keyword-baseline.json`).
- Hybrid acceptance cuối: structured filter `100%`, exact recall@5 `100%`, semantic recall@5
  `93.33%`, zero-result precision `100%`, cold p95 `2560 ms`, cache-hit p95 `0 ms`; đạt toàn bộ
  acceptance targets (`evidence/product-hybrid-search/benchmark-v4.json`).
- Rollout local đã bật bằng `PRODUCT_HYBRID_SEARCH_ENABLED=true` trong `backend/.env`. Giá trị mặc
  định và `.env.example` vẫn giữ `false` để deployment mới luôn fail-safe và có thể rollback ngay.

---

## 19. Verification commands

Điều chỉnh theo môi trường, nhưng tối thiểu chạy:

```powershell
cd backend
.\mvnw.cmd test
```

```powershell
cd chatbot-service
.\.venv\Scripts\python.exe -m pytest
```

```powershell
cd frontend/storefront
npm.cmd run lint
npm.cmd run typecheck
npm.cmd run build
```

Chạy test liên quan trực tiếp sau mỗi phase trước khi chạy full suite.

Không dùng production OpenAI key trong automated tests. Dùng mock/fake provider.

---

## 20. Rollout và rollback

Rollout:

1. Apply migration tương thích ngược.
2. Deploy code khi feature flag còn `false`.
3. Chạy backfill.
4. Xác minh coverage và benchmark.
5. Bật internal semantic search cho test/admin traffic.
6. Bật một phần storefront traffic nếu có cơ chế rollout.
7. Theo dõi latency, fallback rate, zero-result rate và click-through.
8. Bật toàn bộ khi acceptance đạt.

Rollback:

1. Set `PRODUCT_HYBRID_SEARCH_ENABLED=false`.
2. Storefront quay về endpoint/list search hiện tại.
3. Không xóa embedding/migration trong incident.
4. Không purge queue/DLQ nếu chưa audit.
5. Giữ log/metrics để phân tích.

Commerce và keyword search phải tiếp tục hoạt động khi hybrid search bị tắt.

---

## 21. Definition of Done

Chỉ đánh dấu hoàn thành khi:

- [x] Người dùng tìm được bằng tên, brand, category, SKU và keyword.
- [x] Câu tiếng Việt được tách đúng thành structured filters.
- [x] Semantic search hoạt động trên sản phẩm ACTIVE.
- [x] ACTIVE text embedding coverage >= 98%.
- [x] Structured filters không bị semantic ranking vượt qua.
- [x] Keyword + semantic được fusion và rerank có test.
- [x] Autocomplete không gọi OpenAI.
- [x] Header search hỗ trợ keyboard và mobile.
- [x] Filter chips, sidebar, sort và pagination đồng bộ URL.
- [x] Spring enrich lại status/price/stock/images.
- [x] OpenAI/chatbot lỗi vẫn fallback keyword thành công.
- [x] Không phụ thuộc `ai_forecasting_service`.
- [x] Không phụ thuộc `chatbot-admin-service`.
- [x] Không leak secret/vector/DB credential.
- [x] Backend tests pass.
- [x] Chatbot-service tests pass.
- [x] Storefront lint, typecheck và production build pass.
- [x] Benchmark đạt acceptance targets.
- [x] Có rollout/rollback runbook.

---

## 22. Quy tắc làm việc cho Codex

1. Không sửa dữ liệu Supabase trước khi có dry-run report và migration/script idempotent.
2. Không truncate hoặc xóa embedding hiện có trong Flyway migration.
3. Không hardcode secrets.
4. Không commit `.env` thật.
5. Không thay đổi visual-search vector/schema cho text search.
6. Không làm public browser gọi trực tiếp chatbot-service.
7. Không cho LLM tạo SQL.
8. Không bỏ fallback keyword.
9. Không thay đổi API/DTO hiện tại một cách breaking nếu có thể mở rộng tương thích.
10. Không sửa file unrelated chỉ để làm tests pass.
11. Giữ nguyên thay đổi hiện có của người dùng trong worktree.
12. Sau mỗi phase, cập nhật checklist trong tài liệu này bằng bằng chứng test/benchmark cụ thể.
13. Nếu phát hiện dữ liệu production khác audit trong mục 2.4, dừng write, cập nhật audit và điều chỉnh kế hoạch trước khi tiếp tục.
14. Nếu lựa chọn kiến trúc khác tài liệu này, phải ghi ADR ngắn, nêu bằng chứng và trade-off trước khi triển khai.
