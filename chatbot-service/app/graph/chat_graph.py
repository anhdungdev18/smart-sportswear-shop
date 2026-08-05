"""
LangGraph StateGraph pipeline — mirrors DigiAISaleAgent's graph structure.

Graph topology:
  START
    └─ input_guard
         ├─ [end]      → END               (empty message blocked)
         └─ [continue] → load_context      (cold-start DB history)
                           └─ normalize    (parse query once)
                                └─ intent_router
                                     └─ select_mode
                                          ├─ [fast]     → tool_selector → policy_guard
                                          ├─ [workflow] → tool_selector_llm → policy_guard
                                          └─ [clarify]  → generate_answer
                                                              └─ validate_answer
                                                                     └─ save_result → END
  policy_guard → tool_executor → generate_answer → validate_answer → save_result → END
"""
from __future__ import annotations

from functools import lru_cache

from langgraph.graph import END, START, StateGraph

from app.graph.state import AgentState
from app.graph.routing import route_after_guard, route_by_mode

# Nodes
from app.graph.nodes.input_guard            import input_guard_node
from app.graph.nodes.load_context           import load_context_node
from app.graph.nodes.normalize              import normalize_node
from app.graph.nodes.intent_router          import intent_router_node
from app.graph.nodes.select_mode            import select_mode_node
from app.graph.nodes.tool_selector          import tool_selector_node
from app.graph.nodes.tool_selector_llm_node import tool_selector_llm_node
from app.graph.nodes.policy_guard           import policy_guard_node
from app.graph.nodes.tool_executor          import tool_executor_node
from app.graph.nodes.response_generator     import response_generator_node
from app.graph.nodes.validate_answer        import validate_answer_node
from app.graph.nodes.save_result            import save_result_node


def _build_graph() -> StateGraph:
    g = StateGraph(AgentState)

    # ── Register nodes ────────────────────────────────────────────────────────
    g.add_node("input_guard",         input_guard_node)
    g.add_node("load_context",        load_context_node)
    g.add_node("normalize",           normalize_node)
    g.add_node("intent_router",       intent_router_node)
    g.add_node("select_mode",         select_mode_node)
    g.add_node("tool_selector",       tool_selector_node)
    g.add_node("tool_selector_llm",   tool_selector_llm_node)
    g.add_node("policy_guard",        policy_guard_node)
    g.add_node("tool_executor",       tool_executor_node)
    g.add_node("generate_answer",     response_generator_node)
    g.add_node("validate_answer",     validate_answer_node)
    g.add_node("save_result",         save_result_node)

    # ── Edges ─────────────────────────────────────────────────────────────────
    g.add_edge(START, "input_guard")

    # input_guard: empty/blocked → END, valid → load_context
    g.add_conditional_edges(
        "input_guard",
        route_after_guard,
        {"continue": "load_context", "end": END},
    )

    g.add_edge("load_context", "normalize")
    g.add_edge("normalize",    "intent_router")
    g.add_edge("intent_router", "select_mode")

    # select_mode: branch by execution_mode
    g.add_conditional_edges(
        "select_mode",
        route_by_mode,
        {
            "fast":     "tool_selector",
            "workflow": "tool_selector_llm",
            "clarify":  "generate_answer",
        },
    )

    # Both tool selectors feed into policy_guard
    g.add_edge("tool_selector",     "policy_guard")
    g.add_edge("tool_selector_llm", "policy_guard")

    g.add_edge("policy_guard",    "tool_executor")
    g.add_edge("tool_executor",   "generate_answer")
    g.add_edge("generate_answer", "validate_answer")
    g.add_edge("validate_answer", "save_result")
    g.add_edge("save_result",     END)

    return g.compile()


@lru_cache(maxsize=1)
def get_graph():
    """Compiled graph singleton — built once per process."""
    return _build_graph()


async def run_chat_graph(
    session_id: str,
    user_id: str | None,
    channel: str,
    message: str,
    user_role: str | None = None,
    access_token: str | None = None,
    session_context: dict | None = None,
    is_new_session: bool = False,
) -> AgentState:
    initial_state: AgentState = {
        "session_id":            session_id,
        "user_id":               user_id,
        "user_role":             user_role,
        "access_token":          access_token,
        "channel":               channel,
        "message":               message,
        "intent":                "",
        "intent_confidence":     0.0,
        "normalized_query":      "",
        "parsed_query":          {},
        "selected_tool":         "none",
        "tool_args":             {},
        "tool_result":           None,
        "reply":                 "",
        "errors":                [],
        "policy_allowed":        True,
        "policy_reason":         "no_tool",
        "requires_confirmation": False,
        "execution_blocked":     False,
        "session_context":       session_context or {},
        "is_new_session":        is_new_session,
        "pending_action_display": "",
        "chat_history":          (session_context or {}).get("chat_history", []),
        "execution_mode":        "",
    }
    result = await get_graph().ainvoke(initial_state)
    return result  # type: ignore[return-value]
