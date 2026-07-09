from __future__ import annotations

from app.graph.state import AgentState
from app.graph.nodes.intent_router import intent_router_node
from app.graph.nodes.tool_selector import tool_selector_node
from app.graph.nodes.tool_executor import tool_executor_node
from app.graph.nodes.response_generator import response_generator_node

# Sequential node pipeline.
# Each node: AgentState -> dict (partial state update) — identical to LangGraph node contract.
# TODO Phase 2: migrate to LangGraph StateGraph when policy_guard node is added.
_NODES = [
    intent_router_node,
    tool_selector_node,
    tool_executor_node,
    response_generator_node,
]


async def run_chat_graph(
    session_id: str,
    user_id: str | None,
    channel: str,
    message: str,
) -> AgentState:
    state: AgentState = {
        "session_id": session_id,
        "user_id": user_id,
        "channel": channel,
        "message": message,
        "intent": "",
        "selected_tool": "none",
        "tool_args": {},
        "tool_result": None,
        "reply": "",
        "errors": [],
    }
    for node in _NODES:
        update = await node(state)
        state = {**state, **update}
    return state
