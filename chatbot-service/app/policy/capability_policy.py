from __future__ import annotations

# In-memory capability registry. Phase 3+: replace with DB/config store.
# Keys match ToolDefinition.capability field.
_capabilities: dict[str, bool] = {
    "product_search": True,
    "knowledge_qa":   True,
    "order_status":   True,
    "cart_action":    True,
    "order_action":   True,
}


def is_enabled(capability: str) -> bool:
    return _capabilities.get(capability, False)


def set_enabled(capability: str, enabled: bool) -> None:
    _capabilities[capability] = enabled


def get_all() -> dict[str, bool]:
    return dict(_capabilities)
