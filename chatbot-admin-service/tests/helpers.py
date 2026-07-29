import base64
import hashlib
import hmac
import json
import time

from app.config.settings import settings


def make_token(role: str = "ADMIN") -> str:
    header = {"alg": settings.JWT_ACCESS_ALGORITHM, "typ": "JWT"}
    payload = {
        "sub": "admin@example.com",
        "userId": "11111111-1111-1111-1111-111111111111",
        "role": role,
        "exp": int(time.time()) + 300,
    }
    signing_input = ".".join([_b64(header), _b64(payload)])
    signature = hmac.new(settings.JWT_ACCESS_SECRET.encode("utf-8"), signing_input.encode("ascii"), hashlib.sha256).digest()
    return f"{signing_input}.{_b64_bytes(signature)}"


def _b64(payload: dict) -> str:
    return _b64_bytes(json.dumps(payload, separators=(",", ":")).encode("utf-8"))


def _b64_bytes(payload: bytes) -> str:
    return base64.urlsafe_b64encode(payload).decode("ascii").rstrip("=")
