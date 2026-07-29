from __future__ import annotations

from uuid import uuid4

from fastapi import HTTPException, status

from app.auth.jwt_verifier import verify_admin_jwt
from app.graph.nodes.generate_answer import compose_grounded_answer, generate_grounded_answer
from app.graph.nodes.input_guard import guard_input
from app.graph.nodes.policy_guard import guard_policy
from app.graph.nodes.validate_answer import validate_answer
from app.graph.planner import build_readonly_plan, decide_next
from app.graph.routing import classify_message_intelligently, select_tool
from app.graph.evidence import build_evidence_pack
from app.graph.state import AdminGraphState
from app.memory.session_store import get_session_memory, save_session_memory
from app.tools.registry import ToolRegistry


async def run_admin_graph(session_id: str, message: str, token: str | None, registry: ToolRegistry | None = None) -> AdminGraphState:
    run_id = str(uuid4())
    actor = verify_admin_jwt(token)
    guard_input(message)

    memory = get_session_memory(session_id)
    tool_registry = registry or ToolRegistry()
    classification = await classify_message_intelligently(
        message,
        _memory_summary(memory),
        available_tools=tool_registry.available_tool_names() if hasattr(tool_registry, "available_tool_names") else None,
    )
    intent = _resolve_follow_up_intent(classification.intent, classification.questionType, memory)
    question_type = classification.questionType
    primary_tool = select_tool(intent)
    plan = build_readonly_plan(intent, message, question_type)
    tool_calls = []
    trace_steps = [
        {
            "step": 0,
            "node": "planner",
            "tool": primary_tool,
            "reason": f"planned {len(plan)} read-only tool call(s)",
            "decision": "CONTINUE" if plan else "FINAL_ANSWER",
        }
    ]
    seen_calls: set[tuple[str, tuple[tuple[str, str], ...]]] = set()
    partial = False

    for index, planned in enumerate(plan, start=1):
        call_key = (planned.tool, tuple(sorted((key, str(value)) for key, value in planned.args.items())))
        repeated = call_key in seen_calls
        decision = decide_next(len(tool_calls), len(plan), repeated)
        if decision == "LIMIT_REACHED":
            partial = True
            trace_steps.append(
                {
                    "step": index,
                    "node": "decide_next",
                    "tool": planned.tool,
                    "reason": "tool call limit or repeated call reached",
                    "decision": decision,
                }
            )
            break

        seen_calls.add(call_key)
        guard_policy(actor, planned.tool)
        try:
            result, source = await tool_registry.execute(planned.tool, token or "", planned.args)
        except TimeoutError as exc:
            raise HTTPException(
                status_code=status.HTTP_504_GATEWAY_TIMEOUT,
                detail="Admin Copilot tool timed out",
            ) from exc
        tool_calls.append(
            {
                "tool": planned.tool,
                "args": planned.args,
                "result": result,
                "source": source,
                "reason": planned.reason,
            }
        )
        trace_steps.append(
            {
                "step": index,
                "node": "observe",
                "tool": planned.tool,
                "reason": planned.reason,
                "observation": _summarize_observation(result),
                "decision": decide_next(len(tool_calls), len(plan), False),
            }
        )

    if not tool_calls:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="No read-only tool could be executed")

    evidence_pack = build_evidence_pack(
        message=message,
        intent=intent,
        question_type=question_type,
        tool_calls=tool_calls,
        memory=memory,
    )
    primary_call = _primary_answer_call(tool_calls)
    reply, warnings, numbers = await compose_grounded_answer(evidence_pack, primary_call)
    if len(tool_calls) > 1:
        numbers.append(f"contextSources={len(tool_calls) - 1}")
        reply += f" Tôi cũng đã đối chiếu thêm {len(tool_calls) - 1} nguồn dữ liệu để bổ sung ngữ cảnh."
    if partial:
        warnings.append("Kết quả là partial answer vì agent đã chạm giới hạn bước hoặc phát hiện tool lặp.")
    try:
        validate_answer(reply, numbers, question_type=question_type, evidence_missing=_evidence_missing(primary_call["result"]))
    except HTTPException:
        reply, warnings, numbers = generate_grounded_answer(
            evidence_pack.intent,
            primary_call["tool"],
            primary_call["result"],
            message,
            evidence_pack.question_type,
        )
        validate_answer(reply, numbers, question_type=question_type, evidence_missing=_evidence_missing(primary_call["result"]))
    save_session_memory(session_id, _next_memory(message, intent, question_type, primary_call, numbers))

    return AdminGraphState(
        session_id=session_id,
        message=message,
        token="[REDACTED]",
        actor=actor,
        intent=intent,
        question_type=question_type,
        selected_tool=primary_call["tool"],
        tool_args=primary_call["args"],
        tool_result=primary_call["result"],
        tool_source=primary_call["source"],
        tool_calls=tool_calls,
        react_steps=trace_steps,
        partial=partial,
        reply=reply,
        warnings=warnings,
        grounded_numbers=numbers,
        run_id=run_id,
    )


def _summarize_observation(result: object) -> str:
    if isinstance(result, list):
        return f"rows={len(result)}"
    if isinstance(result, dict):
        if isinstance(result.get("content"), list):
            return f"rows={len(result['content'])}, totalElements={result.get('totalElements')}"
        numeric_keys = [key for key, value in result.items() if isinstance(value, (int, float)) and not isinstance(value, bool)]
        if numeric_keys:
            return ", ".join(f"{key}={result[key]}" for key in numeric_keys[:4])
        return f"keys={len(result)}"
    return "result=available"


def _primary_answer_call(tool_calls: list[dict]) -> dict:
    for call in tool_calls:
        if call["tool"] == "get_revenue_breakdown":
            return call
    for call in tool_calls:
        if call["tool"] != "get_ai_data_freshness":
            return call
    return tool_calls[0]


def _evidence_missing(result: object) -> bool:
    return isinstance(result, dict) and (
        result.get("breakdownAvailable") is False or result.get("trendAvailable") is False
    )


def _memory_summary(memory: dict) -> str | None:
    if not memory:
        return None
    topic = memory.get("lastTopic")
    question = memory.get("lastQuestion")
    return f"lastTopic={topic}; lastQuestion={question}"


def _resolve_follow_up_intent(intent: str, question_type: str, memory: dict) -> str:
    if intent == "UNKNOWN" and question_type in {"FOLLOW_UP", "EXPLANATION", "COMPARISON", "DIAGNOSIS"}:
        previous = memory.get("lastTopic")
        if previous:
            return previous
    return intent


def _next_memory(message: str, intent: str, question_type: str, primary_call: dict, numbers: list[str]) -> dict:
    return {
        "lastTopic": intent,
        "lastQuestionType": question_type,
        "lastQuestion": message,
        "lastTool": primary_call["tool"],
        "lastMetrics": numbers[:8],
    }
