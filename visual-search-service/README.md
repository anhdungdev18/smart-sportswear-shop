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
