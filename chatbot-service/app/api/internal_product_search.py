from __future__ import annotations

import hmac

from fastapi import APIRouter, Header, HTTPException, status

from app.config.settings import settings
from app.schemas.internal_product_search import InternalSearchRequest, InternalSearchResponse
from app.services.product_search_service import search_internal

router = APIRouter(prefix="/internal/v1", tags=["internal-product-search"])


@router.post("/product-search", response_model=InternalSearchResponse)
async def product_search(
    request: InternalSearchRequest,
    x_internal_token: str = Header(default="", alias="X-Internal-Token"),
) -> InternalSearchResponse:
    expected = settings.PRODUCT_SEARCH_INTERNAL_TOKEN
    if not expected or not hmac.compare_digest(
        x_internal_token.encode("utf-8"), expected.encode("utf-8")
    ):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")
    try:
        return InternalSearchResponse.model_validate(await search_internal(request))
    except HTTPException:
        raise
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Product search temporarily unavailable",
        ) from None
