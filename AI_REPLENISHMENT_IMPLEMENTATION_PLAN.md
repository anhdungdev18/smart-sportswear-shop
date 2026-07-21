# Kế hoạch triển khai hệ thống hỗ trợ nhập hàng thông minh

> Tài liệu thực thi cho dự án `smart-sportswear-shop`
>
> Thời gian mục tiêu: 4 tuần
> Phạm vi chính: hỗ trợ admin dự báo nhu cầu theo SKU và đề xuất số lượng nhập hàng
> Dữ liệu đánh giá: dữ liệu mô phỏng có kiểm soát, tách biệt khỏi production

---

## 1. Mục tiêu của đề tài

Xây dựng một hệ thống hỗ trợ admin trả lời được bốn câu hỏi:

1. SKU nào có nguy cơ hết hàng?
2. SKU đó dự kiến hết hàng sau bao nhiêu ngày?
3. Nhu cầu trong kỳ tiếp theo là bao nhiêu?
4. Admin nên nhập thêm bao nhiêu và vì sao?

Tên đề tài đề xuất:

> **Hệ thống hỗ trợ quyết định nhập hàng thông minh dựa trên dự báo nhu cầu cho cửa hàng thể thao**

Mô tả ngắn dùng trong báo cáo:

> Hệ thống tổng hợp lịch sử bán hàng theo từng SKU, thử nghiệm nhiều phương pháp dự báo, tự động chọn phương pháp có sai số backtest thấp nhất và kết hợp kết quả với tồn kho khả dụng, thời gian nhập hàng, mức tồn an toàn và quy cách đóng gói để tạo đề xuất nhập hàng có thể giải thích cho quản trị viên.

---

## 2. Phạm vi khóa cứng

### 2.1. Bắt buộc hoàn thành

- [ ] Sinh dữ liệu giao dịch demo trong 180 ngày bằng seed cố định.
- [x] Tổng hợp nhu cầu theo ngày cho từng biến thể/SKU.
- [x] Cài đặt Moving Average làm baseline.
- [x] Cài đặt EWMA.
- [x] Cài đặt Croston cho nhu cầu gián đoạn.
- [x] Backtest và tự chọn thuật toán tốt nhất theo SKU.
- [x] Tính MAE và WAPE.
- [x] Tính tồn an toàn, điểm đặt hàng lại và số lượng đề xuất nhập.
- [x] Trả kết quả qua API admin.
- [x] Hiển thị danh sách đề xuất tại trang tồn kho admin.
- [x] Cho phép admin chấp nhận, điều chỉnh hoặc bỏ qua đề xuất.
- [x] Lưu lại quyết định của admin.
- [ ] Có unit test và integration test cho luồng chính.
- [ ] Có số liệu so sánh trước/sau để đưa vào báo cáo.

### 2.2. Không làm trong phiên bản một tháng

- Tự động đặt hàng tới nhà cung cấp.
- Tự động tăng tồn kho khi admin chấp nhận đề xuất.
- Dự báo giá bán.
- Cá nhân hóa theo từng khách hàng.
- Gợi ý “thường được mua cùng” trong phạm vi chính.
- Dùng dữ liệu mô phỏng nhưng trình bày như dữ liệu thật.

### 2.3. Cấu trúc Microservice mới (Cập nhật)

> [!NOTE]
> **Thay đổi kiến trúc (Refactoring):** Mặc dù hệ thống ban đầu quy định không làm Microservice riêng, tuy nhiên để đảm bảo tính module hóa và dễ bảo trì, toàn bộ logic dự báo tồn kho (AI Replenishment) đã được nhân bản (Clone Monolith) từ `backend` sang một dự án Spring Boot độc lập tên là `ai_forecasting_service`.
> - **Backend (`:8080`)**: Chuyên xử lý bán hàng và Core E-commerce.
> - **AI Forecasting Service (`:8081`)**: Chuyên xử lý các thuật toán tính toán và dự báo tồn kho (EWMA, Croston, v.v.).

Nếu hoàn thành toàn bộ phần bắt buộc sớm, “thường được mua cùng” chỉ được xem là phần mở rộng.

---

## 3. Hiện trạng có thể tái sử dụng

Các thành phần đã có trong dự án:

- `Order` có `createdAt`, trạng thái đơn hàng và danh sách `OrderItem`.
- `OrderItem` liên kết trực tiếp với `ProductVariant` và lưu số lượng bán.
- `ProductVariant` có `stockQuantity`, `reservedQuantity`, SKU, size và màu.
- `InventoryService` là nơi duy nhất được phép thay đổi tồn kho.
- `InventoryTransaction` lưu lịch sử thay đổi tồn.
- `ReportService` đã có báo cáo bán chạy và tồn thấp.
- Admin đã có trang `/inventory` và form nhập/xuất/điều chỉnh kho.

Quy tắc kiến trúc bắt buộc giữ nguyên:

> Module dự báo chỉ đọc dữ liệu đơn hàng và tồn kho. Mọi thay đổi tồn thực tế vẫn phải đi qua `InventoryService`.

Điểm cần sửa trong code hiện tại:

- `forecast` trên dashboard hiện đang được gán bằng `stockQuantity + reservedQuantity`; đây không phải dự báo.
- Dashboard có thể fallback âm thầm sang dữ liệu mẫu khi API lỗi.
- Biểu đồ doanh thu hiện còn sử dụng mảng dữ liệu tĩnh.

Trong môi trường production, dữ liệu mẫu phải được tắt hoặc hiển thị nhãn rõ ràng là `DEMO DATA`.

---

## 4. Kiến trúc tổng thể

```text
orders + order_items
        |
        v
SalesHistoryRepository
        |
        v
Daily demand by SKU
        |
        +-----------------------------+
        |              |              |
        v              v              v
Moving Average       EWMA          Croston
        |              |              |
        +--------------+--------------+
                       |
                       v
             ForecastBacktestService
                       |
                       v
            Chọn mô hình WAPE thấp nhất
                       |
                       v
                Demand forecast
                       |
        product_variants + inventory_policies
                       |
                       v
             ReplenishmentService
                       |
                       v
      replenishment_recommendations
                       |
                       v
              Admin Inventory UI
```

Module backend mới:

```text
backend/src/main/java/com/dunghaiquyen/ecommerce/modules/replenishment/
├── controller/
│   └── AdminReplenishmentController.java
├── dto/
│   ├── ForecastMetricResponse.java
│   ├── InventoryPolicyRequest.java
│   ├── ReplenishmentActionRequest.java
│   ├── ReplenishmentSuggestionDetailResponse.java
│   └── ReplenishmentSuggestionResponse.java
├── entity/
│   ├── ForecastRun.java
│   ├── InventoryPolicy.java
│   ├── ReplenishmentPriority.java
│   ├── ReplenishmentRecommendation.java
│   └── ReplenishmentStatus.java
├── forecasting/
│   ├── CrostonForecastAlgorithm.java
│   ├── EwmaForecastAlgorithm.java
│   ├── ForecastAlgorithm.java
│   ├── ForecastResult.java
│   └── MovingAverageForecastAlgorithm.java
├── repository/
│   ├── ForecastRunRepository.java
│   ├── InventoryPolicyRepository.java
│   ├── ReplenishmentRecommendationRepository.java
│   └── SalesHistoryRepository.java
└── service/
    ├── DemandForecastService.java
    ├── ForecastBacktestService.java
    └── ReplenishmentService.java
```

Module frontend mới:

```text
frontend/admin/src/modules/replenishment/
├── api.ts
├── browser-api.ts
└── types.ts

frontend/admin/src/components/inventory/
├── AdminInventoryClient.tsx
├── ReplenishmentDetailDialog.tsx
└── ReplenishmentSuggestionTable.tsx
```

---

## 5. Dữ liệu mô phỏng

### 5.0. Cập nhật chính thức sau khi trao đổi với giảng viên

Giảng viên đã cho phép nhóm tự tạo dữ liệu theo hướng phù hợp với chương trình. Vì vậy, bộ dữ liệu mô phỏng có kiểm soát là phương án chính thức của đề tài. Nếu nội dung trong mục 5.0 này khác với hướng dẫn cũ ở các mục phía dưới thì ưu tiên thực hiện theo mục 5.0.

Nội dung có thể ghi trong báo cáo:

> Do thời gian thực hiện đề tài không đủ để thu thập lịch sử giao dịch dài hạn, giảng viên cho phép sử dụng dữ liệu mô phỏng phù hợp với cấu trúc và nghiệp vụ của hệ thống. Nhóm xây dựng bộ sinh dữ liệu có kiểm soát, sử dụng seed cố định và nhiều nhóm nhu cầu khác nhau để bảo đảm thí nghiệm có thể tái tạo. Hệ thống vẫn đọc dữ liệu từ các bảng giao dịch chuẩn, vì vậy có thể chuyển sang dữ liệu thực tế mà không phải thay đổi thuật toán hoặc kiến trúc chính.

#### Phương án phải thực hiện

Sử dụng chiến lược **hybrid seeder**:

1. Tái sử dụng core seed hiện tại để tạo user, category, brand và product hợp lệ.
2. Tạo khoảng 30 variant riêng cho dự báo, dùng prefix SKU `FD-`.
3. Tạo một runner dự báo chạy sau `SeedDataRunner`.
4. Sinh khoảng 3.000 đơn trong 180 ngày bằng random seed cố định `2026`.
5. Sinh order và order item trong bộ nhớ.
6. Dùng `JdbcTemplate.batchUpdate` để ghi dữ liệu lịch sử vào đúng bảng `orders` và `order_items`.
7. Gắn marker `[FORECAST_DEMO]` vào `orders.note`.
8. Khi chạy lại, chỉ cleanup đơn và order item có marker này.
9. Không xóa hay sửa đơn do người dùng tự tạo.

Không nên tạo 3.000 đơn lịch sử qua `OrderService.createOrderFromCart`, vì trường `createdAt` hiện do JPA Auditing gán thời điểm chạy chương trình. Luồng checkout còn tạo cart, reserve, payment, transition và notification không cần thiết, khiến seed chậm và khó tạo chính xác lịch sử lùi 180 ngày.

#### Cấu hình riêng

Tạo:

```text
backend/src/main/java/com/dunghaiquyen/ecommerce/config/AppForecastDemoProperties.java
backend/src/main/java/com/dunghaiquyen/ecommerce/infra/seed/ForecastDemoDataSeeder.java
backend/src/main/java/com/dunghaiquyen/ecommerce/infra/seed/ForecastDemoDataRunner.java
```

Cấu hình:

```yaml
app:
  forecast-demo:
    enabled: false
    random-seed: 2026
    history-days: 180
    order-count: 3000
    variant-count: 30
```

`ForecastDemoDataRunner` chỉ hoạt động khi `app.forecast-demo.enabled=true`. Runner này phải chạy sau core seed. Có thể đặt thứ tự:

```text
SeedDataRunner: order 10
ForecastDemoDataRunner: order 20
```

#### Phân phối nhu cầu

| Nhóm SKU | Tỷ lệ | Đặc điểm |
|---|---:|---|
| Fast moving | 20% | Bán gần như mỗi ngày |
| Normal | 40% | Bán đều ở mức trung bình |
| Slow moving | 25% | Ít phát sinh nhu cầu |
| Intermittent | 15% | Nhiều ngày bằng 0, thỉnh thoảng tăng |

Bổ sung hiệu ứng cuối tuần tăng từ 10% đến 30%, một số SKU có xu hướng tăng hoặc giảm và hai đến ba đợt nhu cầu tăng do khuyến mãi.

Phân phối trạng thái đơn đề xuất:

| Trạng thái | Tỷ lệ |
|---|---:|
| DELIVERED | 70% |
| SHIPPING | 10% |
| PACKING | 7% |
| CONFIRMED | 5% |
| PENDING_CONFIRMATION | 3% |
| CANCELLED | 5% |

Đơn pending và cancelled được tạo có chủ đích để kiểm thử việc loại dữ liệu không hợp lệ khỏi forecast.

#### Quy tắc dữ liệu bắt buộc

Mỗi order phải có:

- UUID và order code duy nhất.
- Customer demo hợp lệ.
- Address snapshot JSON hợp lệ.
- Subtotal, discount, shipping fee và total nhất quán.
- Trạng thái đơn và thanh toán hợp lệ.
- Created time nằm đúng trong khoảng 180 ngày.
- Marker `[FORECAST_DEMO]`.

Mỗi order item phải:

- Tham chiếu đúng order, product và variant.
- Copy đúng snapshot tên, SKU, size, màu và giá.
- Có quantity lớn hơn 0.
- Có line total bằng unit price nhân quantity.

#### Ảnh chụp tồn kho hiện tại

Dữ liệu mô phỏng chỉ cần lịch sử nhu cầu và ảnh chụp tồn kho hiện tại, không cần tái dựng toàn bộ ledger nhập xuất trong 180 ngày:

- SKU bán nhanh có tồn thấp để sinh đề xuất `CRITICAL`.
- SKU bán đều có tồn gần reorder point để sinh `HIGH` hoặc `MEDIUM`.
- Một số SKU có tồn đủ để chứng minh hệ thống không đề xuất nhập.
- Reserved quantity luôn không âm và không lớn hơn stock quantity.

Nội dung cần ghi trong báo cáo:

> Bộ dữ liệu mô phỏng gồm lịch sử nhu cầu và ảnh chụp tồn kho tại thời điểm đánh giá; đề tài không tái dựng toàn bộ chuỗi nhập/xuất kho lịch sử.

#### Idempotency và an toàn

- Seeder chạy hai lần không được làm tăng gấp đôi số đơn.
- Cleanup chỉ tác động dữ liệu có marker `[FORECAST_DEMO]`.
- Không dùng `deleteAll` cho toàn bộ order, product hoặc variant.
- Seeder bị tắt mặc định.
- Cùng random seed phải tạo cùng tập nhu cầu.
- Không chạy forecast demo seeder trong production.

#### Tiêu chí hoàn thành riêng cho bộ dữ liệu

- [ ] Có 30 SKU prefix `FD-`.
- [ ] Có khoảng 3.000 đơn trải đều trong 180 ngày.
- [ ] Có đủ bốn nhóm nhu cầu.
- [ ] Có đơn cancelled và pending để kiểm thử bộ lọc.
- [ ] Không vi phạm khóa ngoại hoặc check constraint.
- [ ] Chạy lại seeder không nhân đôi dữ liệu.
- [ ] Cleanup không xóa dữ liệu người dùng.
- [ ] Có SKU cần nhập khẩn cấp, cần nhập vừa và không cần nhập.
- [ ] Có thể truy vấn lại tổng bán từ `order_items`.
- [ ] Báo cáo ghi rõ đây là dữ liệu mô phỏng được giảng viên cho phép.

### 5.1. Nguyên tắc

Dữ liệu demo phải:

- Có thể tái tạo bằng cùng một random seed.
- Không chạy mặc định trong production.
- Tạo thông qua hybrid demo seeder riêng, không chèn vào migration Flyway.
- Tuân thủ quy tắc tồn kho và trạng thái đơn hàng của hệ thống.
- Có phân phối khác nhau giữa các nhóm SKU.
- Được ghi rõ là dữ liệu mô phỏng trong báo cáo.

### 5.2. Cấu hình đề xuất

```yaml
app:
  forecast-demo:
    enabled: false
    random-seed: 2026
    history-days: 180
    order-count: 3000
    variant-count: 30
```

Chỉ bật trong profile demo:

```yaml
app:
  forecast-demo:
    enabled: true
```

### 5.3. Nhóm nhu cầu cần mô phỏng

Chia SKU thành các nhóm:

| Nhóm | Tỷ lệ gợi ý | Đặc điểm |
|---|---:|---|
| Fast moving | 20% | Bán gần như mỗi ngày, nhu cầu cao |
| Normal | 40% | Bán đều, nhu cầu trung bình |
| Slow moving | 25% | Bán ít |
| Intermittent | 15% | Nhiều ngày bằng 0, thỉnh thoảng bán tăng |

Các hiệu ứng nên có:

- Cuối tuần tăng khoảng 10–30%.
- Một số SKU có xu hướng tăng dần.
- Một số SKU giảm dần.
- Có hai hoặc ba đợt khuyến mãi làm nhu cầu tăng.
- Tỷ lệ đơn hủy khoảng 5–10%.
- Có một lượng nhỏ đơn trả hàng.

Không đưa đơn `CANCELLED` vào nhu cầu bán thực tế.

### 5.4. Lớp seeder đề xuất

```text
backend/src/main/java/com/dunghaiquyen/ecommerce/infra/seed/ForecastDemoDataSeeder.java
backend/src/main/java/com/dunghaiquyen/ecommerce/infra/seed/ForecastDemoDataRunner.java
```

Seeder cần có điều kiện:

```java
@ConditionalOnProperty(
    prefix = "app.forecast-demo",
    name = "enabled",
    havingValue = "true"
)
```

### 5.5. Tiêu chí nghiệm thu dữ liệu

- [ ] Cùng seed `2026` sinh cùng kết quả.
- [ ] Có đủ ít nhất 180 ngày dữ liệu.
- [ ] Có ít nhất 2.000 đơn không bị hủy.
- [ ] Có SKU bán nhanh, chậm và gián đoạn.
- [ ] Không có tồn kho âm.
- [ ] `reservedQuantity <= stockQuantity`.
- [ ] Tổng số bán có thể kiểm tra lại từ `order_items`.
- [ ] Seeder không chạy khi cấu hình bị tắt.

---

## 6. Migration cơ sở dữ liệu

Tạo file:

```text
backend/src/main/resources/db/migration/V13__replenishment_forecasting.sql
```

### 6.1. Bảng `inventory_policies`

```sql
create table inventory_policies (
    id uuid primary key,
    variant_id uuid not null references product_variants (id) on delete cascade,
    lead_time_days integer not null default 7,
    target_cover_days integer not null default 30,
    service_level numeric(4, 3) not null default 0.950,
    minimum_order_quantity integer not null default 1,
    pack_size integer not null default 1,
    supplier_name varchar(255),
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_inventory_policies_variant unique (variant_id),
    constraint chk_inventory_policies_lead_time check (lead_time_days >= 0),
    constraint chk_inventory_policies_target_cover check (target_cover_days > 0),
    constraint chk_inventory_policies_service_level check (service_level > 0 and service_level < 1),
    constraint chk_inventory_policies_minimum_order check (minimum_order_quantity > 0),
    constraint chk_inventory_policies_pack_size check (pack_size > 0)
);

create index idx_inventory_policies_active on inventory_policies (active);
```

### 6.2. Bảng `forecast_runs`

```sql
create table forecast_runs (
    id uuid primary key,
    variant_id uuid not null references product_variants (id) on delete cascade,
    algorithm varchar(30) not null,
    training_from date not null,
    training_to date not null,
    forecast_horizon_days integer not null,
    average_daily_demand numeric(12, 4) not null,
    forecast_quantity numeric(12, 4) not null,
    mae numeric(12, 4),
    wape numeric(12, 6),
    residual_std_dev numeric(12, 4),
    confidence varchar(20) not null,
    generated_at timestamptz not null,
    constraint chk_forecast_runs_algorithm check (algorithm in ('MOVING_AVERAGE', 'EWMA', 'CROSTON')),
    constraint chk_forecast_runs_horizon check (forecast_horizon_days > 0),
    constraint chk_forecast_runs_confidence check (confidence in ('LOW', 'MEDIUM', 'HIGH'))
);

create index idx_forecast_runs_variant_generated
    on forecast_runs (variant_id, generated_at desc);
```

### 6.3. Bảng `replenishment_recommendations`

```sql
create table replenishment_recommendations (
    id uuid primary key,
    variant_id uuid not null references product_variants (id) on delete cascade,
    forecast_run_id uuid references forecast_runs (id) on delete set null,
    available_quantity integer not null,
    incoming_quantity integer not null default 0,
    reorder_point integer not null,
    safety_stock integer not null,
    suggested_quantity integer not null,
    admin_quantity integer,
    estimated_stockout_days integer,
    priority varchar(20) not null,
    status varchar(20) not null default 'PENDING',
    explanation_json jsonb not null,
    admin_note text,
    acted_by uuid references users (id),
    acted_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_replenishment_priority check (priority in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    constraint chk_replenishment_status check (status in ('PENDING', 'ACCEPTED', 'ADJUSTED', 'DISMISSED', 'RECEIVED')),
    constraint chk_replenishment_available check (available_quantity >= 0),
    constraint chk_replenishment_incoming check (incoming_quantity >= 0),
    constraint chk_replenishment_reorder_point check (reorder_point >= 0),
    constraint chk_replenishment_safety_stock check (safety_stock >= 0),
    constraint chk_replenishment_suggested check (suggested_quantity >= 0),
    constraint chk_replenishment_admin_quantity check (admin_quantity is null or admin_quantity >= 0)
);

create index idx_replenishment_status_priority
    on replenishment_recommendations (status, priority, created_at desc);

create index idx_replenishment_variant_created
    on replenishment_recommendations (variant_id, created_at desc);
```

### 6.4. Quy tắc migration

- [ ] Không sửa migration cũ đã chạy.
- [ ] ID được tạo theo chiến lược UUID hiện tại của Hibernate.
- [ ] Entity kế thừa đúng abstract entity tương ứng.
- [ ] Enum Java khớp tuyệt đối với check constraint.
- [ ] Chạy migration trên database trống và database hiện có.

---

## 7. Truy vấn lịch sử nhu cầu

Nhu cầu theo ngày được lấy từ:

```text
order_items.quantity
join orders.created_at
group by variant_id và ngày
```

Chỉ tính các đơn:

```text
CONFIRMED
PACKING
SHIPPING
DELIVERED
```

Không tính:

```text
PENDING_CONFIRMATION
CANCELLED
```

Lý do:

- `PENDING_CONFIRMATION` chưa chắc trở thành nhu cầu thực tế.
- `CANCELLED` không phải hàng đã bán.

Projection đề xuất:

```java
public interface DailyVariantDemandProjection {
    UUID getVariantId();
    LocalDate getDemandDate();
    long getQuantity();
}
```

Repository cần trả dữ liệu trong một khoảng ngày. Service phải lấp các ngày không bán bằng `0`; nếu bỏ qua ngày bằng 0, trung bình nhu cầu sẽ bị thổi phồng.

Pseudo SQL:

```sql
select
    oi.variant_id,
    (o.created_at at time zone 'Asia/Ho_Chi_Minh')::date as demand_date,
    sum(oi.quantity) as quantity
from order_items oi
join orders o on o.id = oi.order_id
where o.order_status in ('CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED')
  and o.created_at >= :from
  and o.created_at < :toExclusive
group by oi.variant_id, demand_date
order by oi.variant_id, demand_date;
```

Lưu ý múi giờ phải thống nhất với `AppTimeZone` hiện tại.

### Tiêu chí nghiệm thu truy vấn

- [ ] Ngày không bán được điền bằng 0.
- [ ] Đơn hủy không được tính.
- [ ] Đơn chờ xác nhận không được tính.
- [ ] Kết quả theo đúng SKU/variant, không chỉ theo product.
- [ ] Không xảy ra N+1 query khi tính cho nhiều SKU.
- [ ] Truy vấn 180 ngày cho toàn bộ SKU chạy trong thời gian chấp nhận được.

---

## 8. Các thuật toán dự báo

### 8.1. Interface chung

```java
public interface ForecastAlgorithm {
    ForecastAlgorithmType type();

    ForecastResult forecast(
            List<Integer> dailyDemand,
            int horizonDays);
}
```

`ForecastResult` tối thiểu gồm:

```java
public record ForecastResult(
        ForecastAlgorithmType algorithm,
        double averageDailyDemand,
        double forecastQuantity,
        List<Double> dailyForecast) {
}
```

Mọi thuật toán phải:

- Không trả NaN hoặc infinity.
- Không trả nhu cầu âm.
- Xử lý được chuỗi toàn số 0.
- Xử lý được lịch sử ngắn.

### 8.2. Moving Average

Baseline 30 ngày:

```text
dailyForecast = tổng nhu cầu min(30, số ngày hiện có) / số ngày được dùng
forecastQuantity = dailyForecast × horizonDays
```

Nếu không có ngày lịch sử:

```text
dailyForecast = 0
confidence = LOW
```

### 8.3. EWMA

Công thức:

```text
S(t) = alpha × X(t) + (1 - alpha) × S(t - 1)
```

Giá trị khởi đầu đề xuất:

```text
alpha = 0.30
```

Có thể thử tập alpha nhỏ:

```text
0.10, 0.20, 0.30, 0.40, 0.50
```

Chọn alpha có WAPE backtest thấp nhất. Không cần tối ưu liên tục trong MVP.

### 8.4. Croston

Croston dùng cho SKU có nhu cầu gián đoạn:

- Ước lượng kích thước nhu cầu khi phát sinh.
- Ước lượng khoảng cách giữa hai lần phát sinh nhu cầu.
- Dự báo nhu cầu/ngày bằng tỷ lệ hai ước lượng trên.

Tham số ban đầu:

```text
alpha = 0.10
```

Nếu chuỗi toàn số 0, kết quả dự báo bằng 0.

### 8.5. Không gọi LLM để tính dự báo

Không gửi lịch sử bán hàng cho OpenAI/Gemini để hỏi số lượng cần nhập. Lý do:

- Kết quả không ổn định.
- Khó tái hiện.
- Khó đánh giá bằng metric.
- Có thể sinh số không đúng dữ liệu.
- Tốn chi phí và phụ thuộc mạng.

Phần giải thích nên tạo bằng template từ các con số đã tính.

---

## 9. Backtesting và chọn mô hình

### 9.1. Chia dữ liệu

Với 180 ngày:

```text
150 ngày đầu: train
30 ngày cuối: test/backtest
```

Không được dùng dữ liệu 30 ngày test để tính tham số trước khi đánh giá.

### 9.2. Walk-forward backtest

Phương án nên dùng:

1. Lấy dữ liệu trước ngày test hiện tại.
2. Dự báo ngày tiếp theo.
3. So sánh dự báo với thực tế.
4. Mở rộng cửa sổ thêm một ngày.
5. Lặp hết 30 ngày.

Cách này giống tình huống triển khai thực tế hơn việc dự báo toàn bộ 30 ngày một lần.

### 9.3. Metric

MAE:

```text
MAE = sum(abs(actual - forecast)) / n
```

WAPE:

```text
WAPE = sum(abs(actual - forecast)) / sum(actual)
```

Quy tắc khi `sum(actual) = 0`:

- Không chia cho 0.
- Đặt WAPE là `null` hoặc dùng MAE để xếp hạng.
- Gắn confidence `LOW` nếu không đủ nhu cầu để đánh giá.

Không dùng MAPE làm metric chính vì dữ liệu bán theo ngày có nhiều giá trị 0.

### 9.4. Chọn thuật toán

Thứ tự:

1. Nếu tổng actual trong test lớn hơn 0: chọn WAPE thấp nhất.
2. Nếu tổng actual bằng 0: chọn MAE thấp nhất.
3. Nếu bằng nhau: ưu tiên thuật toán đơn giản hơn.

Thứ tự ưu tiên khi bằng nhau:

```text
MOVING_AVERAGE > EWMA > CROSTON
```

Mục đích là tránh chọn mô hình phức tạp khi không cải thiện kết quả.

### 9.5. Confidence

Quy tắc MVP:

```text
HIGH:
- Có ít nhất 90 ngày lịch sử
- Có nhu cầu ở ít nhất 20 ngày
- WAPE <= 25%

MEDIUM:
- Có ít nhất 60 ngày lịch sử
- Có nhu cầu ở ít nhất 10 ngày
- WAPE <= 50%

LOW:
- Các trường hợp còn lại
- Hoặc không có đủ dữ liệu backtest
```

Confidence không được giả làm xác suất thống kê. Nó chỉ là mức tin cậy nghiệp vụ theo rule đã công bố.

---

## 10. Công thức đề xuất nhập hàng

### 10.1. Tồn khả dụng

```text
availableQuantity = stockQuantity - reservedQuantity
```

Không dùng `stockQuantity` đơn lẻ vì hàng đang giữ cho đơn khác không còn thật sự khả dụng.

### 10.2. Tồn an toàn

Mục tiêu service level mặc định 95%:

```text
z = 1.65
safetyStock = ceil(z × residualStdDev × sqrt(leadTimeDays))
```

Mapping service level MVP:

| Service level | Z |
|---:|---:|
| 90% | 1.28 |
| 95% | 1.65 |
| 97.5% | 1.96 |
| 99% | 2.33 |

Không cần thêm thư viện thống kê chỉ để tính Z trong MVP; dùng bảng mapping cố định.

Nếu chưa đủ dữ liệu tính sai số:

```text
safetyStock = ceil(averageDailyDemand × 3)
```

Ba ngày là fallback mặc định và phải được ghi trong explanation.

### 10.3. Điểm đặt hàng lại

```text
reorderPoint = ceil(
    averageDailyDemand × leadTimeDays
    + safetyStock
)
```

### 10.4. Tồn mục tiêu

```text
targetStock = ceil(
    averageDailyDemand × (leadTimeDays + targetCoverDays)
    + safetyStock
)
```

### 10.5. Số lượng đề xuất

```text
rawSuggestion = max(
    0,
    targetStock - availableQuantity - incomingQuantity
)
```

Phiên bản hiện tại chưa có purchase order, vì vậy:

```text
incomingQuantity = 0
```

Giới hạn này phải được ghi trong báo cáo.

Áp dụng số lượng nhập tối thiểu:

```text
if rawSuggestion > 0:
    rawSuggestion = max(rawSuggestion, minimumOrderQuantity)
```

Làm tròn theo pack size:

```text
suggestedQuantity = ceil(rawSuggestion / packSize) × packSize
```

### 10.6. Ngày dự kiến hết hàng

```text
if averageDailyDemand <= 0:
    estimatedStockoutDays = null
else:
    estimatedStockoutDays = floor(
        availableQuantity / averageDailyDemand
    )
```

### 10.7. Mức ưu tiên

```text
CRITICAL:
- availableQuantity = 0
- Hoặc estimatedStockoutDays <= leadTimeDays

HIGH:
- estimatedStockoutDays <= leadTimeDays + 7

MEDIUM:
- availableQuantity <= reorderPoint

LOW:
- Các trường hợp còn lại có suggestedQuantity > 0
```

Không tạo recommendation nếu `suggestedQuantity = 0`, trừ khi cần lưu forecast run phục vụ đánh giá.

### 10.8. Ví dụ hoàn chỉnh

```text
averageDailyDemand = 1.3
leadTimeDays = 7
targetCoverDays = 30
residualStdDev = 1.1
serviceLevel = 95%, z = 1.65
availableQuantity = 8
incomingQuantity = 0
packSize = 5

safetyStock = ceil(1.65 × 1.1 × sqrt(7)) = 5
reorderPoint = ceil(1.3 × 7 + 5) = 15
targetStock = ceil(1.3 × 37 + 5) = 54
rawSuggestion = 54 - 8 = 46
suggestedQuantity = ceil(46 / 5) × 5 = 50
estimatedStockoutDays = floor(8 / 1.3) = 6
priority = CRITICAL
```

---

## 11. Giải thích đề xuất

Không cần LLM trong MVP. Dùng dữ liệu có cấu trúc và template.

`explanation_json` đề xuất:

```json
{
  "summary": "SKU dự kiến hết hàng trước khi lô hàng mới có thể về.",
  "reasons": [
    "Tồn khả dụng hiện tại là 8 sản phẩm.",
    "Nhu cầu trung bình dự báo là 1,3 sản phẩm/ngày.",
    "Tồn kho dự kiến chỉ đủ khoảng 6 ngày.",
    "Thời gian nhập hàng được cấu hình là 7 ngày.",
    "Đề xuất đã được làm tròn theo quy cách đóng gói 5 sản phẩm."
  ],
  "formula": {
    "safetyStock": 5,
    "reorderPoint": 15,
    "targetStock": 54,
    "rawSuggestion": 46,
    "roundedSuggestion": 50
  }
}
```

Mọi số trong giải thích phải khớp với response và bản ghi database.

---

## 12. API admin

Base path:

```text
/api/v1/admin/replenishment
```

Phân quyền đề xuất:

- `ADMIN`: xem policy, sửa policy, sinh đề xuất, duyệt và bỏ qua.
- `WAREHOUSE_STAFF`: xem đề xuất và thực hiện nhập kho khi hàng đã về.

### 12.1. Danh sách đề xuất

```http
GET /api/v1/admin/replenishment/suggestions
```

Query:

```text
status=PENDING
priority=CRITICAL
keyword=nike
page=1
limit=20
```

Response item:

```json
{
  "id": "uuid",
  "variantId": "uuid",
  "productId": "uuid",
  "sku": "NIKE-V16-42-BLACK",
  "productName": "Nike Vapor 16",
  "size": "42",
  "color": "Black",
  "availableQuantity": 8,
  "averageDailyDemand": 1.3,
  "forecastHorizonDays": 37,
  "forecastQuantity": 48.1,
  "estimatedStockoutDays": 6,
  "reorderPoint": 15,
  "safetyStock": 5,
  "suggestedQuantity": 50,
  "priority": "CRITICAL",
  "algorithm": "EWMA",
  "confidence": "HIGH",
  "mae": 0.42,
  "wape": 0.18,
  "status": "PENDING",
  "createdAt": "2026-07-13T08:00:00Z"
}
```

### 12.2. Chi tiết đề xuất

```http
GET /api/v1/admin/replenishment/suggestions/{id}
```

Bao gồm:

- Response item đầy đủ.
- Daily actual history.
- Daily backtest forecast.
- Daily future forecast.
- Explanation.
- Policy đang áp dụng.

### 12.3. Sinh lại đề xuất

```http
POST /api/v1/admin/replenishment/generate
```

Body tùy chọn:

```json
{
  "variantIds": ["uuid-1", "uuid-2"]
}
```

Nếu không truyền `variantIds`, chạy cho toàn bộ variant active.

Trong một tháng chưa cần scheduler tự động. Admin bấm “Tạo lại đề xuất” là đủ. Scheduler chỉ thêm sau khi luồng thủ công ổn định.

### 12.4. Cập nhật policy

```http
PUT /api/v1/admin/replenishment/policies/{variantId}
```

```json
{
  "leadTimeDays": 7,
  "targetCoverDays": 30,
  "serviceLevel": 0.95,
  "minimumOrderQuantity": 5,
  "packSize": 5,
  "supplierName": "Nhà cung cấp A",
  "active": true
}
```

### 12.5. Chấp nhận

```http
POST /api/v1/admin/replenishment/suggestions/{id}/accept
```

```json
{
  "note": "Đã xác nhận với nhà cung cấp"
}
```

Kết quả:

```text
status = ACCEPTED
adminQuantity = suggestedQuantity
```

Không gọi `InventoryService.importStock()` tại bước này.

### 12.6. Điều chỉnh

```http
POST /api/v1/admin/replenishment/suggestions/{id}/adjust
```

```json
{
  "quantity": 40,
  "note": "Ngân sách chỉ cho phép nhập 40"
}
```

Kết quả:

```text
status = ADJUSTED
adminQuantity = 40
```

### 12.7. Bỏ qua

```http
POST /api/v1/admin/replenishment/suggestions/{id}/dismiss
```

```json
{
  "note": "Sản phẩm chuẩn bị ngừng kinh doanh"
}
```

`note` bắt buộc khi bỏ qua để phục vụ đánh giá sau này.

---

## 13. Quy tắc nghiệp vụ quan trọng

### 13.1. Không tự động cập nhật kho

```text
ACCEPTED/ADJUSTED
    !=
Hàng đã về kho
```

Chỉ khi hàng thực sự được nhận, nhân viên mới dùng luồng nhập kho hiện có:

```text
InventoryService.importStock()
```

Sau khi nhập thành công, recommendation có thể chuyển sang `RECEIVED`.

### 13.2. Không ghi đè lịch sử

Mỗi lần generate:

- Lưu một `ForecastRun` mới.
- Có thể đóng recommendation `PENDING` cũ của cùng variant theo rule rõ ràng.
- Không sửa metric của forecast run cũ.

Phương án MVP:

1. Nếu recommendation cũ vẫn `PENDING`, cập nhật nó trỏ tới forecast run mới và lưu số liệu mới.
2. Nếu recommendation đã được admin xử lý, tạo recommendation mới.

### 13.3. SKU không có lịch sử

Nếu variant chưa từng bán:

- Forecast bằng 0.
- Confidence `LOW`.
- Không đề xuất dựa trên “AI”.
- Có thể cảnh báo “Chưa đủ dữ liệu”.
- Admin vẫn quản lý bằng policy/minimum stock hiện có nếu muốn.

Không tự suy diễn nhu cầu từ sản phẩm khác trong MVP.

### 13.4. SKU inactive

Không tạo đề xuất cho:

- Product `INACTIVE` hoặc `DRAFT`.
- Variant `INACTIVE`.

Variant `OUT_OF_STOCK` vẫn được dự báo nếu product còn kinh doanh.

---

## 14. Frontend admin

### 14.1. Vị trí

Tích hợp vào trang:

```text
frontend/admin/src/app/inventory/page.tsx
```

Thứ tự màn hình:

1. KPI dự báo.
2. Bảng đề xuất nhập hàng.
3. Thao tác kho hiện tại.
4. Bảng tồn kho hiện tại.
5. Lịch sử giao dịch kho.

### 14.2. KPI

Hiển thị:

- Số SKU `CRITICAL`.
- Số SKU `HIGH`.
- Tổng số lượng được đề xuất nhập.
- WAPE trung bình của các SKU đủ dữ liệu.

KPI phải lấy từ API thật. Khi API lỗi, hiển thị lỗi hoặc trạng thái không có dữ liệu; không fallback âm thầm sang mock.

### 14.3. Bảng đề xuất

| Cột | Nội dung |
|---|---|
| Ưu tiên | Critical/High/Medium/Low |
| SKU | SKU biến thể |
| Sản phẩm | Tên, size, màu |
| Khả dụng | Stock trừ reserved |
| Nhu cầu/ngày | Dự báo trung bình |
| Hết sau | Số ngày dự kiến |
| Đề xuất | Số lượng nhập |
| Mô hình | MA/EWMA/Croston |
| Tin cậy | High/Medium/Low |
| Hành động | Xem, duyệt, sửa, bỏ qua |

Bộ lọc:

- Keyword/SKU.
- Priority.
- Status.
- Confidence.

### 14.4. Dialog chi tiết

Hiển thị:

- Biểu đồ actual và backtest forecast.
- Dự báo tương lai.
- Các metric.
- Policy áp dụng.
- Công thức tính đề xuất.
- Danh sách lý do.
- Các nút hành động.

### 14.5. Luồng “Điền vào form nhập kho”

Chỉ áp dụng khi hàng thực tế đã về:

1. Admin/warehouse mở recommendation đã duyệt.
2. Bấm “Điền vào form nhập kho”.
3. Form kho nhận `variantId` và `adminQuantity`.
4. Người dùng xác nhận lại.
5. Frontend gọi API import kho hiện có.
6. Sau khi import thành công, đánh dấu recommendation `RECEIVED`.

Không gọi hai API theo cách có thể khiến kho đã tăng nhưng recommendation chưa cập nhật mà không báo lỗi. Nếu chưa xây được transaction liên module, ưu tiên:

1. Import kho thành công là nguồn sự thật.
2. Nếu cập nhật recommendation lỗi, hiển thị cảnh báo và cho phép đồng bộ lại.

---

## 15. Bảo mật và audit

- [ ] Tất cả API sử dụng `@PreAuthorize`.
- [ ] Không trả dữ liệu dự báo admin qua API public.
- [ ] `actedBy` lấy từ principal, không nhận từ request body.
- [ ] Không cho sửa recommendation đã `RECEIVED`.
- [ ] Validate quantity không âm.
- [ ] Validate service level.
- [ ] Validate transition trạng thái.
- [ ] Ghi audit log cho accept, adjust, dismiss và received nếu module audit hỗ trợ.

Transition hợp lệ:

```text
PENDING -> ACCEPTED
PENDING -> ADJUSTED
PENDING -> DISMISSED
ACCEPTED -> RECEIVED
ADJUSTED -> RECEIVED
```

Các transition khác trả `422 Unprocessable Entity`.

---

## 16. Cache và hiệu năng

Không tối ưu sớm, nhưng phải tránh truy vấn từng SKU một.

Yêu cầu:

- Lấy lịch sử bán của tất cả SKU bằng truy vấn tổng hợp theo khoảng ngày.
- Load policy theo batch.
- Load variant/product theo batch hoặc fetch join phù hợp.
- Generate toàn bộ recommendation trong một service operation có kiểm soát.
- Không giữ transaction database mở trong lúc chạy tính toán dài nếu không cần.

MVP chưa cần Redis cho forecast. Có thể cache danh sách suggestion 30 giây ở frontend giống report hiện tại.

Nếu generate chậm:

1. Đo thời gian trước.
2. Thêm index phù hợp cho `orders.created_at`, `orders.order_status` và join hiện có.
3. Chưa cần queue/background job trừ khi request thực sự timeout.

---

## 17. Kiểm thử

### 17.1. Unit test thuật toán

`MovingAverageForecastAlgorithmTest`

- [ ] Chuỗi đều `[1,1,1,...]` dự báo đúng 1/ngày.
- [ ] Chuỗi toàn 0 trả 0.
- [ ] Chuỗi ngắn hơn window vẫn chạy.
- [ ] Không trả số âm.

`EwmaForecastAlgorithmTest`

- [ ] Nhu cầu tăng gần đây làm forecast tăng.
- [ ] Alpha hợp lệ.
- [ ] Chuỗi toàn 0 trả 0.
- [ ] Không trả NaN.

`CrostonForecastAlgorithmTest`

- [ ] Chuỗi nhu cầu gián đoạn trả forecast dương hợp lý.
- [ ] Chuỗi toàn 0 trả 0.
- [ ] Một lần phát sinh nhu cầu không làm lỗi.

### 17.2. Unit test backtest

- [x] Không rò rỉ dữ liệu test vào train.
- [x] MAE đúng theo dữ liệu mẫu tính tay.
- [x] WAPE đúng theo dữ liệu mẫu tính tay.
- [x] Tổng actual bằng 0 không chia cho 0.
- [x] Chọn model có metric tốt nhất.
- [x] Tie-break chọn model đơn giản hơn.

### 17.3. Unit test đề xuất nhập

- [x] `available = stock - reserved`.
- [x] Không đề xuất số âm.
- [x] Áp dụng minimum order quantity.
- [x] Làm tròn đúng pack size.
- [x] Tính đúng safety stock.
- [x] Xác định đúng priority.
- [x] Nhu cầu 0 không gây chia cho 0.
- [x] Explanation khớp các số đã tính.

### 17.4. Integration test API

`ReplenishmentIntegrationTest`

- [x] Admin xem được suggestion.
- [ ] Warehouse staff chỉ được dùng API đã cấp quyền.
- [x] Customer nhận 403.
- [ ] Generate bỏ qua đơn cancelled.
- [ ] Generate bỏ qua variant inactive.
- [x] Accept lưu principal hiện tại.
- [x] Adjust yêu cầu quantity hợp lệ.
- [x] Dismiss yêu cầu note.
- [x] Transition sai trả 422.
- [x] Accept không làm tăng stock.
- [x] Import kho vẫn tạo `InventoryTransaction`.

### 17.5. Kiểm thử frontend

- [x] Loading state.
- [x] Empty state.
- [x] API error state không hiện mock data.
- [x] Filter hoạt động.
- [x] Dialog hiển thị đúng dữ liệu.
- [x] Disable nút khi request đang chạy.
- [x] Không double submit.
- [x] Sau hành động, status cập nhật đúng.

---

## 18. Đánh giá thực nghiệm

### 18.1. Thí nghiệm dự báo

Với mỗi SKU đủ dữ liệu, lưu:

| SKU | Model | MAE | WAPE | Được chọn |
|---|---|---:|---:|---|
| A | Moving Average | 0.72 | 31% | Không |
| A | EWMA | 0.42 | 18% | Có |
| A | Croston | 0.65 | 27% | Không |

Tổng hợp:

- WAPE trung bình từng thuật toán.
- Số SKU chọn mỗi thuật toán.
- Kết quả theo nhóm fast/normal/slow/intermittent.

### 18.2. Mô phỏng hiệu quả tồn kho

So sánh hai chính sách trên 30 ngày test:

**Baseline:** nhập khi tồn khả dụng `<= 10`, nhập một lượng cố định.

**Proposed:** nhập theo forecast, lead time và safety stock.

Đánh giá:

- Số lần stockout.
- Số ngày stockout.
- Service level.
- Tồn trung bình.
- Số lượng nhập dư cuối kỳ.

Không chỉ báo cáo sai số dự báo. Một dự báo tốt phải cải thiện quyết định tồn kho.

### 18.3. Trung thực khi kết luận

Phải ghi:

- Dữ liệu là mô phỏng.
- Kết quả chứng minh tính đúng đắn kỹ thuật và khả năng áp dụng, không chứng minh hiệu quả thương mại thực tế.
- Cần hiệu chỉnh lại tham số khi có dữ liệu thật.
- Chưa tính hàng đang đặt do chưa có purchase order module.
- Chưa tính đầy đủ ảnh hưởng giá, marketing, thời tiết và mùa vụ thực tế.

---

## 19. Kế hoạch 4 tuần

### Tuần 1 — Dữ liệu và nền tảng

Mục tiêu: lấy được chuỗi nhu cầu hằng ngày cho từng SKU.

- [x] Ngày 1: tạo branch và migration V13.
- [x] Ngày 1: tạo entity, enum và repository cơ bản.
- [x] Ngày 2: tạo `ForecastDemoDataSeeder`.
- [x] Ngày 3: sinh dữ liệu 180 ngày có seed cố định.
- [x] Ngày 4: viết truy vấn daily demand theo variant.
- [x] Ngày 4: lấp ngày không bán bằng 0.
- [x] Ngày 5: integration test truy vấn và seeder.
- [x] Ngày 6: sửa `forecast` giả và fallback mock gây nhầm lẫn.
- [x] Ngày 7: chạy lại toàn bộ test hiện có.

Điều kiện kết thúc tuần:

- Có dữ liệu demo tái tạo được.
- API/service nội bộ lấy được 180 điểm dữ liệu/ngày cho mỗi SKU.
- Đơn hủy không bị tính.

### Tuần 2 — Forecast và recommendation

Mục tiêu: backend sinh được suggestion hoàn chỉnh.

- [x] Ngày 8: Moving Average và test.
- [x] Ngày 9: EWMA và test.
- [x] Ngày 10: Croston và test.
- [x] Ngày 11: walk-forward backtest.
- [x] Ngày 12: MAE, WAPE, model selection, confidence.
- [x] Ngày 13: safety stock, reorder point, suggested quantity.
- [x] Ngày 14: lưu forecast run và recommendation.

Điều kiện kết thúc tuần:

- Ba thuật toán chạy được.
- Có metric cho từng SKU đủ dữ liệu.
- Service trả được số lượng đề xuất và lời giải thích.

### Tuần 3 — API và giao diện admin

Mục tiêu: demo được luồng admin hoàn chỉnh.

- [x] Ngày 15: controller và API list/detail/generate.
- [x] Ngày 16: API policy.
- [x] Ngày 17: API accept/adjust/dismiss.
- [x] Ngày 18: frontend types, api và bảng suggestion.
- [x] Ngày 19: filter, KPI và trạng thái.
- [x] Ngày 20: dialog biểu đồ và explanation.
- [x] Ngày 21: tích hợp với form nhập kho hiện tại.

Điều kiện kết thúc tuần:

- Admin xem, duyệt, sửa và bỏ qua được.
- Accept không tăng tồn.
- Hàng thực tế về mới đi qua import stock.

### Tuần 4 — Đánh giá và báo cáo

Mục tiêu: hệ thống ổn định và có bằng chứng đánh giá.

- [ ] Ngày 22: hoàn thiện integration test.
- [ ] Ngày 23: chạy thí nghiệm so sánh thuật toán.
- [ ] Ngày 24: mô phỏng baseline và proposed policy.
- [ ] Ngày 25: xuất bảng MAE/WAPE/stockout/service level.
- [ ] Ngày 26: sửa lỗi UI, encoding và error state.
- [ ] Ngày 27: viết chương phương pháp và thiết kế.
- [ ] Ngày 28: viết chương thực nghiệm và hạn chế.
- [ ] Ngày 29: quay demo dự phòng.
- [ ] Ngày 30: tổng duyệt báo cáo và demo.

---

## 20. Thứ tự commit đề xuất

```text
feat(db): add replenishment forecasting schema
feat(seed): add reproducible forecast demo data
feat(report): aggregate daily demand by variant
feat(forecast): add moving average baseline
feat(forecast): add ewma and croston algorithms
feat(forecast): add walk-forward backtesting and metrics
feat(replenishment): generate explainable stock recommendations
feat(api): expose admin replenishment endpoints
feat(admin): add replenishment suggestion dashboard
feat(admin): add recommendation decision workflow
test(replenishment): cover forecasting and admin workflow
docs: add experiment results and demo guide
```

Mỗi commit nên chạy test trước khi chuyển sang bước tiếp theo.

---

## 21. Tiêu chí hoàn thành cuối cùng

Chỉ coi task hoàn thành khi tất cả điều kiện sau đạt:

### Backend

- [x] Migration chạy thành công.
- [ ] Seeder demo bật/tắt bằng cấu hình.
- [x] Daily demand đúng theo SKU và ngày.
- [x] Ba thuật toán có unit test.
- [x] Backtest không data leakage.
- [x] MAE/WAPE được tính đúng.
- [x] Mô hình được chọn tự động theo metric.
- [x] Recommendation có formula và explanation.
- [x] API có phân quyền.
- [x] Accept/adjust/dismiss được lưu.
- [x] Không có API recommendation nào tự tăng stock.

### Frontend

- [x] KPI lấy dữ liệu thật.
- [x] Bảng suggestion có filter.
- [ ] Có detail/chart/explanation.
- [x] Có loading/empty/error state.
- [x] Không fallback mock âm thầm.
- [ ] Có luồng điền recommendation vào form nhập kho.

### Chất lượng

- [ ] Backend test pass.
- [x] Frontend lint/build pass.
- [ ] Không làm hỏng test hiện có.
- [x] Không có secret trong repository.
- [ ] Dữ liệu demo được gắn nhãn rõ ràng.
- [ ] Có kịch bản demo dự phòng.

### Báo cáo

- [x] Nêu rõ dữ liệu mô phỏng.
- [x] Mô tả công thức và thuật toán.
- [x] Có bảng so sánh ba thuật toán.
- [x] Có MAE và WAPE.
- [ ] Có đánh giá stockout/service level.
- [ ] Có phần giới hạn và hướng phát triển.

---

## 22. Kịch bản demo bảo vệ

Thời lượng đề xuất: 5–7 phút.

### Bước 1 — Giới thiệu vấn đề

> Cảnh báo tồn kho theo ngưỡng cố định không phân biệt SKU bán nhanh và bán chậm. Hai SKU cùng còn 10 sản phẩm nhưng mức rủi ro có thể hoàn toàn khác nhau.

### Bước 2 — Hiển thị dữ liệu

- Mở lịch sử bán của một SKU.
- Chỉ ra các ngày bán và ngày không bán.
- Nói rõ dữ liệu demo được sinh có kiểm soát.

### Bước 3 — So sánh thuật toán

- Hiển thị MAE/WAPE của ba thuật toán.
- Chỉ ra hệ thống chọn model tốt nhất cho SKU.

### Bước 4 — Giải thích recommendation

- Tồn khả dụng.
- Dự báo nhu cầu/ngày.
- Lead time.
- Safety stock.
- Ngày dự kiến hết.
- Số lượng đề xuất sau khi làm tròn pack size.

### Bước 5 — Quyết định của admin

- Điều chỉnh hoặc chấp nhận đề xuất.
- Chứng minh tồn kho chưa tăng.
- Khi giả định hàng đã về, dùng form nhập kho.
- Chứng minh có inventory transaction.

### Bước 6 — Kết quả đánh giá

- So sánh baseline với proposed.
- Nêu thay đổi stockout rate và service level.
- Kết luận đúng phạm vi, không phóng đại dữ liệu mô phỏng.

---

## 23. Cấu trúc báo cáo đề xuất

### Chương 1 — Tổng quan

- Bài toán quản lý tồn kho.
- Lý do chọn đề tài.
- Mục tiêu.
- Phạm vi.

### Chương 2 — Cơ sở lý thuyết

- Demand forecasting.
- Moving Average.
- EWMA.
- Croston.
- MAE và WAPE.
- Safety stock và reorder point.

### Chương 3 — Phân tích và thiết kế

- Kiến trúc hiện tại.
- Luồng dữ liệu.
- Database bổ sung.
- API.
- Quy tắc nghiệp vụ.

### Chương 4 — Cài đặt

- Seeder dữ liệu mô phỏng.
- Forecast algorithms.
- Backtesting.
- Recommendation engine.
- Admin UI.

### Chương 5 — Thực nghiệm

- Mô tả dữ liệu demo.
- Cách chia train/test.
- So sánh thuật toán.
- Đánh giá tồn kho.
- Ảnh màn hình kết quả.

### Chương 6 — Kết luận

- Kết quả đạt được.
- Hạn chế.
- Hướng dùng dữ liệu thật.
- Hướng mở rộng purchase order, promotion features và market basket analysis.

---

## 24. Rủi ro và phương án xử lý

| Rủi ro | Cách xử lý |
|---|---|
| Croston mất nhiều thời gian | Hoàn thành Moving Average + EWMA trước; Croston là thuật toán thứ ba |
| Dữ liệu seed vi phạm tồn kho | Tạo đơn qua service nghiệp vụ hoặc đảm bảo transaction đúng invariant |
| API generate chậm | Batch query lịch sử; chưa tối ưu bằng microservice |
| Không đủ thời gian làm biểu đồ | Ưu tiên bảng suggestion và explanation trước |
| Kết quả forecast không đẹp | Báo cáo trung thực theo từng nhóm SKU, không chỉnh tay metric |
| Dữ liệu mock bị nhầm dữ liệu thật | Hiện badge `DEMO DATA` và tắt fallback âm thầm |
| Giảng viên hỏi “AI ở đâu?” | Giải thích model selection bằng backtesting và forecasting theo SKU; dùng thuật ngữ hệ hỗ trợ quyết định thông minh |
| Admin duyệt làm tăng tồn sai | Tách trạng thái duyệt khỏi `InventoryService.importStock()` |

---

## 25. Nguyên tắc làm việc hằng ngày

1. Làm backend và test trước UI.
2. Mỗi ngày chỉ hoàn thành một lát cắt nhỏ có thể chạy được.
3. Không thêm tính năng ngoài phạm vi khi checklist chính chưa xong.
4. Không sửa số liệu metric để kết quả trông đẹp hơn.
5. Không gọi dữ liệu mô phỏng là dữ liệu thực.
6. Không để LLM quyết định số lượng nhập hàng.
7. Không thay đổi tồn kho ngoài `InventoryService`.
8. Cuối mỗi ngày ghi lại việc đã làm, lỗi còn lại và ảnh kết quả.
9. Cuối mỗi tuần phải có một phiên bản demo chạy được.
10. Luôn giữ một database demo và video demo dự phòng trước ngày báo cáo.

---

## 26. Bước bắt đầu ngay

Thực hiện đúng thứ tự sau:

1. Tạo migration `V13__replenishment_forecasting.sql`.
2. Tạo entity và enum tương ứng.
3. Viết `ForecastDemoDataSeeder` với random seed `2026`.
4. Viết truy vấn daily demand theo SKU.
5. Viết integration test chứng minh đơn hủy không được tính.
6. Chỉ sau khi dữ liệu đúng mới bắt đầu thuật toán Moving Average.

Không bắt đầu từ giao diện, chatbot hoặc biểu đồ. Nền tảng đầu tiên bắt buộc là chuỗi nhu cầu theo ngày chính xác và có thể tái tạo.

## Trạng thái tách AI Database (18/07/2026)

Đã triển khai phương án snapshot trong source:

- Core DB tiếp tục giữ 42 bảng Core và tạm giữ 3 bảng AI cũ để rollback.
- AI DB sở hữu `inventory_policies`, `forecast_runs`, `replenishment_recommendations`.
- AI DB có thêm đúng 4 read-model: `ai_product_variant_snapshot`, `ai_inventory_snapshot`, `ai_sales_daily_snapshot`, `ai_supplier_snapshot`.
- Flyway AI dùng `flyway_ai_schema_history`; migration không còn foreign key sang bảng Core.
- Core cung cấp `POST /internal/v1/ai/replenishment/snapshot`, xác thực bằng `X-AI-Sync-Secret`.
- AI cung cấp `POST /api/v1/admin/replenishment/snapshots/sync`; `generate` tự sync trước khi dự báo.
- Repository AI không còn JOIN `products`, `product_variants`, `orders`, `order_items` của Core.

### Trình tự cutover bắt buộc

1. Tạo AI PostgreSQL/Supabase project và chạy Flyway AI V1-V2.
2. Copy ba bảng AI từ Core sang AI DB theo thứ tự: `inventory_policies`, `forecast_runs`, `replenishment_recommendations`.
3. Đối chiếu `count(*)`, ID, tổng forecast/recommendation theo trạng thái giữa hai DB.
4. Cấu hình cùng một `AI_SYNC_SECRET`; AI trỏ `CORE_API_BASE_URL` về Core API.
5. Gọi `/snapshots/sync`, đối chiếu số variant và lịch sử bán theo ngày.
6. Chuyển Admin sang AI API, theo dõi ít nhất một chu kỳ dự báo và giữ bảng cũ ở Core.
7. Chỉ tạo migration xóa ba bảng AI khỏi Core sau khi đã ký xác nhận đối chiếu và hết thời gian rollback.

`ai_supplier_snapshot` hiện sẵn sàng nhưng Core chưa có bảng supplier chuẩn hóa, vì vậy snapshot supplier hợp lệ hiện là danh sách rỗng. Không suy diễn supplier từ chuỗi `inventory_policies.supplier_name`.

## 27. Cập nhật hoàn thiện ngày 19/07/2026

- Full generate đã có bounded parallelism (1–8 worker, mặc định 4), cô lập lỗi theo SKU và trả batch result.
- Có khóa chống hai batch generate đồng thời trong cùng AI instance; triển khai nhiều instance phải dùng distributed lock.
- Workflow nhập kho giữ tính nhất quán an toàn: recommendation chỉ điền form, Core import tạo `InventoryTransaction`, không tự chuyển `RECEIVED` qua lời gọi phân tán không có transaction chung.
- Verification: AI 29/29 test; Core targeted 18/18 test; Admin lint và production build pass.
- Kết quả hiệu năng 370,55 giây là baseline tuần tự cũ. Chưa có benchmark Supabase mới cho parallelism=4 nên không tuyên bố số tăng tốc chưa đo.