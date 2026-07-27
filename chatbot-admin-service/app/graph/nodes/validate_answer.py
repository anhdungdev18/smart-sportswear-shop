from fastapi import HTTPException, status


def validate_answer(reply: str, grounded_numbers: list[str]) -> None:
    if not reply.strip():
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Empty copilot answer")
    if any(char.isdigit() for char in reply) and not grounded_numbers:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Ungrounded numeric answer")
