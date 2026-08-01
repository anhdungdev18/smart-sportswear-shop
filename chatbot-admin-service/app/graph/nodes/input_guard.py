from fastapi import HTTPException, status

from app.config.settings import settings


def guard_input(message: str) -> None:
    if len(message) > settings.MAX_INPUT_CHARS:
        raise HTTPException(status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, detail="Message is too long")
    blocked_terms = ["drop table", "delete from", "update users", "jwt_access_secret", "db_password"]
    lowered = message.lower()
    if any(term in lowered for term in blocked_terms):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Unsafe admin-copilot input")
