# Admin AI ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â BÃƒÂ¡Ã‚ÂºÃ‚Â¯t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u tÃƒÂ¡Ã‚Â»Ã‚Â« Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¢y

## Hai tÃƒÆ’Ã‚Â i liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u chÃƒÆ’Ã‚Â­nh

1. [`ADMIN_AI_ENV_CONFIGURATION.md`](./ADMIN_AI_ENV_CONFIGURATION.md)
2. [`ADMIN_AI_IMPLEMENTATION_ROADMAP.md`](./ADMIN_AI_IMPLEMENTATION_ROADMAP.md)

Hai tÃƒÆ’Ã‚Â i liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u nÃƒÆ’Ã‚Â y Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ triÃƒÂ¡Ã‚Â»Ã†â€™n khai Admin AI, nhÃƒâ€ Ã‚Â°ng phÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÂ¡Ã‚Â»Ã‚Â±c hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n theo thÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â±
trong file nÃƒÆ’Ã‚Â y.

## ThÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â± thÃƒÂ¡Ã‚Â»Ã‚Â±c hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n

```text
P0: sÃƒÂ¡Ã‚Â»Ã‚Â­a cÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh .env
  -> Phase -1: triÃƒÂ¡Ã‚Â»Ã†â€™n khai Synthetic Seeder V2
  -> seed dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u 180 ngÃƒÆ’Ã‚Â y
  -> xÃƒÆ’Ã‚Â¡c minh dataset
  -> snapshot sync
  -> model evaluation
  -> forecast generation
  -> Roadmap Phase 0: data quality
  -> Phase 1: demand classification
  -> Phase 2: forecast/backtest
  -> Phase 3: inventory decision engine
  -> Phase 4: admin intelligence UI
  -> Phase 5: chatbot-admin-service read-only
  -> Phase 6: Admin Copilot UI
  -> Phase 7: ReAct read-only
  -> Phase 8: approval-based write actions
```

## ViÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡c cÃƒÂ¡Ã‚ÂºÃ‚Â§n lÃƒÆ’Ã‚Â m Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u tiÃƒÆ’Ã‚Âªn

MÃƒÂ¡Ã‚Â»Ã…Â¸ `ADMIN_AI_ENV_CONFIGURATION.md` vÃƒÆ’Ã‚Â  hoÃƒÆ’Ã‚Â n tÃƒÂ¡Ã‚ÂºÃ‚Â¥t checklist P0:

- SÃƒÂ¡Ã‚Â»Ã‚Â­a `APP_FORECAST_DEMO_*` thÃƒÆ’Ã‚Â nh `FORECAST_DEMO_*`.
- Cho forecasting Flyway Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Âc `SPRING_FLYWAY_ENABLED`.
- ChÃƒÂ¡Ã‚Â»Ã‚Ân rÃƒÆ’Ã‚Âµ local service + Supabase hoÃƒÂ¡Ã‚ÂºÃ‚Â·c Docker + PostgreSQL local.
- Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ JWT secret vÃƒÆ’Ã‚Â  `AI_SYNC_SECRET`.
- Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng bÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ port backend vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi `CORE_API_BASE_URL`.
- XÃƒÆ’Ã‚Â¡c nhÃƒÂ¡Ã‚ÂºÃ‚Â­n `.env` khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Git theo dÃƒÆ’Ã‚Âµi.

## Phase -1 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Synthetic Seeder V2

Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â¢y lÃƒÆ’Ã‚Â  phase nÃƒÂ¡Ã‚ÂºÃ‚Â±m giÃƒÂ¡Ã‚Â»Ã‚Â¯a cÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh `.env` vÃƒÆ’Ã‚Â  Phase 0 cÃƒÂ¡Ã‚Â»Ã‚Â§a roadmap.

KhÃƒÆ’Ã‚Â´ng chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° thÃƒÆ’Ã‚Âªm biÃƒÂ¡Ã‚ÂºÃ‚Â¿n vÃƒÆ’Ã‚Â o `.env`. CÃƒÂ¡Ã‚ÂºÃ‚Â§n triÃƒÂ¡Ã‚Â»Ã†â€™n khai code tÃƒâ€ Ã‚Â°Ãƒâ€ Ã‚Â¡ng ÃƒÂ¡Ã‚Â»Ã‚Â©ng:

```text
AppForecastDemoProperties
ForecastDemoDataSeeder
forecast_demo_scenarios migration/entity
inventory history simulation
seed validation
seed reproducibility tests
cleanup safety tests
```

CÃƒÆ’Ã‚Â¡c biÃƒÂ¡Ã‚ÂºÃ‚Â¿n demand profile trong `ADMIN_AI_ENV_CONFIGURATION.md` chÃƒâ€ Ã‚Â°a cÃƒÆ’Ã‚Â³ tÃƒÆ’Ã‚Â¡c dÃƒÂ¡Ã‚Â»Ã‚Â¥ng
cho Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¿n khi code Phase -1 hoÃƒÆ’Ã‚Â n thÃƒÆ’Ã‚Â nh.

## Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã†â€™m dÃƒÂ¡Ã‚Â»Ã‚Â«ng bÃƒÂ¡Ã‚ÂºÃ‚Â¯t buÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c sau khi seed

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° chuyÃƒÂ¡Ã‚Â»Ã†â€™n sang forecasting khi dataset Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t:

- Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Âºng 180 ngÃƒÆ’Ã‚Â y.
- Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Âºng random seed vÃƒÆ’Ã‚Â  anchor date.
- Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Âºng sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ SKU theo tÃƒÂ¡Ã‚Â»Ã‚Â«ng demand profile.
- CÃƒÆ’Ã‚Â³ ground truth cho tÃƒÂ¡Ã‚Â»Ã‚Â«ng SKU demo.
- NgÃƒÆ’Ã‚Â y khÃƒÆ’Ã‚Â´ng bÃƒÆ’Ã‚Â¡n Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c biÃƒÂ¡Ã‚Â»Ã†â€™u diÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¦n bÃƒÂ¡Ã‚ÂºÃ‚Â±ng demand bÃƒÂ¡Ã‚ÂºÃ‚Â±ng 0.
- Ãƒâ€žÃ‚ÂÃƒâ€ Ã‚Â¡n hÃƒÂ¡Ã‚Â»Ã‚Â§y khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c tÃƒÆ’Ã‚Â­nh vÃƒÆ’Ã‚Â o valid demand.
- TÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng order bÃƒÂ¡Ã‚ÂºÃ‚Â±ng tÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u marker mong Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â£i.
- `line_total = unit_price * quantity`.
- TÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng order khÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºp tÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng order item.
- Inventory policy khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t hoÃƒÆ’Ã‚Â n toÃƒÆ’Ã‚Â n.
- Supplier, lead time, MOQ vÃƒÆ’Ã‚Â  pack size demo khÃƒÆ’Ã‚Â´ng bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ thiÃƒÂ¡Ã‚ÂºÃ‚Â¿u.
- ChÃƒÂ¡Ã‚ÂºÃ‚Â¡y lÃƒÂ¡Ã‚ÂºÃ‚Â¡i cÃƒÆ’Ã‚Â¹ng seed cho kÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£ aggregate giÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng nhau.

NÃƒÂ¡Ã‚ÂºÃ‚Â¿u chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t, sÃƒÂ¡Ã‚Â»Ã‚Â­a Seeder V2 vÃƒÆ’Ã‚Â  seed lÃƒÂ¡Ã‚ÂºÃ‚Â¡i; khÃƒÆ’Ã‚Â´ng chuyÃƒÂ¡Ã‚Â»Ã†â€™n sang Phase 0.

## ThÃƒÂ¡Ã‚Â»Ã‚Â© tÃƒÂ¡Ã‚Â»Ã‚Â± chÃƒÂ¡Ã‚ÂºÃ‚Â¡y forecasting

LuÃƒÆ’Ã‚Â´n thÃƒÂ¡Ã‚Â»Ã‚Â±c hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n:

```text
snapshot sync
  -> model evaluation
  -> kiÃƒÂ¡Ã‚Â»Ã†â€™m tra generation status
  -> forecast generation
  -> kiÃƒÂ¡Ã‚Â»Ã†â€™m tra generation status
  -> replenishment suggestions
```

KhÃƒÆ’Ã‚Â´ng chÃƒÂ¡Ã‚ÂºÃ‚Â¡y forecast trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc evaluation nÃƒÂ¡Ã‚ÂºÃ‚Â¿u mÃƒÂ¡Ã‚Â»Ã‚Â¥c tiÃƒÆ’Ã‚Âªu lÃƒÆ’Ã‚Â  chÃƒÂ¡Ã‚Â»Ã‚Ân model theo SKU.

## Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã†â€™m dÃƒÂ¡Ã‚Â»Ã‚Â«ng trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc Admin Copilot

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° bÃƒÂ¡Ã‚ÂºÃ‚Â¯t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u `chatbot-admin-service` khi:

- Data-quality API Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ hoÃƒÂ¡Ã‚ÂºÃ‚Â¡t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng.
- Demand classification Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ cÃƒÆ’Ã‚Â³ test.
- Forecast cÃƒÆ’Ã‚Â³ evaluation metrics.
- Decision engine trÃƒÂ¡Ã‚ÂºÃ‚Â£ structured explanation.
- Admin Intelligence UI hiÃƒÂ¡Ã‚Â»Ã†â€™n thÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng confidence/warnings.
- Read API Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.

Admin Copilot khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c dÃƒÆ’Ã‚Â¹ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ che lÃƒÂ¡Ã‚ÂºÃ‚Â¥p API hoÃƒÂ¡Ã‚ÂºÃ‚Â·c dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u chÃƒâ€ Ã‚Â°a hoÃƒÆ’Ã‚Â n chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â°nh.

## Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã†â€™m dÃƒÂ¡Ã‚Â»Ã‚Â«ng trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc ReAct

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° bÃƒÂ¡Ã‚ÂºÃ‚Â¯t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â§u ReAct khi:

- Tool selection accuracy Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 90%.
- Grounded numeric accuracy Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 95%.
- KhÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ role bypass.
- Read-only task success Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 85%.
- KhÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ infinite loop.

NÃƒÂ¡Ã‚ÂºÃ‚Â¿u query copilot mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t lÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£t Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ giÃƒÂ¡Ã‚ÂºÃ‚Â£i quyÃƒÂ¡Ã‚ÂºÃ‚Â¿t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ nghiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡p vÃƒÂ¡Ã‚Â»Ã‚Â¥ thÃƒÆ’Ã‚Â¬ khÃƒÆ’Ã‚Â´ng bÃƒÂ¡Ã‚ÂºÃ‚Â¯t buÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c dÃƒÆ’Ã‚Â¹ng
ReAct.

## Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã†â€™m dÃƒÂ¡Ã‚Â»Ã‚Â«ng trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc write actions

ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° mÃƒÂ¡Ã‚Â»Ã…Â¸ write tool khi cÃƒÆ’Ã‚Â³:

- Human approval UI.
- Audit log.
- Idempotency key.
- Before/after snapshot.
- Resource revalidation.
- Permission test theo role.
- Concurrent action test.

KhÃƒÆ’Ã‚Â´ng cho agent tÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng:

- Ãƒâ€žÃ‚ÂiÃƒÂ¡Ã‚Â»Ã‚Âu chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â°nh tÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“n kho.
- ChÃƒÂ¡Ã‚ÂºÃ‚Â¥p nhÃƒÂ¡Ã‚ÂºÃ‚Â­n nhÃƒÂ¡Ã‚ÂºÃ‚Â­p hÃƒÆ’Ã‚Â ng.
- HoÃƒÆ’Ã‚Â n tiÃƒÂ¡Ã‚Â»Ã‚Ân.
- Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i role ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng.
- KhÃƒÆ’Ã‚Â³a tÃƒÆ’Ã‚Â i khoÃƒÂ¡Ã‚ÂºÃ‚Â£n.
- XÃƒÆ’Ã‚Â³a catalog.

## LÃƒâ€ Ã‚Â°u ÃƒÆ’Ã‚Â½ an toÃƒÆ’Ã‚Â n

1. ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° seed database demo/development.
2. Backup Supabase trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi seed.
3. Cleanup chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° xÃƒÆ’Ã‚Â³a record cÃƒÆ’Ã‚Â³ marker chÃƒÆ’Ã‚Â­nh xÃƒÆ’Ã‚Â¡c.
4. Sau khi seed mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t lÃƒÂ¡Ã‚ÂºÃ‚Â§n, tÃƒÂ¡Ã‚ÂºÃ‚Â¯t:

```dotenv
APP_SEED_ENABLED=false
FORECAST_DEMO_ENABLED=false
```

5. KhÃƒÆ’Ã‚Â´ng commit `.env`, database password, JWT secret hoÃƒÂ¡Ã‚ÂºÃ‚Â·c API key.
6. KhÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t secret trong biÃƒÂ¡Ã‚ÂºÃ‚Â¿n `NEXT_PUBLIC_*`.
7. KhÃƒÆ’Ã‚Â´ng cho LLM chÃƒÂ¡Ã‚ÂºÃ‚Â¡y SQL tÃƒÆ’Ã‚Â¹y ÃƒÆ’Ã‚Â½.
8. KhÃƒÆ’Ã‚Â´ng gÃƒÂ¡Ã‚Â»Ã‚Â­i access token hoÃƒÂ¡Ã‚ÂºÃ‚Â·c PII vÃƒÆ’Ã‚Â o prompt.
9. KhÃƒÆ’Ã‚Â´ng dÃƒÆ’Ã‚Â¹ng dÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u synthetic Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ tuyÃƒÆ’Ã‚Âªn bÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ chÃƒÆ’Ã‚Â­nh xÃƒÆ’Ã‚Â¡c production.
10. LÃƒâ€ Ã‚Â°u dataset manifest Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ tÃƒÆ’Ã‚Â¡i lÃƒÂ¡Ã‚ÂºÃ‚Â­p kÃƒÂ¡Ã‚ÂºÃ‚Â¿t quÃƒÂ¡Ã‚ÂºÃ‚Â£.

## Quy tÃƒÂ¡Ã‚ÂºÃ‚Â¯c chuyÃƒÂ¡Ã‚Â»Ã†â€™n phase

MÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i phase trong roadmap cÃƒÆ’Ã‚Â³ `Definition of Done`. ChÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° chuyÃƒÂ¡Ã‚Â»Ã†â€™n phase khi tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£
Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã‚Âu kiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n bÃƒÂ¡Ã‚ÂºÃ‚Â¯t buÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t hoÃƒÂ¡Ã‚ÂºÃ‚Â·c phÃƒÂ¡Ã‚ÂºÃ‚Â§n chÃƒâ€ Ã‚Â°a Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¡t Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c ghi rÃƒÆ’Ã‚Âµ thÃƒÆ’Ã‚Â nh technical debt
khÃƒÆ’Ã‚Â´ng ÃƒÂ¡Ã‚ÂºÃ‚Â£nh hÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã…Â¸ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¿n phase tiÃƒÂ¡Ã‚ÂºÃ‚Â¿p theo.

KhÃƒÆ’Ã‚Â´ng triÃƒÂ¡Ã‚Â»Ã†â€™n khai Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng thÃƒÂ¡Ã‚Â»Ã‚Âi Phase 5ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“8 khi Phase 0ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“4 chÃƒâ€ Ã‚Â°a ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh.


## Phase -1 progress notes - 2026-07-25

- [x] Added `AppSyntheticDataProperties` safety gate: synthetic seed requires `SYNTHETIC_DATA_ALLOWED=true` and environment `demo` or `development`.
- [x] Expanded `AppForecastDemoProperties` for Synthetic Seeder V2 profile counts, multipliers, cancellation rate, supplier/policy settings, ground-truth flag, and scenario version.
- [x] Added `V23__forecast_demo_scenarios.sql` to persist per-variant demand profile, ground-truth totals, supplier, lead time, MOQ, pack size, service level, seed, marker, and anchor metadata.
- [x] Reworked `ForecastDemoDataSeeder` to generate deterministic one-item orders with exact marker cleanup, reproducible UUIDs, no-demand variants, cancelled-order exclusion from valid demand, diversified inventory policies, and scenario ground truth.
- [x] Updated backend/docker env examples and `application.yml` with Synthetic Seeder V2 variables; seed defaults remain disabled.
- [x] Added integration coverage for reproducibility, ground truth, policy diversity, exact-marker cleanup, cancelled-order handling, and `line_total` integrity.
- [x] Verification: `.\mvnw.cmd -Dtest=ForecastDemoDataSeederIntegrationTest test` passed with 2 tests on 2026-07-25.
- [ ] Real 180-day seed has not been run yet. Keep the mandatory stop until the target DB is confirmed demo/development and backed up.
## Phase 0 progress notes - 2026-07-25

- [x] Added `AiDataQualityProperties` typed thresholds bound from `AI_DATA_QUALITY_*` variables.
- [x] Added read-only Admin Data Quality API under `/api/v1/admin/ai/data-quality` with summary, variant list, and variant detail endpoints.
- [x] Added `SkuDataQualityService` scoring for continuous sales history, non-zero demand days, inventory snapshot coverage, supplier configuration, and warnings.
- [x] Data-quality evaluation treats missing sales snapshot days as a blocking issue because zero-demand days must be materialized as quantity 0.
- [x] Added unit coverage for missing sales days, high-quality complete series, supplier gaps, and inventory history gaps.
- [x] Verification: `.\mvnw.cmd -Dtest=SkuDataQualityServiceTest test` passed with 3 tests on 2026-07-25.
- [ ] Data source DEMO/REAL/IMPORTED is not yet added to snapshot metadata.
- [ ] Real 180-day seed remains intentionally not run until DB demo/development and backup are confirmed.
