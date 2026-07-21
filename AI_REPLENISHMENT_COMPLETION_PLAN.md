# Kế hoạch hoàn thiện AI Replenishment

> Dự án: `smart-sportswear-shop`  
> Ngày lập kế hoạch: 19/07/2026  
> Tài liệu gốc: `AI_REPLENISHMENT_IMPLEMENTATION_PLAN.md`  
> Trạng thái xuất phát: thuật toán và phần lớn API/UI đã có; dữ liệu giao dịch demo đã tồn tại trên Supabase; các bảng snapshot, forecast và recommendation đang rỗng.

---

## 1. Mục tiêu của kế hoạch này

Kế hoạch này chỉ tập trung vào những phần còn thiếu để đưa AI Replenishment từ trạng thái “đã có code” sang trạng thái:

1. Chạy end-to-end trên Supabase bằng tài khoản Admin thật.
2. Có thể tái tạo bộ dữ liệu demo từ database trống.
3. Có đầy đủ test cho luồng nghiệp vụ quan trọng.
4. Có biểu đồ lịch sử/backtest và explanation thật trên Admin.
5. Có luồng chuyển recommendation sang form nhập kho nhưng không tự động tăng tồn.
6. Có số liệu so sánh baseline và proposed policy để sử dụng trong báo cáo.
7. Có một phiên bản Git ổn định, có thể dựng lại và demo dự phòng.

Không mở rộng sang:

- Tự động đặt hàng nhà cung cấp.
- Tự động tăng tồn khi accept recommendation.
- Machine learning phức tạp hoặc gọi LLM để tính forecast.
- Market basket analysis.
- Dự báo giá hoặc cá nhân hóa khách hàng.

---

## 2. Trạng thái xuất phát đã xác minh

### 2.1. Source code

- Core backend đang chạy tại `:8082`.
- Admin frontend đang chạy tại `:3001`.
- AI Forecasting Service có source nhưng chưa chạy tại `:8081`.
- Module replenishment cũ đã được chuyển khỏi Core sang `ai_forecasting_service`.
- `ai_forecasting_service` và nhiều file refactor hiện chưa được commit.
- AI service có 13 test và tất cả đang pass.
- Admin ESLint đang pass.
- Core test suite chưa chạy được vì `TEST_DB_URL` chưa được cung cấp đúng định dạng JDBC.

### 2.2. Supabase Core data

| Chỉ số | Giá trị đã kiểm tra |
|---|---:|
| Product variant | 545 |
| Active variant | 545 |
| Tổng order | 3.008 |
| Order có marker `[FORECAST_DEMO]` | 3.000 |
| Khoảng dữ liệu demo | 18/01/2026–16/07/2026 |
| Số ngày có dữ liệu demo | 180 |
| Variant có bán hợp lệ | 50 |
| Cancelled demo order | 153 |
| Pending demo order | 84 |
| Variant có prefix `FD-` | 0 |
| Tồn kho âm | 0 |
| Reserved lớn hơn stock | 0 |

### 2.3. Supabase AI data

Các migration AI V1–V2 đã chạy, nhưng toàn bộ dữ liệu vận hành AI đang rỗng:

| Bảng | Số dòng xuất phát |
|---|---:|
| `inventory_policies` | 0 |
| `forecast_runs` | 0 |
| `replenishment_recommendations` | 0 |
| `ai_product_variant_snapshot` | 0 |
| `ai_inventory_snapshot` | 0 |
| `ai_sales_daily_snapshot` | 0 |
| `ai_supplier_snapshot` | 0 |

Đây là baseline bắt buộc dùng để so sánh sau khi hoàn thành từng giai đoạn.

---

## 3. Các quyết định phải giữ cố định

### 3.1. Kiến trúc

- Core backend sở hữu dữ liệu commerce và tồn kho thật.
- AI service sở hữu policy, forecast, recommendation và read-model snapshot.
- AI không truy cập trực tiếp entity/repository Core.
- AI lấy dữ liệu qua endpoint snapshot nội bộ của Core.
- Mọi thay đổi tồn kho thật chỉ đi qua `InventoryService`.
- Accept/adjust/dismiss chỉ lưu quyết định, không tăng tồn kho.

### 3.2. Dữ liệu demo

Chọn một trong hai phương án trước khi sửa seeder:

#### Phương án A — Khuyến nghị

Giữ 50 variant catalog hiện có làm dữ liệu forecast demo và cập nhật tài liệu chính thức:

- Không yêu cầu prefix `FD-`.
- Gắn quan hệ demo bằng marker trên order và metadata/profile seed.
- Không tạo thêm sản phẩm trùng lặp.
- Báo cáo nêu rõ dữ liệu mô phỏng sử dụng catalog hợp lệ của hệ thống.

Ưu điểm: khớp Supabase hiện tại, ít rủi ro, không phải tạo lại catalog.

#### Phương án B

Khôi phục đúng thiết kế cũ:

- Tạo 30 variant riêng có prefix `FD-`.
- Sinh lại 3.000 order chỉ cho 30 variant này.
- Dọn bộ demo cũ một cách có kiểm soát.

Chỉ dùng phương án B nếu giảng viên hoặc yêu cầu nghiệm thu bắt buộc phải có `FD-`.

Mặc định kế hoạch này sử dụng **Phương án A**. Không được vừa giữ 50 variant hiện tại vừa tuyên bố có 30 SKU `FD-`.

### 3.3. Database

Trong giai đoạn hoàn thiện trước mắt:

- Cho phép Core và AI tiếp tục dùng chung Supabase/schema hiện tại.
- Vẫn giữ ranh giới ownership bằng code và read-model.
- Không xóa ba bảng AI cũ khỏi Core.
- Không tiếp tục cutover sang Supabase project thứ hai cho đến khi luồng shared-database chạy end-to-end và có biên bản đối chiếu.

---

## 4. Thứ tự triển khai bắt buộc

```text
Ổn định Git và cấu hình
        |
        v
Khởi động AI + sync snapshot thật
        |
        v
Generate forecast/recommendation thật
        |
        v
Sửa lỗi và bổ sung test backend
        |
        v
History/backtest chart + explanation
        |
        v
Điền recommendation vào form nhập kho
        |
        v
Seeder tái lập được
        |
        v
Thí nghiệm baseline/proposed
        |
        v
Báo cáo, demo dự phòng và nghiệm thu
```

Không làm UI chart hoặc báo cáo số liệu cuối trước khi snapshot và generate thực tế thành công.

---

## 5. Giai đoạn 0 — Ổn định worktree và cấu hình

### Mục tiêu

Tạo một trạng thái source có thể build lại, không mất phần refactor AI và không phụ thuộc vào cấu hình bí mật trong repository.

### Công việc

- [x] Rà toàn bộ `git status`.
- [ ] Xác nhận các file bị xóa khỏi Core là chủ đích do chuyển sang AI service.
- [x] Đảm bảo `ai_forecasting_service` có đầy đủ source, test, migration, `pom.xml`, Dockerfile và README.
- [x] Đảm bảo `.env`, `.env.local`, credential và JWT secret đều được ignore.
- [x] Cập nhật `.env.example` cho Core, AI và Admin.
- [ ] Thống nhất port:
  - Core: `8082`
  - AI: `8081`
  - Admin: `3001`
  - Storefront: `3000`
- [x] Thống nhất cùng `AI_SYNC_SECRET` giữa Core và AI.
- [x] Thống nhất cùng JWT access secret giữa Core và AI để AI xác thực token Admin do Core phát hành.
- [x] Kiểm tra `CORE_API_BASE_URL`.
- [x] Ghi rõ shared-database mode trong README và runbook.
- [ ] Tạo một commit checkpoint trước khi sửa tiếp.

### Tiêu chí nghiệm thu

- [ ] `git status` không còn file AI quan trọng ở trạng thái untracked ngoài chủ đích.
- [x] Không có secret thật trong file tracked.
- [x] Core compile thành công.
- [x] AI compile và 13 test hiện có pass.
- [x] Admin lint pass.
- [x] Có hướng dẫn chạy local từ repository mới clone.

### Bằng chứng cần lưu

- Kết quả `git status --short`.
- Kết quả build/test.
- Danh sách biến môi trường cần thiết, không ghi giá trị secret.

---

## 6. Giai đoạn 1 — Chạy snapshot end-to-end trên Supabase

### Mục tiêu

Ghi dữ liệu từ Core vào bốn read-model AI bằng đúng endpoint và tài khoản Admin thật.

### Công việc

- [x] Khởi động Core và kiểm tra `/actuator/health`.
- [x] Khởi động AI service và kiểm tra `/actuator/health`.
- [x] Đăng nhập Admin thật qua giao diện hoặc API Core.
- [x] Kiểm tra JWT Admin gọi được API AI.
- [x] Gọi `POST /api/v1/admin/replenishment/snapshots/sync`.
- [x] Xác nhận AI gửi đúng `X-AI-Sync-Secret` về Core.
- [ ] Xác nhận Core chỉ trả:
  - `CONFIRMED`
  - `PACKING`
  - `SHIPPING`
  - `DELIVERED`
- [ ] Xác nhận Core loại:
  - `CANCELLED`
  - `PENDING_CONFIRMATION`
- [x] Xác nhận ngày không bán được xử lý thành 0 khi tạo chuỗi demand.
- [x] Chạy sync lần hai để kiểm tra idempotency.

### Kết quả dữ liệu mong đợi

Không hard-code chính xác nếu dữ liệu Core thay đổi, nhưng lần sync đầu phải thỏa:

- `ai_product_variant_snapshot`: bằng số active variant được Core trả về, dự kiến 545.
- `ai_inventory_snapshot`: có một snapshot cho mỗi variant ở lần chụp đầu.
- `ai_sales_daily_snapshot`: lớn hơn 0 và khớp tổng hợp Core.
- `ai_supplier_snapshot`: có thể bằng 0 vì Core chưa có supplier chuẩn hóa.
- `inventory_policies`: được tự tạo policy mặc định cho các variant được sync.

### Truy vấn đối chiếu bắt buộc

```sql
select count(*) from ai_product_variant_snapshot;
select count(*) from ai_inventory_snapshot;
select count(*) from ai_sales_daily_snapshot;
select count(*) from ai_supplier_snapshot;
select count(*) from inventory_policies;

select count(*)
from ai_inventory_snapshot
where stock_quantity < 0
   or reserved_quantity < 0
   or reserved_quantity > stock_quantity;

select count(*)
from ai_sales_daily_snapshot
where quantity < 0;
```

### Tiêu chí nghiệm thu

- [x] Sync trả HTTP thành công bằng Admin thật.
- [x] Customer hoặc request không có JWT bị từ chối.
- [x] Secret sai bị từ chối tại Core.
- [x] Product, inventory và sales snapshot có dữ liệu.
- [x] Không có snapshot vi phạm constraint.
- [x] Sync lần hai không nhân đôi product hoặc sales daily.
- [x] Policy Admin đã sửa không bị sync ghi đè.
- [ ] Số liệu Core và AI được lưu vào bảng đối chiếu.

---

## 7. Giai đoạn 2 — Generate forecast và recommendation thật

### Mục tiêu

Chạy forecast trên snapshot Supabase và lưu được kết quả cho từng SKU hợp lệ.

### Công việc

- [x] Gọi `POST /api/v1/admin/replenishment/generate`.
- [x] Xác nhận generate tự sync trước khi forecast.
- [x] Ghi log theo từng variant nhưng không ghi dữ liệu nhạy cảm.
- [x] Không để một SKU lỗi làm hỏng toàn bộ batch nếu có thể cô lập lỗi an toàn.
- [ ] Xác nhận mỗi forecast run lưu:
  - thuật toán được chọn;
  - train range;
  - horizon;
  - average daily demand;
  - forecast quantity;
  - MAE;
  - WAPE;
  - residual standard deviation;
  - confidence.
- [ ] Xác nhận recommendation lưu:
  - available;
  - incoming;
  - safety stock;
  - reorder point;
  - suggested quantity;
  - estimated stockout days;
  - priority;
  - explanation JSON.
- [ ] Xác nhận pending recommendation cũ được cập nhật đúng quy tắc, không tạo nhiều pending row cho cùng variant.
- [ ] Xác nhận variant không cần nhập không bị tạo recommendation thừa, trừ khi business rule yêu cầu lưu để giải thích.

### Truy vấn đối chiếu bắt buộc

```sql
select algorithm, count(*), avg(mae), avg(wape)
from forecast_runs
group by algorithm
order by algorithm;

select confidence, count(*)
from forecast_runs
group by confidence
order by confidence;

select status, priority, count(*), sum(suggested_quantity)
from replenishment_recommendations
group by status, priority
order by status, priority;

select variant_id, count(*)
from replenishment_recommendations
where status = 'PENDING'
group by variant_id
having count(*) > 1;
```

### Tiêu chí nghiệm thu

- [x] `forecast_runs > 0`.
- [ ] Có đủ kết quả của Moving Average, EWMA hoặc Croston tùy SKU.
- [x] Không có forecast quantity âm hoặc NaN.
- [x] `replenishment_recommendations > 0`.
- [x] Không có suggested quantity âm.
- [x] Không trùng pending recommendation theo variant.
- [x] Có ít nhất một recommendation `CRITICAL` hoặc `HIGH`.
- [ ] Có ít nhất một SKU không cần nhập.
- [ ] Generate lần hai không làm sai trạng thái recommendation đã được Admin xử lý.

---

## 8. Giai đoạn 3 — Hoàn thiện backend test

### 8.1. Sửa hạ tầng test Core

- [x] Sửa cách cung cấp `TEST_DB_URL`.
- [x] Đảm bảo URL test bắt đầu bằng `jdbc:postgresql://`.
- [x] Không cho test vô tình kết nối Supabase production/demo.
- [x] Ưu tiên Testcontainers PostgreSQL cho integration test.
- [ ] Chạy toàn bộ Core test suite.

### 8.2. Unit test thuật toán

#### Moving Average

- [x] Chuỗi đều dự báo đúng.
- [x] Chuỗi toàn 0 trả 0.
- [x] Chuỗi ngắn hơn window vẫn chạy.
- [x] Không trả số âm.

#### EWMA

- [x] Nhu cầu gần đây ảnh hưởng forecast.
- [ ] Alpha biên và alpha không hợp lệ được kiểm tra.
- [x] Chuỗi toàn 0 trả 0.
- [ ] Không trả NaN với input bất thường hợp lệ.

#### Croston

- [x] Chuỗi gián đoạn trả forecast hợp lý.
- [x] Chuỗi toàn 0 trả 0.
- [x] Một lần phát sinh không làm lỗi.

### 8.3. Unit test backtest

- [x] Không data leakage.
- [x] MAE khớp ví dụ tính tay.
- [x] WAPE khớp ví dụ tính tay.
- [x] Tổng actual bằng 0 không chia cho 0.
- [x] Chọn model có metric tốt nhất.
- [x] Tie-break chọn model đơn giản hơn.
- [ ] Dữ liệu không đủ trả confidence thấp và không crash.

### 8.4. Unit test recommendation

- [ ] `available = stock - reserved`.
- [ ] Safety stock đúng với service level.
- [ ] Reorder point đúng.
- [ ] Target stock đúng.
- [ ] Không đề xuất âm.
- [ ] Áp dụng minimum order quantity.
- [ ] Làm tròn pack size.
- [ ] Nhu cầu 0 không chia cho 0.
- [ ] Estimated stockout days đúng.
- [ ] Priority đúng.
- [ ] Explanation khớp số đã tính.

### 8.5. Integration test snapshot/generate/API

- [x] Migration AI V1–V2 chạy trên PostgreSQL trống.
- [x] Sync product/inventory/sales snapshot.
- [x] Sync lần hai không nhân đôi policy.
- [x] Không ghi đè policy Admin.
- [ ] Cancelled không được tính.
- [ ] Pending confirmation không được tính.
- [ ] Variant inactive bị bỏ qua.
- [ ] Ngày không bán được điền 0.
- [x] Admin xem được suggestion.
- [x] Customer nhận 403.
- [x] Request thiếu JWT nhận 401.
- [x] Accept lưu principal.
- [x] Adjust yêu cầu quantity hợp lệ.
- [x] Dismiss yêu cầu note.
- [x] Transition sai bị từ chối.
- [x] Accept không tăng stock.
- [ ] Import stock vẫn tạo `InventoryTransaction`.

### Tiêu chí nghiệm thu

- [x] AI test pass 100%.
- [ ] Core test pass 100%.
- [x] Không test nào dùng Supabase thật để ghi dữ liệu.
- [x] Có test riêng cho các business rule quan trọng.
- [ ] Kết quả test được lưu làm bằng chứng báo cáo.

---

## 9. Giai đoạn 4 — Hoàn thiện API detail, chart và explanation

### Mục tiêu

Không còn trả `historyData = []`; dialog Admin hiển thị được dữ liệu thật.

### Backend

- [x] Thiết kế DTO chart gồm tối thiểu:
  - `date`;
  - `actualQuantity`;
  - `predictedQuantity`;
  - `isBacktestPeriod`.
- [x] Trả lịch sử daily demand có ngày bằng 0.
- [x] Trả predicted series của thuật toán được chọn trong backtest window.
- [x] Trả metrics của cả ba thuật toán.
- [x] Trả model được chọn và lý do chọn.
- [x] Trả explanation có cấu trúc thay vì chỉ text rời.
- [x] Giới hạn số điểm chart hợp lý, mặc định 180 ngày.
- [x] Tránh N+1 query.

### Frontend

- [x] Hiển thị Actual và Predicted bằng hai đường khác nhau.
- [x] Đánh dấu vùng backtest.
- [x] Hiển thị MAE/WAPE của ba thuật toán.
- [x] Làm nổi bật model thắng.
- [ ] Hiển thị công thức:
  - available;
  - safety stock;
  - reorder point;
  - target stock;
  - raw suggestion;
  - pack-size rounding.
- [x] Có loading, empty và error state riêng cho dialog.
- [x] Chart responsive.
- [x] Không hiện chart giả nếu backend không có dữ liệu.

### Tiêu chí nghiệm thu

- [x] API detail không còn trả history rỗng với SKU có lịch sử.
- [ ] Số actual trên chart đối chiếu được với `order_items`.
- [x] Predicted series chỉ dùng dữ liệu quá khứ tại từng điểm backtest.
- [x] Dialog hiển thị explanation khớp recommendation đã lưu.
- [x] Frontend lint và production build pass.

---

## 10. Giai đoạn 5 — Tích hợp recommendation với form nhập kho

### Mục tiêu

Cho Admin đưa số lượng recommendation sang form nhập kho, nhưng vẫn yêu cầu thao tác nhập kho riêng.

### Luồng chuẩn

```text
Recommendation PENDING
        |
        v
Admin accept/adjust
        |
        v
Nút "Điền vào form nhập kho"
        |
        v
Form nhập kho được prefill variant + quantity + note
        |
        v
Admin kiểm tra và xác nhận
        |
        v
Core InventoryService.importStock()
        |
        v
InventoryTransaction được tạo
```

### Công việc

- [ ] Thêm callback hoặc shared state để chuyển:
  - `variantId`;
  - SKU;
  - suggested/admin quantity;
  - recommendation ID;
  - note.
- [x] Mở đúng form `IMPORT`.
- [x] Cho Admin sửa quantity trước khi submit.
- [x] Không tự submit.
- [x] Ghi note tham chiếu recommendation ID.
- [x] Sau import thành công, refresh inventory.
- [ ] Quyết định rõ có chuyển recommendation sang `RECEIVED` tự động hay bằng thao tác riêng.
- [ ] Nếu tự chuyển `RECEIVED`, chỉ thực hiện sau khi Core import thành công và cần xử lý lỗi phân tán rõ ràng.
- [x] Trong MVP an toàn, ưu tiên chưa tự chuyển `RECEIVED`; hiển thị trạng thái nhập kho độc lập.

### Tiêu chí nghiệm thu

- [x] Accept recommendation không thay đổi stock.
- [x] Nút điền form không thay đổi stock.
- [x] Chỉ submit form import mới thay đổi stock.
- [x] Import tạo `InventoryTransaction`.
- [x] Số lượng import mặc định khớp admin quantity hoặc suggested quantity.
- [x] Lỗi import không làm recommendation bị ghi nhận là đã nhận hàng.

---

## 11. Giai đoạn 6 — Khôi phục seeder tái lập được

### Mục tiêu

Có thể tạo lại dữ liệu demo trên database trống hoặc database demo mà không làm ảnh hưởng dữ liệu người dùng.

### Vị trí ownership

Seeder giao dịch thuộc Core backend vì ghi vào:

- `orders`;
- `order_items`;
- product/variant catalog;
- inventory snapshot hiện tại.

AI service không trực tiếp seed bảng Core.

### Công việc theo Phương án A

- [ ] Khôi phục `ForecastDemoDataSeeder` trong Core.
- [ ] Khôi phục properties:

```yaml
app:
  forecast-demo:
    enabled: false
    random-seed: 2026
    history-days: 180
    order-count: 3000
    variant-count: 50
```

- [ ] Chọn cố định 50 active variant theo thứ tự ổn định.
- [ ] Gán bốn demand profiles:
  - fast;
  - normal;
  - slow;
  - intermittent.
- [ ] Sinh cuối tuần, trend và promotion spikes có kiểm soát.
- [ ] Gắn `[FORECAST_DEMO]` vào `orders.note`.
- [ ] Cleanup chỉ order/order item có marker.
- [ ] Không xóa product hoặc order người dùng.
- [ ] Dùng seed `2026`.
- [ ] Seeder tắt mặc định.
- [ ] Chặn chạy trong production profile.

### Test bắt buộc

- [ ] Cùng seed cho cùng daily demand.
- [ ] Có đúng khoảng 3.000 demo orders.
- [ ] Có đủ 180 ngày.
- [ ] Có cancelled và pending.
- [ ] Có ít nhất 2.000 order hợp lệ.
- [ ] Chạy lần hai không nhân đôi.
- [ ] Cleanup không xóa order người dùng.
- [ ] Không vi phạm FK/check constraint.
- [ ] Không tồn kho âm.
- [ ] Reserved không lớn hơn stock.
- [ ] Tổng demand truy ngược được từ `order_items`.
- [ ] Seeder không chạy khi disabled.

### Tiêu chí nghiệm thu

- [ ] Có thể tạo database demo mới mà không copy thủ công Supabase hiện tại.
- [ ] Kết quả cùng seed có thể tái lập.
- [ ] README có câu lệnh seed và cảnh báo.
- [ ] Dữ liệu trên UI có badge `DEMO DATA`.

---

## 12. Giai đoạn 7 — Thí nghiệm baseline và proposed policy

### Mục tiêu

Tạo bằng chứng định lượng để trả lời hệ thống mới có cải thiện quyết định tồn kho hay không.

### 12.1. Thí nghiệm forecast

Giữ thiết kế:

- Lịch sử: 180 ngày.
- Backtest: 30 ngày cuối.
- Walk-forward.
- Metrics:
  - MAE;
  - WAPE.
- So sánh:
  - Moving Average;
  - EWMA;
  - Croston.

Xuất bảng:

| Nhóm SKU | Thuật toán | Số SKU thắng | MAE | WAPE |
|---|---|---:|---:|---:|
| Fast | | | | |
| Normal | | | | |
| Slow | | | | |
| Intermittent | | | | |

### 12.2. Baseline policy

Định nghĩa baseline cố định trước khi chạy:

- Ví dụ nhập khi available `<= 10`.
- Số lượng nhập theo mức cover cố định hoặc rule hiện tại.
- Không thay đổi baseline sau khi nhìn kết quả.

### 12.3. Proposed policy

Sử dụng:

- model tốt nhất theo SKU;
- lead time;
- service level;
- safety stock;
- reorder point;
- target cover;
- MOQ;
- pack size.

### 12.4. Mô phỏng nhiều chu kỳ

- [ ] Chia lịch sử thành warm-up và evaluation period.
- [ ] Mỗi ngày cập nhật on-hand giả lập.
- [ ] Trừ demand thực tế.
- [ ] Tạo replenishment order theo baseline/proposed.
- [ ] Nhận hàng sau lead time.
- [ ] Không dùng dữ liệu tương lai để quyết định.
- [ ] Chạy cùng initial inventory và cùng demand cho cả hai policy.

### Metrics tồn kho

- Stockout days.
- Stockout rate.
- Units short.
- Fill rate hoặc service level.
- Average on-hand.
- Số lần nhập.
- Tổng số lượng nhập.
- Nếu đủ thời gian: holding-cost proxy.

### Tiêu chí nghiệm thu

- [ ] Có bảng forecast theo thuật toán và nhóm SKU.
- [ ] Có bảng baseline vs proposed.
- [x] Có stockout rate.
- [x] Có service level/fill rate.
- [x] Có average inventory.
- [ ] Có mô tả giả định lead time và initial stock.
- [ ] Kết quả có thể tái tạo bằng seed cố định.
- [ ] Không chỉnh tay metric hoặc loại SKU để làm đẹp kết quả.
- [ ] Nếu proposed không tốt hơn, báo cáo trung thực và phân tích nguyên nhân.

---

## 13. Giai đoạn 8 — Bảo mật, audit và vận hành

### Bảo mật

- [x] Tất cả AI Admin API có `@PreAuthorize`.
- [x] Customer không gọi được API AI Admin.
- [x] Snapshot Core chỉ xác thực bằng constant-time secret comparison.
- [x] `actedBy` chỉ lấy từ JWT principal.
- [x] Không nhận `actedBy` từ body.
- [x] Validate service level `(0,1)`.
- [x] Validate lead time không âm.
- [x] Validate target cover, MOQ và pack size lớn hơn 0.
- [x] Validate adjust quantity không âm.
- [x] Dismiss bắt buộc note.
- [x] Validate state transition.

### Audit

- [ ] Ghi audit cho:
  - policy update;
  - generate;
  - accept;
  - adjust;
  - dismiss;
  - import stock có nguồn từ recommendation.
- [ ] Audit có actor, time, entity ID và before/after phù hợp.
- [ ] Không ghi JWT hoặc secret vào log.

### Vận hành

- [x] Health check Core, AI và database.
- [ ] Timeout hợp lý khi AI gọi Core.
- [ ] Lỗi một SKU được log đủ để điều tra.
- [x] Generate chống double submit ở frontend.
- [x] Cân nhắc khóa hoặc idempotency cho hai request generate đồng thời.
- [x] Có backup trước migration/cutover.

---

## 14. Giai đoạn 9 — Báo cáo và demo dự phòng

### Báo cáo

- [x] Cập nhật kiến trúc microservice và read-model snapshot.
- [x] Nêu rõ hiện tại shared-database mode hay separate database mode.
- [x] Nêu rõ dữ liệu mô phỏng.
- [ ] Nêu đúng số variant dùng trong demo.
- [ ] Mô tả cách seed và seed `2026`.
- [ ] Mô tả filter trạng thái order.
- [x] Mô tả ba thuật toán.
- [x] Mô tả walk-forward và chống data leakage.
- [x] Có MAE/WAPE.
- [x] Có baseline/proposed.
- [x] Có stockout/service level.
- [ ] Có giới hạn:
  - dữ liệu mô phỏng;
  - lịch sử ngắn;
  - demand thưa;
  - chưa có supplier/purchase order thật;
  - chưa tự động đặt hàng.
- [ ] Có hướng phát triển.

### Demo 5–7 phút

1. Đăng nhập Admin thật.
2. Mở tồn kho và chỉ ra SKU có rủi ro.
3. Chạy forecast.
4. Mở detail:
   - actual history;
   - backtest;
   - MAE/WAPE;
   - model thắng.
5. Giải thích safety stock, reorder point và suggested quantity.
6. Accept hoặc adjust.
7. Chứng minh stock chưa tăng.
8. Điền recommendation vào form nhập kho.
9. Submit import.
10. Chứng minh stock và inventory transaction đã thay đổi đúng.
11. Trình bày bảng baseline vs proposed.

### Demo dự phòng

- [x] Có database demo đã generate sẵn.
- [x] Có tài khoản Admin demo đã kiểm tra.
- [ ] Có ảnh chụp các màn hình chính.
- [x] Có file CSV/bảng kết quả thí nghiệm.
- [ ] Có video demo dự phòng.
- [ ] Có script kiểm tra health và dữ liệu trước buổi bảo vệ.

---

## 15. Chiến lược commit đề xuất

Không gom toàn bộ phần còn lại vào một commit.

1. `chore(ai): stabilize forecasting service extraction`
2. `test(core): restore isolated integration test database`
3. `fix(ai): complete supabase snapshot synchronization`
4. `feat(ai): generate persisted forecasts and recommendations`
5. `test(ai): cover recommendation and admin action rules`
6. `feat(ai): expose history and backtest detail`
7. `feat(admin): render forecast chart and explanation`
8. `feat(admin): prefill inventory import from recommendation`
9. `feat(seed): restore reproducible forecast demo dataset`
10. `feat(evaluation): compare baseline and replenishment policy`
11. `docs(ai): finalize report evidence and demo runbook`

Mỗi commit phải:

- build được;
- test phần liên quan;
- không chứa secret;
- không làm thay đổi dữ liệu Supabase ngoài công việc đã chủ động thực hiện.

---

## 16. Checklist hoàn thành cuối cùng

### End-to-end

- [x] Core health `UP`.
- [x] AI health `UP`.
- [x] Admin đăng nhập được.
- [x] Sync snapshot thành công.
- [x] Generate thành công.
- [x] Có forecast trong Supabase.
- [x] Có recommendation trong Supabase.
- [x] Accept/adjust/dismiss hoạt động.
- [x] Accept không tăng stock.
- [x] Import stock tạo inventory transaction.

### Dữ liệu

- [ ] Dữ liệu demo tái lập được.
- [ ] Có 180 ngày.
- [ ] Có đủ demand profiles.
- [ ] Cancelled/pending bị loại.
- [ ] Không tồn kho âm.
- [ ] Không nhân đôi khi chạy lại.
- [ ] Dữ liệu được gắn nhãn mô phỏng rõ ràng.

### Backend

- [x] AI test pass.
- [ ] Core test pass.
- [x] Migration chạy trên database trống.
- [x] Snapshot idempotent.
- [x] Recommendation formula có test.
- [x] API security có test.
- [x] Admin action có test.

### Frontend

- [x] KPI thật.
- [x] Filter/search thật.
- [x] Detail thật.
- [x] History/backtest chart thật.
- [x] Explanation thật.
- [x] Loading/empty/error đầy đủ.
- [x] Không mock fallback.
- [x] Có luồng điền form nhập kho.
- [x] Lint pass.
- [x] Production build pass.

### Thực nghiệm và báo cáo

- [x] Có bảng MAE/WAPE.
- [ ] Có bảng model thắng theo nhóm SKU.
- [x] Có baseline vs proposed.
- [x] Có stockout rate.
- [x] Có service level/fill rate.
- [x] Có average inventory.
- [ ] Có phần giới hạn.
- [ ] Có hướng phát triển.
- [ ] Có demo dự phòng.

---

## 17. Việc cần làm ngay trong phiên tiếp theo

Thực hiện đúng thứ tự:

1. Ổn định và commit phần tách `ai_forecasting_service`.
2. Khởi động AI service với cấu hình Supabase hiện tại.
3. Đăng nhập Admin thật.
4. Chạy `/snapshots/sync`.
5. Đối chiếu số dòng năm bảng snapshot/policy.
6. Chạy `/generate`.
7. Đối chiếu `forecast_runs` và `replenishment_recommendations`.
8. Ghi lại lỗi theo từng SKU nếu generate chưa hoàn tất.
9. Chỉ sau khi end-to-end thành công mới làm chart và form nhập kho.

### Điều kiện dừng của phiên tiếp theo

Phiên tiếp theo chỉ được coi là hoàn thành khi:

- AI service health `UP`;
- snapshot có dữ liệu thật trên Supabase;
- policy có dữ liệu;
- generate tạo được forecast;
- generate tạo được recommendation;
- số liệu trước/sau được ghi lại vào `AI_REPLENISHMENT_PROGRESS.md`.

Nếu một bước thất bại, phải lưu:

- endpoint đã gọi;
- HTTP status;
- error message đã loại bỏ secret;
- bảng/dòng bị ảnh hưởng;
- nguyên nhân gốc;
- thay đổi code hoặc cấu hình đã thực hiện;
- kết quả chạy lại.

## 12. Cập nhật thực thi mới nhất — 19/07/2026

### Đã hoàn thành trong vòng sửa cuối

- [x] Generate dùng bounded parallelism cấu hình 1–8 worker, mặc định 4.
- [x] Lỗi một SKU được cô lập và trả về bằng `failedVariantIds`.
- [x] Hai batch generate chồng lấn trong cùng instance bị từ chối.
- [x] Admin hiển thị thống kê batch thật thay vì thông báo thành công chung.
- [x] Luồng điền form không tự động đổi recommendation sau import; tránh trạng thái phân tán không có transaction chung.
- [x] AI test 29/29 pass.
- [x] Core targeted test 18/18 pass (`InventoryIntegrationTest`, `AiReplenishmentDataControllerIntegrationTest`).
- [x] Admin lint và production build pass.
- [x] README AI được chuẩn hóa UTF-8, có port, env, shared-database mode và giới hạn khóa một instance.

### Còn là việc vận hành/ngoài source code

- [ ] Chạy benchmark lại full generate 545 SKU trên Supabase với parallelism=4 và lưu thời gian thực đo.
- [ ] Chạy full Core suite thành CI gate; hiện suite trực tiếp liên quan AI đã pass nhưng full suite từng vượt 10 phút.
- [ ] Distributed lock nếu triển khai nhiều AI instance.
- [ ] Ảnh chụp/video demo dự phòng cần người vận hành thực hiện trong môi trường đăng nhập.

Các mục trên không được đánh dấu hoàn thành nếu chưa có bằng chứng thực đo. Baseline full generate tuần tự hiện có là 370,55 giây.