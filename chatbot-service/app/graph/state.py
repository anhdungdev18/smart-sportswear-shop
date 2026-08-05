from __future__ import annotations

from typing import Any, TypedDict


class AgentState(TypedDict):
    session_id: str
    user_id: str | None
    user_role: str | None       # "guest" | "customer" | "admin" | None
    access_token: str | None    # Phase 5: Bearer token forwarded to backend
    channel: str
    message: str
    intent: str                 # SKU_LOOKUP | PRODUCT_SEARCH | PRODUCT_DETAIL | RECOMMEND_PRODUCTS | SIZE_ADVISOR | KNOWLEDGE_QA | ORDER_STATUS | ADD_TO_CART | CANCEL_ORDER | CONFIRM_ACTION | REJECT_ACTION | EXPIRED_CONFIRMATION | UNKNOWN
    selected_tool: str          # primary tool name from registry, or "none"
    tool_args: dict[str, Any]
    tool_result: dict[str, Any] | None
    # --- Parallel secondary tools (read-only compound queries) ---
    secondary_tools: list[dict]         # [{"tool": name, "args": {...}}] run alongside primary
    secondary_results: dict[str, Any]   # tool_name -> result, filled by tool_executor
    reply: str
    errors: list[str]
    # --- Policy fields (Phase 2) ---
    policy_allowed: bool
    policy_reason: str          # "allowed" | "capability_disabled" | "auth_required" | "confirmation_required" | "no_tool" | "unknown_tool"
    requires_confirmation: bool
    execution_blocked: bool
    # --- Session context (Phase 6) ---
    session_context: dict       # short-term memory: last shown products, selected product, variant hints
    is_new_session: bool        # True on the very first message of a freshly-minted session → skip DB history load
    # --- Confirmation flow (Phase 9) ---
    pending_action_display: str  # human-readable label set by policy_guard when confirmation_required
    # --- Conversation history (Phase 10) ---
    chat_history: list[dict]    # last N turns: [{"role": "user"/"assistant", "content": "..."}]
    # --- Execution routing ---
    execution_mode: str         # "fast" | "workflow" | "clarify" — set by select_mode node
    # --- Query normalization (DigiAI-style) ---
    normalized_query: str       # lowercased/stripped, set by normalize node
    parsed_query: dict          # output of parse_query: product_type, sport_type_hint, gender, etc.
    intent_confidence: float    # 0.0–1.0, set by intent_router_node
