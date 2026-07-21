# AI Forecasting Service

Spring Boot service độc lập cho dự báo nhu cầu và đề xuất nhập hàng của Admin.

## Phạm vi

Service sở hữu:

- Moving Average, EWMA và Croston.
- Walk-forward backtest, MAE, WAPE và confidence.
- Inventory policy, forecast run và replenishment recommendation.
- Read-model snapshot nhận từ Core qua API nội bộ.
- API Admin tại `/api/v1/admin/replenishment/**`.

Service không sở hữu Product, Order, User và không được tự thay đổi tồn kho. Accept, adjust hoặc dismiss chỉ lưu quyết định. Tồn kho thực chỉ thay đổi qua `InventoryService` của Core.

## Chạy local

Yêu cầu Java 21 và PostgreSQL.

```powershell
Copy-Item .env.example .env
.\run-local-with-env.ps1
```

Cổng mặc định:

- Core: `http://localhost:8082`
- AI: `http://localhost:8081`
- Admin: `http://localhost:3001`
- Storefront: `http://localhost:3000`

Health AI: `http://localhost:8081/actuator/health`.

## Biến môi trường

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_PARAMS`
- `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`
- `CORE_API_BASE_URL`
- `AI_SYNC_SECRET` — phải giống Core
- `CORS_ALLOWED_ORIGINS`
- `SERVER_PORT` — mặc định `8081`
- `FORECAST_GENERATION_PARALLELISM` — số SKU xử lý đồng thời, giới hạn 1–8, mặc định `4`
- `SPRING_FLYWAY_ENABLED`

Không commit `.env` hoặc secret thật. Với Supabase/shared database đã tồn tại, giữ `SPRING_FLYWAY_ENABLED=false` đến khi lịch sử migration được đối chiếu.

## Generate forecast

`POST /api/v1/admin/replenishment/generate` thực hiện:

1. Đồng bộ snapshot Core.
2. Chạy forecast theo SKU với số worker có giới hạn.
3. Cô lập lỗi từng SKU để một SKU lỗi không hủy toàn batch.
4. Từ chối batch thứ hai nếu một batch đang chạy trong cùng instance.
5. Trả `requested`, `succeeded`, `failed`, `durationMillis` và `failedVariantIds`.

Khóa hiện tại là khóa trong một instance. Nếu scale nhiều AI instance, cần bổ sung distributed lock trên PostgreSQL/Redis.

## Database

- Migration: `classpath:db/ai-migration`
- History table: `flyway_ai_schema_history`
- AI-owned tables: `inventory_policies`, `forecast_runs`, `replenishment_recommendations`
- Read-model: `ai_product_variant_snapshot`, `ai_inventory_snapshot`, `ai_sales_daily_snapshot`, `ai_supplier_snapshot`

Hệ thống hiện vận hành ở shared-database mode trên cùng Supabase project, nhưng giữ ranh giới ownership bằng code và read-model. Xem `../AI_DATABASE_CUTOVER_RUNBOOK.md` trước khi tách database.

## Kiểm thử

```powershell
.\mvnw.cmd test
```

Kết quả xác minh ngày 19/07/2026: 29 test pass, gồm algorithm, backtest, recommendation, simulation, snapshot, security/admin action và batch generation.