from fastapi import HTTPException, status


INTERNAL_MARKERS = [
    "get_",
    "_overview",
    "api read-only",
    "tool",
    "intent",
    "groundednumbers",
    "runid",
]


def validate_answer(
    reply: str,
    grounded_numbers: list[str],
    *,
    question_type: str = "UNKNOWN",
    evidence_missing: bool = False,
) -> None:
    if not reply.strip():
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Empty copilot answer")
    if any(char.isdigit() for char in reply) and not grounded_numbers:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Ungrounded numeric answer")
    lowered = reply.lower()
    if any(marker in lowered for marker in INTERNAL_MARKERS):
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Copilot answer exposed internal implementation details")
    if question_type == "EXPLANATION" and not any(term in lowered for term in ["vì", "vi ", "because", "khac nhau", "thiếu", "thieu", "do "]):
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Explanation answer did not include a reason")
    if evidence_missing and not any(term in lowered for term in ["chưa đủ", "chua du", "thiếu dữ liệu", "thieu du lieu", "chua ket luan"]):
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Answer did not disclose insufficient evidence")
