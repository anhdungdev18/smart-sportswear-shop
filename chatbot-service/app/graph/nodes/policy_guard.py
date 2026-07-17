from __future__ import annotations

from app.graph.state import AgentState
from app.tools.registry import registry
from app.policy import capability_policy, auth_policy, confirmation_policy
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


def _build_pending_display(tool_name: str, args: dict) -> str:
    """Human-readable description used in the confirmation question."""
    if tool_name == "cancel_order":
        code = (args.get("order_code") or "").strip()
        return f"hủy đơn {code}" if code else "hủy đơn"
    if tool_name == "add_to_cart":
        return "thêm sản phẩm vào giỏ hàng"
    return "thao tác này"


async def policy_guard_node(state: AgentState) -> dict:
    tool_name = state["selected_tool"]
    sid = state["session_id"]

    # No tool selected — nothing to guard
    if tool_name == "none":
        return {
            "policy_allowed": True,
            "policy_reason": "no_tool",
            "execution_blocked": False,
            "requires_confirmation": False,
            "pending_action_display": "",
        }

    entry = registry.get(tool_name)
    if entry is None:
        logger.warning(f"[{sid}] policy_guard | tool={tool_name} reason=unknown_tool")
        return {
            "policy_allowed": False,
            "policy_reason": "unknown_tool",
            "execution_blocked": True,
            "requires_confirmation": False,
            "pending_action_display": "",
        }

    definition, _ = entry
    capability = definition.capability

    # 1. Capability check
    if not capability_policy.is_enabled(capability):
        logger.info(f"[{sid}] policy_guard | tool={tool_name} blocked=capability_disabled")
        return {
            "policy_allowed": False,
            "policy_reason": "capability_disabled",
            "execution_blocked": True,
            "requires_confirmation": False,
            "pending_action_display": "",
        }

    # 2. Auth check
    auth_ok, auth_reason = auth_policy.check(definition.requires_auth, state.get("user_id"))
    if not auth_ok:
        logger.info(f"[{sid}] policy_guard | tool={tool_name} blocked={auth_reason}")
        return {
            "policy_allowed": False,
            "policy_reason": auth_reason,
            "execution_blocked": True,
            "requires_confirmation": False,
            "pending_action_display": "",
        }

    # Phase 9: bypass confirmation check if user just confirmed this action
    if state.get("intent") == "CONFIRM_ACTION":
        logger.info(f"[{sid}] policy_guard | tool={tool_name} user_confirmed=true allowed=true")
        return {
            "policy_allowed": True,
            "policy_reason": "user_confirmed",
            "execution_blocked": False,
            "requires_confirmation": False,
            "pending_action_display": "",
        }

    # 3. Confirmation check
    needs_confirm, confirm_reason = confirmation_policy.check(definition.requires_confirmation)
    if needs_confirm:
        display = _build_pending_display(tool_name, state.get("tool_args") or {})
        logger.info(
            f"[{sid}] policy_guard | tool={tool_name} blocked={confirm_reason} display={display!r}"
        )
        return {
            "policy_allowed": True,
            "policy_reason": confirm_reason,
            "execution_blocked": True,
            "requires_confirmation": True,
            "pending_action_display": display,
        }

    logger.info(f"[{sid}] policy_guard | tool={tool_name} allowed=true")
    return {
        "policy_allowed": True,
        "policy_reason": "allowed",
        "execution_blocked": False,
        "requires_confirmation": False,
        "pending_action_display": "",
    }
