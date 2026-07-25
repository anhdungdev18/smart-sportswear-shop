# CÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh `.env` cho Synthetic Data vÃƒÆ’Ã‚Â  Admin AI

TÃƒÆ’Ã‚Â i liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nÃƒÆ’Ã‚Â y lÃƒÆ’Ã‚Â  phÃƒÂ¡Ã‚ÂºÃ‚Â§n bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung cÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh cho
`ADMIN_AI_IMPLEMENTATION_ROADMAP.md`. ThÃƒÂ¡Ã‚Â»Ã‚Â±c hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n checklist P0 trong tÃƒÆ’Ã‚Â i liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nÃƒÆ’Ã‚Â y
trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc Phase 0 cÃƒÂ¡Ã‚Â»Ã‚Â§a roadmap.
## P0 progress notes

- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc vÃƒÆ’Ã‚Â  Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi chiÃƒÂ¡Ã‚ÂºÃ‚Â¿u `ADMIN_AI_START_HERE.md`, `ADMIN_AI_ENV_CONFIGURATION.md`, `ADMIN_AI_IMPLEMENTATION_ROADMAP.md`.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i cÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh forecast demo sang `FORECAST_DEMO_*` trong `backend/.env.example`, `backend/src/main/resources/application.yml`, `docker-compose.yml`, vÃƒÆ’Ã‚Â  `FORECAST_DEMO_DATA_GUIDE.md`.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung `FORECAST_DEMO_ANCHOR_DATE`, `FORECAST_DEMO_MARKER`, `FORECAST_DEMO_CLEANUP` vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi dataset V2: seed `20260725`, anchor `2026-07-24`, 180 ngÃƒÆ’Ã‚Â y, 12.000 order, 120 variant, marker `[FORECAST_DEMO_V2]`.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ sÃƒÂ¡Ã‚Â»Ã‚Â­a `ai_forecasting_service/src/main/resources/application.yml` Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ Flyway Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc `SPRING_FLYWAY_ENABLED`.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ giÃƒÂ¡Ã‚Â»Ã‚Â¯ quy Ãƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc rÃƒÆ’Ã‚Âµ rÃƒÆ’Ã‚Â ng: script local dÃƒÆ’Ã‚Â¹ng `.env` cho Supabase/shared DB; Docker Compose dÃƒÆ’Ã‚Â¹ng PostgreSQL local vÃƒÆ’Ã‚Â  khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â²n bÃƒÂ¡Ã‚ÂºÃ‚Â­t seed mÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ `.env` thÃƒÂ¡Ã‚ÂºÃ‚Â­t: JWT access, JWT refresh vÃƒÆ’Ã‚Â  `AI_SYNC_SECRET` khÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºp giÃƒÂ¡Ã‚Â»Ã‚Â¯a backend vÃƒÆ’Ã‚Â  AI service; `CORE_API_BASE_URL` khÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºp backend `SERVER_PORT=8082`.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung biÃƒÂ¡Ã‚ÂºÃ‚Â¿n Phase 6 vÃƒÆ’Ã‚Â o `frontend/admin/.env.example` vÃƒÆ’Ã‚Â  xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ secret trong biÃƒÂ¡Ã‚ÂºÃ‚Â¿n `NEXT_PUBLIC_*`.
- [x] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n `.env` thÃƒÂ¡Ã‚ÂºÃ‚Â­t Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Git ignore: `backend/.env`, `ai_forecasting_service/.env`, `chatbot-admin-service/.env`, `frontend/admin/.env.local`, `frontend/storefront/.env.local`.
- [ ] ChÃƒâ€ Ã‚Â°a chÃƒÂ¡Ã‚ÂºÃ‚Â¡y seed thÃƒÂ¡Ã‚ÂºÃ‚Â­t. BÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc nÃƒÆ’Ã‚Â y cÃƒÂ¡Ã‚ÂºÃ‚Â§n xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n database lÃƒÆ’Ã‚Â  demo/development vÃƒÆ’Ã‚Â  cÃƒÆ’Ã‚Â³ backup trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi bÃƒÂ¡Ã‚ÂºÃ‚Â­t `APP_SEED_ENABLED=true` + `FORECAST_DEMO_ENABLED=true`.

## 1. CÃƒÆ’Ã‚Â¡ch tÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ chÃƒÂ¡Ã‚Â»Ã‚Â©c

MÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i service dÃƒÆ’Ã‚Â¹ng mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t file mÃƒÆ’Ã‚Â´i trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âng riÃƒÆ’Ã‚Âªng:

```text
backend/.env
ai_forecasting_service/.env
chatbot-service/.env
chatbot-admin-service/.env       # tÃƒÂ¡Ã‚ÂºÃ‚Â¡o ÃƒÂ¡Ã‚Â»Ã…Â¸ Phase 5
frontend/admin/.env.local
frontend/storefront/.env.local
```

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° commit `.env.example`. KhÃƒÆ’Ã‚Â´ng commit `.env`, password, JWT secret hoÃƒÂ¡Ã‚ÂºÃ‚Â·c API
key. TrÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi commit:

```powershell
git status --short
git check-ignore backend/.env
git check-ignore ai_forecasting_service/.env
git check-ignore chatbot-admin-service/.env
```

## 2. Ba lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i cÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh phÃƒÂ¡Ã‚ÂºÃ‚Â£i sÃƒÂ¡Ã‚Â»Ã‚Â­a

### 2.1 TÃƒÆ’Ã‚Âªn biÃƒÂ¡Ã‚ÂºÃ‚Â¿n forecast demo khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t

`backend/src/main/resources/application.yml` Ãƒâ€žÃ¢â‚¬Ëœang Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc:

```dotenv
FORECAST_DEMO_ENABLED
FORECAST_DEMO_RANDOM_SEED
FORECAST_DEMO_ANCHOR_DATE
FORECAST_DEMO_HISTORY_DAYS
FORECAST_DEMO_ORDER_COUNT
FORECAST_DEMO_VARIANT_COUNT
FORECAST_DEMO_MARKER
FORECAST_DEMO_CLEANUP
```

TrÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc P0, `backend/.env.example` dÃƒÆ’Ã‚Â¹ng `APP_FORECAST_DEMO_*`. Prefix `APP_`
khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c cÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc, nÃƒÆ’Ã‚Âªn Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i sang `FORECAST_DEMO_*`.

QuyÃƒÂ¡Ã‚ÂºÃ‚Â¿t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh: giÃƒÂ¡Ã‚Â»Ã‚Â¯ `FORECAST_DEMO_*` vÃƒÆ’Ã‚Â  sÃƒÂ¡Ã‚Â»Ã‚Â­a Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢:

```text
backend/.env.example
FORECAST_DEMO_DATA_GUIDE.md
backend/src/main/resources/application.yml
docker-compose.yml
```

### 2.2 Flyway forecasting Ãƒâ€žÃ¢â‚¬Ëœang hardcode

`ai_forecasting_service/.env.example` cÃƒÆ’Ã‚Â³:

```dotenv
SPRING_FLYWAY_ENABLED=false
```

trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc P0 `ai_forecasting_service/src/main/resources/application.yml` Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t hardcode:

```yaml
spring:
  flyway:
    enabled: true
```

Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ sÃƒÂ¡Ã‚Â»Ã‚Â­a thÃƒÆ’Ã‚Â nh:

```yaml
spring:
  flyway:
    enabled: ${SPRING_FLYWAY_ENABLED:true}
```

### 2.3 Docker Compose Ãƒâ€žÃ¢â‚¬Ëœang ghi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¨ `.env`

`backend` cÃƒÆ’Ã‚Â³ `env_file`, nhÃƒâ€ Ã‚Â°ng `environment` lÃƒÂ¡Ã‚ÂºÃ‚Â¡i Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t `DB_HOST=postgres` vÃƒÆ’Ã‚Â 
credential local. `environment` cÃƒÆ’Ã‚Â³ Ãƒâ€ Ã‚Â°u tiÃƒÆ’Ã‚Âªn cao hÃƒâ€ Ã‚Â¡n `env_file`.

Quy Ãƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc:

- `start-backend.ps1`, `start-ai-service.ps1`: dÃƒÆ’Ã‚Â¹ng Supabase trong `.env`.
- Docker Compose hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i: dÃƒÆ’Ã‚Â¹ng PostgreSQL local.
- NÃƒÂ¡Ã‚ÂºÃ‚Â¿u Docker cÃƒÂ¡Ã‚ÂºÃ‚Â§n Supabase, tÃƒÂ¡Ã‚ÂºÃ‚Â¡o `docker-compose.supabase.yml` riÃƒÆ’Ã‚Âªng.

KhÃƒÆ’Ã‚Â´ng trÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢n hai chÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ trÃƒÆ’Ã‚Â¡nh seed nhÃƒÂ¡Ã‚ÂºÃ‚Â§m database.

## 3. `backend/.env` Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ seed 180 ngÃƒÆ’Ã‚Â y

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° sÃƒÂ¡Ã‚Â»Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥ng trÃƒÆ’Ã‚Âªn database demo/development:

```dotenv
DB_HOST=your-supabase-host
DB_PORT=5432
DB_NAME=postgres
DB_USERNAME=your-supabase-user
DB_PASSWORD=replace-with-local-secret
DB_PARAMS=?sslmode=require

SERVER_PORT=8082
SPRING_FLYWAY_ENABLED=false

APP_SEED_ENABLED=true
APP_SEED_DEMO_PASSWORD=replace-with-local-demo-password

FORECAST_DEMO_ENABLED=true
FORECAST_DEMO_RANDOM_SEED=20260725
FORECAST_DEMO_ANCHOR_DATE=2026-07-24
FORECAST_DEMO_HISTORY_DAYS=180
FORECAST_DEMO_ORDER_COUNT=12000
FORECAST_DEMO_VARIANT_COUNT=120
FORECAST_DEMO_MARKER=[FORECAST_DEMO_V2]
FORECAST_DEMO_CLEANUP=true

JWT_ACCESS_SECRET=replace-with-shared-access-secret
JWT_REFRESH_SECRET=replace-with-shared-refresh-secret
AI_SYNC_SECRET=replace-with-shared-ai-sync-secret
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

Sau khi seed thÃƒÆ’Ã‚Â nh cÃƒÆ’Ã‚Â´ng mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t lÃƒÂ¡Ã‚ÂºÃ‚Â§n, Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i ngay:

```dotenv
APP_SEED_ENABLED=false
FORECAST_DEMO_ENABLED=false
```

NÃƒÂ¡Ã‚ÂºÃ‚Â¿u khÃƒÆ’Ã‚Â´ng tÃƒÂ¡Ã‚ÂºÃ‚Â¯t, `FORECAST_DEMO_CLEANUP=true` khiÃƒÂ¡Ã‚ÂºÃ‚Â¿n mÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i lÃƒÂ¡Ã‚ÂºÃ‚Â§n backend khÃƒÂ¡Ã‚Â»Ã…Â¸i Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng
Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âu xÃƒÆ’Ã‚Â³a vÃƒÆ’Ã‚Â  sinh lÃƒÂ¡Ã‚ÂºÃ‚Â¡i order demo cÃƒÆ’Ã‚Â³ marker tÃƒâ€ Ã‚Â°Ãƒâ€ Ã‚Â¡ng ÃƒÂ¡Ã‚Â»Ã‚Â©ng.

## 4. BiÃƒÂ¡Ã‚ÂºÃ‚Â¿n cÃƒÂ¡Ã‚ÂºÃ‚Â§n bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung cho Synthetic Seeder V2

CÃƒÆ’Ã‚Â¡c biÃƒÂ¡Ã‚ÂºÃ‚Â¿n sau chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c code hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i hÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ trÃƒÂ¡Ã‚Â»Ã‚Â£. ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° thÃƒÆ’Ã‚Âªm vÃƒÆ’Ã‚Â o `.env.example` sau
khi bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung field vÃƒÆ’Ã‚Â o `AppForecastDemoProperties` vÃƒÆ’Ã‚Â  logic trong
`ForecastDemoDataSeeder`.

```dotenv
SYNTHETIC_DATA_ALLOWED=false
SYNTHETIC_DATA_ENVIRONMENT=demo

FORECAST_DEMO_SMOOTH_VARIANTS=20
FORECAST_DEMO_NORMAL_VARIANTS=20
FORECAST_DEMO_SLOW_VARIANTS=15
FORECAST_DEMO_INTERMITTENT_VARIANTS=20
FORECAST_DEMO_ERRATIC_VARIANTS=15
FORECAST_DEMO_GROWING_VARIANTS=10
FORECAST_DEMO_DECLINING_VARIANTS=10
FORECAST_DEMO_NEW_ITEM_VARIANTS=5
FORECAST_DEMO_NO_DEMAND_VARIANTS=5

FORECAST_DEMO_WEEKEND_MULTIPLIER=1.30
FORECAST_DEMO_PROMOTION_MULTIPLIER=3.00
FORECAST_DEMO_PROMOTION_DAY_OF_MONTH=15
FORECAST_DEMO_NEW_ITEM_HISTORY_DAYS=30
FORECAST_DEMO_CANCEL_RATE=0.05

FORECAST_DEMO_INVENTORY_HISTORY_ENABLED=true
FORECAST_DEMO_INVENTORY_HISTORY_DAYS=180
FORECAST_DEMO_SUPPLIER_COUNT=3
FORECAST_DEMO_MIN_LEAD_TIME_DAYS=7
FORECAST_DEMO_MAX_LEAD_TIME_DAYS=30
FORECAST_DEMO_DEFAULT_SERVICE_LEVEL=0.95

FORECAST_DEMO_GROUND_TRUTH_ENABLED=true
FORECAST_DEMO_SCENARIO_VERSION=synthetic-v2
```

Validation bÃƒÂ¡Ã‚ÂºÃ‚Â¯t buÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c:

- TÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng profile count bÃƒÂ¡Ã‚ÂºÃ‚Â±ng `FORECAST_DEMO_VARIANT_COUNT`.
- Rate nÃƒÂ¡Ã‚ÂºÃ‚Â±m trong `[0,1]`; multiplier lÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºn hÃƒâ€ Ã‚Â¡n 0.
- Min lead time khÃƒÆ’Ã‚Â´ng lÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºn hÃƒâ€ Ã‚Â¡n max lead time.
- `SYNTHETIC_DATA_ALLOWED=true`.
- Environment lÃƒÆ’Ã‚Â  `demo` hoÃƒÂ¡Ã‚ÂºÃ‚Â·c `development`.
- Profile production luÃƒÆ’Ã‚Â´n tÃƒÂ¡Ã‚Â»Ã‚Â« chÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi chÃƒÂ¡Ã‚ÂºÃ‚Â¡y.
- Marker khÃƒÆ’Ã‚Â´ng rÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ng.
- Cleanup chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° xÃƒÆ’Ã‚Â³a record cÃƒÆ’Ã‚Â³ marker chÃƒÆ’Ã‚Â­nh xÃƒÆ’Ã‚Â¡c.

## 5. `ai_forecasting_service/.env`

```dotenv
DB_HOST=your-supabase-host
DB_PORT=5432
DB_NAME=postgres
DB_USERNAME=your-supabase-user
DB_PASSWORD=replace-with-local-secret
DB_PARAMS=?sslmode=require

SERVER_PORT=8081
SPRING_FLYWAY_ENABLED=false
FORECAST_GENERATION_PARALLELISM=4

CORE_API_BASE_URL=http://localhost:8082
AI_SYNC_SECRET=replace-with-exact-same-value-as-backend
JWT_ACCESS_SECRET=replace-with-exact-same-value-as-backend
JWT_REFRESH_SECRET=replace-with-exact-same-value-as-backend
JWT_ACCESS_TTL_MINUTES=15
JWT_REFRESH_TTL_DAYS=30

CORS_ALLOWED_ORIGINS=http://localhost:3001
LOG_LEVEL=INFO
```

Ba giÃƒÆ’Ã‚Â¡ trÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ phÃƒÂ¡Ã‚ÂºÃ‚Â£i giÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng backend:

```text
JWT_ACCESS_SECRET
JWT_REFRESH_SECRET
AI_SYNC_SECRET
```

`CORE_API_BASE_URL` phÃƒÂ¡Ã‚ÂºÃ‚Â£i trÃƒÂ¡Ã‚Â»Ã‚Â Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng `SERVER_PORT` backend.

## 6. `frontend/admin/.env.local`

```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8082
SERVER_API_BASE_URL=http://localhost:8082

NEXT_PUBLIC_AI_API_BASE_URL=http://localhost:8081
SERVER_AI_API_BASE_URL=http://localhost:8081

# Phase 6
NEXT_PUBLIC_ADMIN_COPILOT_API_BASE_URL=http://localhost:8003
SERVER_ADMIN_COPILOT_API_BASE_URL=http://localhost:8003
```

Next.js tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc `.env.local`. KhÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t secret trong biÃƒÂ¡Ã‚ÂºÃ‚Â¿n `NEXT_PUBLIC_*`.

## 7. `.env.example` cho `chatbot-admin-service`

TÃƒÂ¡Ã‚ÂºÃ‚Â¡o ÃƒÂ¡Ã‚Â»Ã…Â¸ Phase 5:

```dotenv
ADMIN_COPILOT_ENV=development
ADMIN_COPILOT_HOST=0.0.0.0
ADMIN_COPILOT_PORT=8003

CORE_BACKEND_API_BASE_URL=http://localhost:8082
FORECASTING_API_BASE_URL=http://localhost:8081

JWT_ACCESS_SECRET=replace-with-exact-same-value-as-backend
JWT_ACCESS_ALGORITHM=HS256

REDIS_URL=redis://localhost:6379
SESSION_TTL_SECONDS=3600

MODEL_PROVIDER=anthropic
MODEL_NAME=replace-with-supported-model
ANTHROPIC_API_KEY=
OPENAI_API_KEY=

MAX_AGENT_STEPS=4
MAX_TOOL_CALLS_PER_RUN=6
AGENT_TIMEOUT_SECONDS=30
TOOL_TIMEOUT_SECONDS=8
MAX_INPUT_CHARS=4000

READ_ONLY_MODE=true
WRITE_TOOLS_ENABLED=false
APPROVALS_ENABLED=false

OBSERVABILITY_ENABLED=true
EVALUATION_LOGGING_ENABLED=true
LOG_LEVEL=INFO
CORS_ALLOWED_ORIGINS=http://localhost:3001
```

Admin Copilot khÃƒÆ’Ã‚Â´ng chÃƒÂ¡Ã‚ÂºÃ‚Â¡y SQL trÃƒÂ¡Ã‚Â»Ã‚Â±c tiÃƒÂ¡Ã‚ÂºÃ‚Â¿p. NÃƒÂ¡Ã‚ÂºÃ‚Â¿u cÃƒÂ¡Ã‚ÂºÃ‚Â§n lÃƒâ€ Ã‚Â°u run/feedback, sÃƒÂ¡Ã‚Â»Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥ng mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t
DB role riÃƒÆ’Ã‚Âªng chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° cÃƒÆ’Ã‚Â³ quyÃƒÂ¡Ã‚Â»Ã‚Ân trÃƒÆ’Ã‚Âªn bÃƒÂ¡Ã‚ÂºÃ‚Â£ng `admin_agent_*`.

## 8. Threshold AI nÃƒÆ’Ã‚Âªn Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°a vÃƒÆ’Ã‚Â o `.env`

```dotenv
AI_DATA_QUALITY_MIN_HISTORY_DAYS=60
AI_DATA_QUALITY_HIGH_HISTORY_DAYS=120
AI_DATA_QUALITY_MIN_NONZERO_DAYS=12
AI_DATA_QUALITY_HIGH_NONZERO_DAYS=30

AI_FORECAST_MAX_WAPE_HIGH_CONFIDENCE=0.30
AI_FORECAST_MAX_WAPE_MEDIUM_CONFIDENCE=0.60
AI_FORECAST_MIN_BACKTEST_WINDOWS=3

AI_REPLENISHMENT_ALLOW_LOW_CONFIDENCE=false
AI_REPLENISHMENT_REQUIRE_SUPPLIER=true
AI_REPLENISHMENT_REQUIRE_REAL_POLICY=true
```

Bind cÃƒÆ’Ã‚Â¡c biÃƒÂ¡Ã‚ÂºÃ‚Â¿n vÃƒÆ’Ã‚Â o typed configuration properties vÃƒÆ’Ã‚Â  validate khi startup,
khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc `System.getenv()` rÃƒÂ¡Ã‚ÂºÃ‚Â£i rÃƒÆ’Ã‚Â¡c.

## 9. Quy trÃƒÆ’Ã‚Â¬nh chÃƒÂ¡Ã‚ÂºÃ‚Â¡y

### BÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc 1 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n database

- [ ] Database lÃƒÆ’Ã‚Â  demo/development.
- [ ] CÃƒÆ’Ã‚Â³ backup trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi seed.
- [ ] Marker khÃƒÆ’Ã‚Â´ng trÃƒÆ’Ã‚Â¹ng dataset khÃƒÆ’Ã‚Â¡c.
- [ ] Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¿m dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u demo hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i.

### BÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc 2 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â seed mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t lÃƒÂ¡Ã‚ÂºÃ‚Â§n

```dotenv
APP_SEED_ENABLED=true
FORECAST_DEMO_ENABLED=true
```

```powershell
./start-backend.ps1
```

ChÃƒÂ¡Ã‚Â»Ã‚Â log xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ order vÃƒÆ’Ã‚Â  order item.

### BÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc 3 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â tÃƒÂ¡Ã‚ÂºÃ‚Â¯t seed

```dotenv
APP_SEED_ENABLED=false
FORECAST_DEMO_ENABLED=false
```

Restart backend vÃƒÆ’Ã‚Â  xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ order khÃƒÆ’Ã‚Â´ng thay Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i.

### BÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc 4 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â chÃƒÂ¡Ã‚ÂºÃ‚Â¡y forecasting

```powershell
./start-ai-service.ps1
```

GÃƒÂ¡Ã‚Â»Ã‚Âi API theo thÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â±:

```text
POST /api/v1/admin/replenishment/snapshots/sync
POST /api/v1/admin/replenishment/evaluate
GET  /api/v1/admin/replenishment/generate/status
POST /api/v1/admin/replenishment/generate
GET  /api/v1/admin/replenishment/generate/status
GET  /api/v1/admin/replenishment/suggestions
```

## 10. Dataset manifest

LÃƒâ€ Ã‚Â°u metadata tÃƒÆ’Ã‚Â¡i lÃƒÂ¡Ã‚ÂºÃ‚Â­p dataset, khÃƒÆ’Ã‚Â´ng lÃƒâ€ Ã‚Â°u secret:

```json
{
  "scenarioVersion": "synthetic-v2",
  "randomSeed": 20260725,
  "anchorDate": "2026-07-24",
  "historyDays": 180,
  "orderCount": 12000,
  "variantCount": 120,
  "marker": "[FORECAST_DEMO_V2]"
}
```

## 11. Checklist P0

- [x] SÃƒÂ¡Ã‚Â»Ã‚Â­a `APP_FORECAST_DEMO_*` thÃƒÆ’Ã‚Â nh `FORECAST_DEMO_*`.
- [x] BÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ sung anchor date, marker, cleanup vÃƒÆ’Ã‚Â o `backend/.env.example`.
- [x] CÃƒÂ¡Ã‚ÂºÃ‚Â­p nhÃƒÂ¡Ã‚ÂºÃ‚Â­t `FORECAST_DEMO_DATA_GUIDE.md`.
- [x] SÃƒÂ¡Ã‚Â»Ã‚Â­a Flyway forecasting Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc `SPRING_FLYWAY_ENABLED`.
- [x] ChÃƒÂ¡Ã‚Â»Ã‚Ân rÃƒÆ’Ã‚Âµ local+Supabase hoÃƒÂ¡Ã‚ÂºÃ‚Â·c Docker+PostgreSQL local.
- [x] KhÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ Compose ghi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¨ database ngoÃƒÆ’Ã‚Â i ÃƒÆ’Ã‚Â½ muÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœn.
- [x] Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ JWT secret giÃƒÂ¡Ã‚Â»Ã‚Â¯a cÃƒÆ’Ã‚Â¡c service.
- [x] Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ `AI_SYNC_SECRET`.
- [x] Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ backend port vÃƒÆ’Ã‚Â  `CORE_API_BASE_URL`.
- [x] DÃƒÆ’Ã‚Â¹ng `.env.local` cho Next.js.
- [x] KhÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t secret trong `NEXT_PUBLIC_*`.
- [ ] Seed mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t lÃƒÂ¡Ã‚ÂºÃ‚Â§n rÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“i tÃƒÂ¡Ã‚ÂºÃ‚Â¯t cÃƒÂ¡Ã‚ÂºÃ‚Â£ hai flag. ChÃƒâ€ Ã‚Â°a chÃƒÂ¡Ã‚ÂºÃ‚Â¡y vÃƒÆ’Ã‚Â¬ cÃƒÂ¡Ã‚ÂºÃ‚Â§n xÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n database demo/development vÃƒÆ’Ã‚Â  backup trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc.
- [x] KiÃƒÂ¡Ã‚Â»Ã†â€™m tra `.env` Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Git ignore trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi commit.


## Phase -1 progress notes - 2026-07-25

- [x] `SYNTHETIC_DATA_ALLOWED` and `SYNTHETIC_DATA_ENVIRONMENT` are now bound by `AppSyntheticDataProperties` and checked before synthetic seed runs.
- [x] V2 forecast demo variables are now bound by `AppForecastDemoProperties` and configured in `backend/.env.example`, `backend/src/main/resources/application.yml`, and `docker-compose.yml`.
- [x] Added scenario manifest table `forecast_demo_scenarios` for ground truth, demand profiles, supplier, lead time, MOQ, pack size, service level, marker, seed, and anchor date.
- [x] `ForecastDemoDataSeeder` now supports deterministic Synthetic Seeder V2, exact-marker cleanup, cancelled-order exclusion from valid demand, no-demand SKUs, and diversified inventory policies.
- [x] Integration test passed: `.\mvnw.cmd -Dtest=ForecastDemoDataSeederIntegrationTest test` on 2026-07-25, 2 tests, build success.
- [ ] Real seed remains intentionally not run until database demo/development and backup are confirmed.
## Phase 0 progress notes - 2026-07-25

- [x] Added `AiDataQualityProperties` typed thresholds bound from `AI_DATA_QUALITY_*` variables.
- [x] Added read-only Admin Data Quality API under `/api/v1/admin/ai/data-quality` with summary, variant list, and variant detail endpoints.
- [x] Added `SkuDataQualityService` scoring for continuous sales history, non-zero demand days, inventory snapshot coverage, supplier configuration, and warnings.
- [x] Data-quality evaluation treats missing sales snapshot days as a blocking issue because zero-demand days must be materialized as quantity 0.
- [x] Added unit coverage for missing sales days, high-quality complete series, supplier gaps, and inventory history gaps.
- [x] Verification: `.\mvnw.cmd -Dtest=SkuDataQualityServiceTest test` passed with 3 tests on 2026-07-25.
- [ ] Data source DEMO/REAL/IMPORTED is not yet added to snapshot metadata.
- [ ] Real 180-day seed remains intentionally not run until DB demo/development and backup are confirmed.
