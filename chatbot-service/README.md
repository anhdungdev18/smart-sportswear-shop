# chatbot-service

AI chatbot service cho sport shop — độc lập với Spring Boot backend.

**Phase hiện tại: Phase 1 — Orchestration nền** (graph chạy thật, tools mock, chưa có retrieval thật)

---

## Cách chạy local

```bash
cd chatbot-service

python -m venv .venv
.venv\Scripts\activate        # Windows
# source .venv/bin/activate   # macOS/Linux

pip install -r requirements.txt

cp .env.example .env

python run.py
# hoặc: uvicorn app.main:app --reload --host 0.0.0.0 --port 8002
```

Service chạy tại `http://localhost:8002`

---

## Endpoints

| Method | Path    | Mô tả                              |
|--------|---------|------------------------------------|
| GET    | /health | Health check                       |
| POST   | /chat   | Chat — đi qua orchestration graph  |
| GET    | /docs   | Swagger UI                         |

### POST /chat

Request:
```json
{
  "sessionId": "sess_123",
  "userId": "u_123",
  "message": "Tôi muốn mua giày chạy bộ dưới 1 triệu",
  "channel": "web"
}
```

Response (Phase 1):
```json
{
  "reply": "Tôi tìm thấy 2 sản phẩm phù hợp: Giày chạy bộ Dry-Fit, Áo thể thao thoáng khí.",
  "toolCalls": [
    { "tool": "search_products", "result": { "items": [...], "total": 2 } }
  ],
  "suggestions": [],
  "sessionState": {
    "sessionId": "sess_123",
    "intent": "PRODUCT_SEARCH",
    "selectedTool": "search_products"
  }
}
```

---

## Intent đã support (Phase 1)

| Intent           | Từ khóa kích hoạt                              | Tool được gọi        |
|------------------|------------------------------------------------|----------------------|
| PRODUCT_SEARCH   | tìm, muốn mua, giày, áo, quần, phụ kiện…      | search_products      |
| KNOWLEDGE_QA     | chính sách, đổi trả, giao hàng, size…         | answer_knowledge     |
| ORDER_STATUS     | đơn hàng, mã đơn, trạng thái đơn…             | get_order_status     |
| ADD_TO_CART      | thêm vào giỏ, giỏ hàng…                       | add_to_cart          |
| CANCEL_ORDER     | hủy đơn, cancel đơn…                          | cancel_order         |
| UNKNOWN          | không match                                    | (none)               |

---

## Tools mock (Phase 1)

| Tool               | Trả về                             | Thật từ phase |
|--------------------|------------------------------------|---------------|
| search_products    | 2 sản phẩm mock                    | Phase 3       |
| answer_knowledge   | chính sách mock                    | Phase 4       |
| get_order_status   | status mock PENDING_CONFIRMATION   | Phase 5       |
| add_to_cart        | `{mock: true}`                     | Phase 5       |
| cancel_order       | `{mock: true}`                     | Phase 5       |

---

## Orchestration flow (Phase 1)

```
POST /chat
  -> intent_router_node   (rule-based keyword matching)
  -> tool_selector_node   (intent -> tool name + args)
  -> tool_executor_node   (registry lookup + run mock fn)
  -> response_generator   (template reply per intent)
  -> ChatResponse
```

---

## Biến môi trường

| Biến                 | Default           | Dùng từ phase |
|----------------------|-------------------|---------------|
| CHATBOT_ENV          | development       | Phase 0       |
| CHATBOT_HOST         | 0.0.0.0           | Phase 0       |
| CHATBOT_PORT         | 8002              | Phase 0       |
| MODEL_PROVIDER       | anthropic         | Phase 1+      |
| MODEL_NAME           | claude-sonnet-4-6 | Phase 1+      |
| DB_READ_URL          | (trống)           | Phase 3       |
| BACKEND_API_BASE_URL | localhost:8080    | Phase 5       |
| REDIS_URL            | localhost:6379    | Phase 9       |
| LOG_LEVEL            | INFO              | Phase 0       |
| ANTHROPIC_API_KEY    | (trống)           | Phase 1+      |

---

## Cấu trúc thư mục

```
app/
  api/            — FastAPI routers
  config/         — Settings (pydantic-settings)
  graph/
    state.py      — AgentState TypedDict
    chat_graph.py — Sequential node runner (→ LangGraph Phase 2)
    nodes/        — intent_router, tool_selector, tool_executor, response_generator
  tools/
    registry.py   — ToolRegistry + ToolDefinition + setup_registry()
    *_tool.py     — Mock tool adapters (5 tools)
  retrieval/
    product/      — Phase 3+
    knowledge/    — Phase 4+
  services/       — Phase 3+
  repositories/   — Phase 3+
  clients/        — Phase 5+ (HTTP → Spring Boot)
  policy/         — Phase 2+
  memory/         — Phase 9+
  observability/  — Logging
  schemas/        — Pydantic schemas
```

---

## Roadmap

| Phase | Nội dung                         | Status   |
|-------|----------------------------------|----------|
| 0     | Scaffold, mock endpoint          | ✅ Done   |
| 1     | Orchestration nền, tool mock     | ✅ Done   |
| 2     | Policy layer                     | Planned  |
| 3     | Product retrieval thật           | Planned  |
| 4     | Knowledge retrieval thật         | Planned  |
| 5     | Transaction tools (Spring Boot)  | Planned  |
| 6-10  | Detail, recommendation, hybrid…  | Planned  |
