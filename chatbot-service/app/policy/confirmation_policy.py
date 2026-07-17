from __future__ import annotations

# Phase 2: confirmation is derived from ToolDefinition.requires_confirmation.
# Phase 9+: replace with multi-turn session confirmation store.


def check(requires_confirmation: bool) -> tuple[bool, str]:
    """
    Returns (needs_confirm, reason).
    needs_confirm=True  → block execution, ask user to confirm
    needs_confirm=False → proceed
    """
    if requires_confirmation:
        return True, "confirmation_required"
    return False, "no_confirmation_required"
