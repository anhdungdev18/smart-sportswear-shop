from __future__ import annotations

from app.graph.state import AgentState
from app.tools.registry import registry
from app.observability.trace_logger import get_logger, log_tool_call

logger = get_logger(__name__)


async def tool_executor_node(state: AgentState) -> dict:
    tool_name = state["selected_tool"]

    if tool_name == "none":
        return {"tool_result": {}}

    entry = registry.get(tool_name)
    if entry is None:
        msg = f"Tool '{tool_name}' not found in registry"
        logger.warning(f"[{state['session_id']}] {msg}")
        return {"tool_result": {}, "errors": state["errors"] + [msg]}

    definition, fn = entry
    args = state["tool_args"]

    log_tool_call(logger, state["session_id"], tool_name, args)

    try:
        result = await fn(args)
        logger.info(f"[{state['session_id']}] tool_executed | tool={tool_name} ok=true")
        return {"tool_result": result}
    except Exception as exc:
        msg = f"Tool '{tool_name}' failed: {exc}"
        logger.error(f"[{state['session_id']}] {msg}")
        return {"tool_result": {}, "errors": state["errors"] + [msg]}
