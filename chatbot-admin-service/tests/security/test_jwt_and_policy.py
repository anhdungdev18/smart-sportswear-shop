import pytest
from fastapi import HTTPException

from app.auth.jwt_verifier import verify_admin_jwt
from app.graph.nodes.input_guard import guard_input
from app.graph.nodes.policy_guard import guard_policy
from tests.helpers import make_token


def _token(role: str) -> str:
    return make_token(role)


def test_verifier_reads_actor_from_jwt():
    actor = verify_admin_jwt(_token("ADMIN"))

    assert actor.actor_id == "11111111-1111-1111-1111-111111111111"
    assert actor.role == "ADMIN"


def test_non_admin_role_is_blocked():
    actor = verify_admin_jwt(_token("CUSTOMER"))

    with pytest.raises(HTTPException) as exc:
        guard_policy(actor, "get_inventory_risks")

    assert exc.value.status_code == 403


def test_sql_like_prompt_is_blocked():
    with pytest.raises(HTTPException):
        guard_input("drop table users")
