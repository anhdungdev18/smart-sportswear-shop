# chatbot-service

AI chatbot service cho sport shop — độc lập với Spring Boot backend.

**Phase hiện tại: Phase 0 — Scaffold** (chưa có retrieval thật, chưa có graph thật)

---

## Cách chạy local

```bash
cd chatbot-service

# 1. Tạo virtualenv
python -m venv .venv
.venv\Scripts\activate        # Windows
# source .venv/bin/activate   # macOS/Linux

# 2. Cài dependencies
pip install -r requirements.txt

# 3. Tạo .env từ example
cp .env.example .env
# Điền ANTHROPIC_API_KEY nếu cần

# 4. Chạy service
python run.py
# hoặc
uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
```

Service sẽ chạy tại `http://localhost:8001`

---

## Endpoints hiện có

| Method | Path      | Mô tả                        |
|--------|-----------|------------------------------|
| GET    | /health   | Health check                 |
| POST   | /chat     | Chat endpoint (mock Phase 0) |
| GET    | /docs     | Swagger UI (auto)            |

### GET /health

```json
{ "status": "ok", "service": "chatbot-service" }
```

### POST /chat

Request:
```json
{
  "sessionId": "sess_123",
  "userId": "u_123",
  "message": "Tôi cần giày chạy bộ dưới 1 triệu",
  "channel": "web"
}
```

Response (Phase 0 mock):
```json
{
  "reply": "Đây là phản hồi mock từ chatbot-service Phase 0.",
  "toolCalls": [],
  "suggestions": [],
  "sessionState": { "sessionId": "sess_123" }
}
```

---

## Biến môi trường

| Biến                  | Default           | Ghi chú                        |
|-----------------------|-------------------|--------------------------------|
| CHATBOT_ENV           | development       |                                |
| CHATBOT_HOST          | 0.0.0.0           |                                |
| CHATBOT_PORT          | 8001              |                                |
| MODEL_PROVIDER        | anthropic         |                                |
| MODEL_NAME            | claude-sonnet-4-6 |                                |
| DB_READ_URL           | (trống)           | Dùng từ Phase 3                |
| BACKEND_API_BASE_URL  | localhost:8080    | Dùng từ Phase 5                |
| REDIS_URL             | localhost:6379    | Dùng từ Phase 9                |
| LOG_LEVEL             | INFO              |                                |
| ANTHROPIC_API_KEY     | (trống)           | Bắt buộc từ Phase 1            |

---

## Cấu trúc thư mục

```
app/
  api/            — FastAPI routers
  config/         — Settings (pydantic-settings)
  graph/          — LangGraph orchestration (Phase 1+)
  tools/          — Tool Registry + Executor (Phase 1+)
  retrieval/
    product/      — Product search retrieval (Phase 3+)
    knowledge/    — FAQ/policy retrieval (Phase 4+)
  services/       — Use-case business logic (Phase 3+)
  repositories/   — Read-only DB access (Phase 3+)
  clients/        — HTTP clients → Spring Boot (Phase 5+)
  policy/         — Capability/auth/confirmation guards (Phase 2+)
  memory/         — Session + conversation memory (Phase 9+)
  observability/  — Logging, tracing, evaluation
  schemas/        — Pydantic request/response schemas
```

---

## Roadmap phase

| Phase | Nội dung                        |
|-------|---------------------------------|
| 0     | ✅ Scaffold, mock endpoint       |
| 1     | LangGraph, intent router        |
| 2     | Policy layer                    |
| 3     | Product retrieval               |
| 4     | Knowledge retrieval             |
| 5     | Transaction tools (Spring Boot) |
| 6-10  | Detail, recommendation, hybrid… |
