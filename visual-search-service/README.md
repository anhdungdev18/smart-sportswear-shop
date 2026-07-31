# Visual Search Service

Independent FastAPI service for catalog image embeddings and visual product search. The feature flag is off by default and the fake deterministic provider is the safe local/test default.

## Local development

```powershell
python -m venv .venv
.\.venv\Scripts\pip install -r requirements-test.txt
.\.venv\Scripts\pytest
.\.venv\Scripts\uvicorn app.main:app --reload --port 8090
```

Copy `.env.example` to `.env` for local overrides. Never commit provider or internal-service secrets.

## Phase 3 image pipeline

Catalog downloads accept only HTTPS URLs from the configured Cloudinary cloud or
Shopify `/s/files/` path. Redirect targets and resolved/connected IP addresses are
checked to prevent SSRF, and response bytes, MIME type, decoded pixels and image
format are bounded before a deterministic JPEG is produced and hashed.

Set `VISUAL_EMBEDDING_PROVIDER=voyage` and provide `VOYAGE_API_KEY` to use the
real `voyage-multimodal-3.5` provider. Catalog images are sent as normalized
base64 bytes with `input_type=document`; search images use `input_type=query`.
The API call has bounded timeout/retries and validates the configured vector
dimension. Unit tests use an in-memory HTTP transport and never call Voyage or
download from the Internet.

## Phase 6 search API

`POST /internal/v1/search?limit=20` accepts a multipart field named `image` and
requires `X-Internal-Service-Token`. It validates and normalizes the upload,
creates a query embedding, runs exact cosine search against READY embeddings
for the ACTIVE model, groups matches by product, and excludes non-ACTIVE
products. Query images are not stored; only provider usage and latency are
recorded.

## Phase 4 indexing worker

Start the durable catalog consumer separately from the HTTP process:

```powershell
python -m app.worker
```

The worker uses manual acknowledgements and a configurable prefetch (default 5).
Transient database/network/provider failures are republished as persistent
messages through the 30-second, 5-minute and 1-hour TTL queues. Invalid events,
permanent image errors and exhausted retries are sent to
`visual-search.indexing.dlq`. Duplicate event IDs and unchanged READY image
hashes are skipped without another provider call.

`GET /health/ready` verifies the database connection, an ACTIVE model matching
the configured provider/model/dimensions, and the main RabbitMQ queue. It returns
HTTP 503 with component states when any dependency is unavailable. When running
the Python process directly on the host, `RABBITMQ_URL` must use `localhost`;
the `rabbitmq` hostname is only valid from another Docker Compose container.

Run the broker integration test after starting RabbitMQ:

```powershell
$env:RUN_RABBITMQ_INTEGRATION='1'
python -m pytest -q
```
