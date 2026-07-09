from typing import Any

# TODO Phase 1: build StateGraph with the following nodes:
#   intent_router -> tool_selector -> policy_guard -> tool_executor -> response_generator
# Flow: API -> Graph -> Tool Selector -> Policy Guard -> Tool -> Service -> Response


async def run_chat_graph(
    session_id: str,
    user_id: str | None,
    message: str,
) -> dict[str, Any]:
    """Phase 0 stub — graph not wired yet."""
    raise NotImplementedError("chat_graph not implemented — Phase 1")
