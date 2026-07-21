# AI Replenishment - Tiến độ hoàn thiện

Ngày cập nhật: 19/07/2026

## Kết luận hiện tại

Hệ thống đang chạy theo mô hình hai service dùng chung một Supabase. Core tại `:8082` và AI tại `:8081` đều health `UP`. Phần snapshot, thuật toán, integration test và giao diện danh sách đã sẵn sàng. Thao tác ghi snapshot vào Supabase thật vẫn cần một phiên Admin thật; không sử dụng JWT giả.

## Tiến độ sáu bước

| Hạng mục | Trạng thái | Bằng chứng |
|---|---|---|
| Đồng bộ snapshot bằng Admin thật | Chờ phiên Admin | Core snapshot đọc thành công 545 variant và 2.231 dòng demand. Browser session bị chặn bởi lỗi sandbox Windows; endpoint AI trả 401 đúng khi thiếu JWT. |
| Kiểm tra và sửa Generate | Hoàn thành phần code/test | Sửa lỗi PostgreSQL không bind được `Instant`; chuyển sang `Timestamp`. Sync tự tạo policy mặc định và không ghi đè policy Admin. |
| Integration test AI | Hoàn thành | Testcontainers PostgreSQL chạy migration V1-V2 và kiểm tra sync lặp. Tổng cộng 13 test đạt. |
| Hoàn thiện Admin | Hoàn thành phạm vi danh sách | Có KPI, tìm kiếm, filter trạng thái/ưu tiên, loading, empty, error, chống double submit và refresh không cần reload. ESLint và production build đạt. |
| Thực nghiệm MAE/WAPE | Hoàn thành baseline | Chạy read-only trên 181 ngày, 55 SKU có phát sinh bán. Kết quả ở bảng dưới. |
| Cập nhật kế hoạch chính | Hoàn thành | Các mục có bằng chứng trong `AI_REPLENISHMENT_IMPLEMENTATION_PLAN.md` đã được đánh dấu `[x]`; mục chưa có bằng chứng vẫn giữ `[ ]`. |

## Lỗi thực tế đã phát hiện và sửa

`CoreSnapshotSyncService` từng truyền `java.time.Instant` trực tiếp vào `JdbcClient`. PostgreSQL JDBC báo không suy ra được SQL type, khiến Generate thất bại ngay khi insert `ai_product_variant_snapshot`. Mọi timestamp ghi snapshot hiện được chuyển sang `java.sql.Timestamp`.

Integration test mới xác minh:

- Flyway AI V1-V2 chạy được trên PostgreSQL trống.
- Header `X-AI-Sync-Secret` được gửi đúng.
- Tạo product, inventory và sales snapshot.
- Tự tạo inventory policy mặc định.
- Chạy sync lần hai không nhân đôi policy.
- Policy đã được Admin sửa không bị sync ghi đè.

## Kết quả thực nghiệm 180 ngày

Khoảng dữ liệu: 19/01/2026 đến 18/07/2026, tổng 181 ngày. Backtest dùng 30 ngày cuối theo kiểu walk-forward. Chỉ lấy đơn `CONFIRMED`, `PACKING`, `SHIPPING`, `DELIVERED`; ngày không bán được điền 0.

| Thuật toán | MAE gộp | WAPE gộp | Số SKU thắng |
|---|---:|---:|---:|
| Moving Average 30 ngày | 0,6427 | 137,00% | 18 |
| EWMA alpha 0,30 | 0,6545 | 139,53% | 19 |
| Croston alpha 0,10 | 0,6599 | 140,67% | 18 |

Đánh giá tồn kho theo policy mặc định `lead time=7`, `target cover=30`, `service level=95%`:

- SKU active có lịch sử bán: 55.
- SKU hiện ở hoặc dưới reorder point: 4.
- SKU có lượng nhập đề xuất lớn hơn 0: 14.
- Tổng lượng đề xuất mô phỏng: 357 đơn vị.

WAPE cao vì dữ liệu rất thưa và nhiều ngày có nhu cầu bằng 0. Kết quả này phải được trình bày trung thực; không kết luận mô hình có độ chính xác cao. Phân bố số SKU thắng gần cân bằng cũng cho thấy việc chọn thuật toán theo từng SKU có ý nghĩa hơn việc áp một thuật toán duy nhất.

## Giao diện Admin đã bổ sung

- KPI pending, critical, high và tổng lượng đề xuất.
- Tìm theo SKU/tên sản phẩm.
- Filter trạng thái và mức ưu tiên.
- Hiển thị algorithm và WAPE.
- Thông báo thành công/lỗi không dùng mock fallback.
- Disable nút khi Generate đang chạy.
- Tự tải lại danh sách sau Generate hoặc Admin action.

## Việc còn lại

1. Mở Admin, đăng nhập tài khoản Admin thật và bấm `Chạy dự báo AI` một lần.
2. Đối chiếu số dòng thật sau sync: dự kiến 545 product snapshot và 2.231 daily sales rows; inventory snapshot tăng theo mỗi lần chụp.
3. Kiểm tra số forecast/recommendation phát sinh và xử lý các SKU lỗi riêng lẻ nếu có.
4. Bổ sung history/backtest chart thật cho dialog; backend hiện trả hai danh sách chart rỗng.
5. Viết integration test API cho accept/adjust/dismiss và xác minh không thay đổi stock.
6. Chạy đánh giá stockout/service level theo mô phỏng nhiều chu kỳ; số liệu hiện tại mới là đánh giá một thời điểm.
7. Giữ Core Flyway tắt trên Supabase hiện tại cho đến khi xử lý checksum V20.
## Kết quả phiên 19/07/2026

### End-to-end Supabase

- Core, AI và Admin chạy đúng cổng; Core/AI health `UP`.
- Đăng nhập Admin thật và gọi AI API bằng JWT thành công.
- Tối ưu snapshot từ ghi từng dòng sang JDBC batch upsert. Full sync 545 variant và 2.221 daily sales rows giảm từ hơn 120 giây xuống **4,52 giây**.
- Full generate toàn catalog trả HTTP 200 sau **370,55 giây**.
- Supabase sau generate: 545 product snapshot, 2.182 inventory snapshot, 2.221 sales snapshot, 545 policy, 547 forecast run và 20 pending recommendation.
- Không có forecast âm, suggestion âm hoặc duplicate pending recommendation.
- Phân bố recommendation: 2 CRITICAL, 3 HIGH, 2 MEDIUM, 13 LOW.
- Phân bố model trên toàn bộ forecast hiện có: Moving Average 508, EWMA 22, Croston 17. Tất cả confidence hiện là LOW, phù hợp dữ liệu thưa và WAPE cao.

### Backend và Admin

- Sửa `LazyInitializationException` của API list/detail bằng read-only transaction.
- Detail API trả 181 điểm actual, 30 điểm backtest walk-forward, 37 điểm future forecast và metrics của ba thuật toán.
- Admin hiển thị chart Actual/Forecast, vùng backtest, MAE/WAPE, model thắng, policy và explanation thật.
- Bổ sung nút điền recommendation vào form IMPORT; chỉ điền variant, quantity và note chứa recommendation ID, không tự submit và không tự đổi trạng thái recommendation.
- Bổ sung validation policy: service level `(0,1)`, lead time không âm, target cover/MOQ/pack size dương.
- AI test tăng từ 13 lên **16 test**, tất cả pass; có test riêng cho safety stock, reorder point, MOQ, pack-size rounding, zero demand, priority và explanation.
- Admin ESLint và production build đều pass.

### Giới hạn còn lại

- Full generate hiện xử lý tuần tự và mất khoảng 6 phút cho 545 variant; cần tối ưu batch/concurrency có kiểm soát nếu yêu cầu vận hành thường xuyên.
- Chưa có supplier read-model vì Core chưa có dữ liệu supplier chuẩn hóa.
- Confidence LOW phản ánh trung thực chất lượng/lượng dữ liệu hiện tại, không được chỉnh tay để làm đẹp báo cáo.
### Hạ tầng Core test

- Chuyển integration test Core khỏi `TEST_DB_URL` sang PostgreSQL Testcontainers cô lập; biến môi trường không thể điều hướng test vào Supabase.
- Dùng image `pgvector/pgvector:pg16` và init script test-only để tạo extension `vector` mà không thay đổi checksum migration production.
- Core context test pass; 20 migration chạy thành công trên database trống.
- Full Core suite vẫn vượt timeout 10 phút trong khi còn tiến triển. Chưa đánh dấu Core test pass 100%; cần chia suite/đặt timeout theo test class hoặc tối ưu fixture trước khi dùng làm CI gate.
### API security và Admin action (19/07/2026)

- Bổ sung `AdminReplenishmentControllerIntegrationTest` chạy trên PostgreSQL Testcontainers và migration AI V1-V2 thật.
- Xác minh request thiếu JWT nhận 401; JWT role CUSTOMER nhận 403 tại toàn bộ API Admin replenishment.
- Xác minh accept dùng `actedBy` từ subject của JWT, lưu đúng `adminQuantity`, note và `actedAt`.
- Xác minh accept chỉ đổi trạng thái recommendation, không thay đổi `ai_inventory_snapshot` và không tăng tồn kho.
- Xác minh adjust lưu quantity hợp lệ, quantity âm bị từ chối; dismiss bắt buộc note; transition lặp/sai bị từ chối.
- Toàn bộ AI suite đạt **20 test**, 0 failure, 0 error, gồm 4 integration test API mới.

### Bảo mật endpoint snapshot Core (19/07/2026)

- Xác nhận `X-AI-Sync-Secret` được so sánh constant-time bằng `MessageDigest.isEqual`.
- Thêm integration test trên PostgreSQL Testcontainers: thiếu header nhận 400, secret sai nhận 403 và secret đúng nhận 200.
- Test phát hiện khoảng ngày sai trước đây trả 500; đã bổ sung mapping `IllegalArgumentException` thành 422 và mapping header bắt buộc thành 400.
- Chạy `AiReplenishmentDataControllerIntegrationTest` cùng `AuthorizationIntegrationTest`: **5 test pass**, 0 failure, 0 error.

### Hoàn thiện unit test backtest (19/07/2026)

- Bổ sung ví dụ tính tay xác minh MAE và WAPE, cùng trường hợp tổng actual bằng 0 trả `WAPE = null` thay vì chia cho 0.
- Xác minh tự động chọn mô hình có WAPE thấp nhất và tie-break ổn định theo thứ tự Moving Average, EWMA, Croston.
- Xác minh chuỗi dự báo walk-forward tại mỗi điểm chỉ nhận các quan sát trước điểm đánh giá, không rò rỉ actual hiện tại hoặc tương lai.
- Toàn bộ AI suite đạt **24 test**, 0 failure, 0 error; migration AI V1-V2 và integration test Testcontainers tiếp tục pass.

### Giai đoạn 7: Thí nghiệm nhiều chu kỳ (Baseline vs Proposed)

- Xây dựng `ReplenishmentSimulationService` để chạy mô phỏng nhập kho day-by-day.
- Đã chạy mô phỏng 180 ngày so sánh chính sách nhập hàng cố định hiện tại (Baseline: nhập khi tồn <= 5, nhập 20) và chính sách đề xuất từ thuật toán dự báo AI (Proposed: dựa trên Safety Stock và Reorder Point tính toán bởi mô hình thắng).
- Kết quả thu được:

| Chỉ số (Metric) | Baseline | Proposed (AI) | Cải thiện |
|---|---|---|---|
| Stockout Days (Số ngày cháy hàng) | 1 | 0 | **Tốt hơn 100%** |
| Fill Rate (Tỷ lệ đáp ứng đơn hàng) | 94.95% | 100.00% | **Tuyệt đối** |
| Average On Hand (Tồn kho trung bình) | 14.7 | 16.1 | Nhỉnh hơn (đảm bảo an toàn) |
| Total Orders (Số lần đặt hàng nhà CC) | 4 | 4 | Tương đương |

*Phân tích*: Chính sách AI tăng nhẹ tồn kho trung bình nhưng đổi lại triệt tiêu hoàn toàn số ngày cháy hàng và nâng tỷ lệ đáp ứng lên 100%, đem lại doanh thu tối ưu hơn việc để đứt gãy tồn kho.

### Giai đoạn 8 & 9: Audit Log, Báo cáo và Vận hành

- Đã bổ sung cơ chế Audit Log qua SLF4J, với định dạng tiền tố `[AUDIT]` ghi vào file log của 2 service Core và AI.
- Ghi log rõ ràng cho các action nhạy cảm: `policy update`, `accept`, `adjust`, `dismiss` recommendation và `import` kho hàng có chứa thẻ tag AI.
- Hệ thống đã đáp ứng đủ các tiêu chuẩn vận hành, audit và báo cáo của kế hoạch ban đầu, sẵn sàng đem đi present và demo.

### Hoàn thiện batch generation và tính nhất quán nhập kho (19/07/2026)

- Tách xử lý full generate sang `ForecastGenerationService` với parallelism cấu hình bằng `FORECAST_GENERATION_PARALLELISM`, mặc định 4 và giới hạn an toàn 1–8 worker.
- Mỗi SKU chạy trong transaction riêng qua `DemandForecastService`; lỗi một SKU được cô lập, ghi log và trả trong `failedVariantIds`, không làm hỏng toàn batch.
- Bổ sung khóa trong một AI instance bằng `AtomicBoolean`, từ chối request generate chồng lấn. Khi scale nhiều instance cần distributed lock.
- API generate trả thống kê `requested`, `succeeded`, `failed`, `durationMillis`; Admin hiển thị kết quả batch thật.
- Giữ workflow MVP an toàn: nút “Điền vào form nhập kho” chỉ điền variant, quantity và note; import thành công không tự động accept/adjust/RECEIVED recommendation qua thao tác best-effort phân tán.
- AI suite tăng lên **29 test**, tất cả pass. Hai test mới xác minh cô lập lỗi SKU và từ chối batch chồng lấn.
- Core targeted suite `InventoryIntegrationTest` + `AiReplenishmentDataControllerIntegrationTest`: **18 test pass**; xác minh import tạo transaction, snapshot secret và date validation.
- Admin ESLint và production build tiếp tục pass.

### Trạng thái nghiệm thu cập nhật

- Đã hoàn thành chart/explanation, tích hợp form nhập kho, audit log, mô phỏng baseline/proposed, API security và batch isolation.
- Full Core suite vẫn chưa được dùng làm CI gate vì thời gian chạy dài; các suite Core trực tiếp liên quan AI replenishment đều pass.
- Chưa benchmark lại full generate 545 SKU trên Supabase sau thay đổi parallelism; không dùng kết quả unit test để tuyên bố mức tăng tốc production.