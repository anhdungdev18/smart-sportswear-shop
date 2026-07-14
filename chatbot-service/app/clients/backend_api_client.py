from __future__ import annotations

import httpx
from app.config.settings import settings
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_TIMEOUT = 10.0  # seconds


class BackendError(Exception):
    def __init__(self, message: str, code: str, status: int | None = None):
        super().__init__(message)
        self.code = code        # auth_required | forbidden | not_found | conflict | invalid | timeout | unavailable
        self.status = status


def _auth_headers(access_token: str | None) -> dict:
    if access_token:
        return {"Authorization": f"Bearer {access_token}"}
    return {}


def _raise_for_status(response: httpx.Response) -> None:
    s = response.status_code
    if s == 200 or s == 201:
        return
    try:
        body = response.json()
        msg = body.get("message", response.text)
    except Exception:
        msg = response.text

    if s == 401:
        raise BackendError(msg, "auth_required", s)
    if s == 403:
        raise BackendError(msg, "forbidden", s)
    if s == 404:
        raise BackendError(msg, "not_found", s)
    if s == 409:
        raise BackendError(msg, "conflict", s)
    if s in (400, 422):
        raise BackendError(msg, "invalid", s)
    raise BackendError(msg, "backend_error", s)


async def get(path: str, access_token: str | None = None, params: dict | None = None) -> dict:
    url = f"{settings.BACKEND_API_BASE_URL}{path}"
    headers = _auth_headers(access_token)
    logger.debug(f"backend_client GET {url} params={params}")
    try:
        async with httpx.AsyncClient(timeout=_TIMEOUT) as client:
            r = await client.get(url, headers=headers, params=params)
        logger.info(f"backend_client GET {path} status={r.status_code}")
        _raise_for_status(r)
        return r.json()
    except BackendError:
        raise
    except httpx.TimeoutException:
        raise BackendError("Backend timeout", "timeout")
    except httpx.ConnectError:
        raise BackendError("Backend unreachable", "unavailable")


async def post(path: str, access_token: str | None = None, json: dict | None = None) -> dict:
    url = f"{settings.BACKEND_API_BASE_URL}{path}"
    headers = _auth_headers(access_token)
    logger.debug(f"backend_client POST {url}")
    try:
        async with httpx.AsyncClient(timeout=_TIMEOUT) as client:
            r = await client.post(url, headers=headers, json=json or {})
        logger.info(f"backend_client POST {path} status={r.status_code}")
        _raise_for_status(r)
        return r.json()
    except BackendError:
        raise
    except httpx.TimeoutException:
        raise BackendError("Backend timeout", "timeout")
    except httpx.ConnectError:
        raise BackendError("Backend unreachable", "unavailable")
