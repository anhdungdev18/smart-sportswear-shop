import asyncio

import pytest
from fastapi import HTTPException

from app.graph.admin_graph import run_admin_graph
from tests.helpers import make_token


class TimeoutRegistry:
    async def execute(self, name, token, args):
        raise TimeoutError("boom")


def test_tool_timeout_returns_504():
    with pytest.raises(HTTPException) as exc:
        asyncio.run(run_admin_graph("s1", "Ton kho co risk nao?", make_token("ADMIN"), registry=TimeoutRegistry()))

    assert exc.value.status_code == 504
