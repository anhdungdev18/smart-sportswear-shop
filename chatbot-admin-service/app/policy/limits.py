from time import monotonic

from app.config.settings import settings

_BUCKETS: dict[str, list[float]] = {}


def assert_rate_limit(actor_id: str) -> None:
    now = monotonic()
    window_start = now - 60
    bucket = [ts for ts in _BUCKETS.get(actor_id, []) if ts >= window_start]
    if len(bucket) >= settings.RATE_LIMIT_PER_MINUTE:
        raise PermissionError("Rate limit exceeded")
    bucket.append(now)
    _BUCKETS[actor_id] = bucket
