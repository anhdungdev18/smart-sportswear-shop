"""
Conditional edge routing functions for LangGraph StateGraph.

Each function receives the current AgentState and returns a string key
that maps to the next node in add_conditional_edges().
"""
from __future__ import annotations

from langgraph.graph import END

from app.graph.state import AgentState


def route_after_guard(state: AgentState) -> str:
    """After input_guard: skip graph if input was empty/blocked."""
    if state.get("execution_blocked") and not state.get("intent"):
        return "end"
    return "continue"


def route_by_mode(state: AgentState) -> str:
    """
    After select_mode: branch into fast / workflow / clarify.

    fast     — known intent, deterministic tool selection
    workflow — UNKNOWN intent + LLM available → LLM picks tool
    clarify  — UNKNOWN intent + no LLM → ask for clarification
    """
    return state.get("execution_mode") or "fast"
