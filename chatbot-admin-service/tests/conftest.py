from __future__ import annotations

import os

os.environ.setdefault("APPROVAL_STORAGE_BACKEND", "sqlite")
os.environ.setdefault("APPROVAL_SQLITE_PATH", "data/test_admin_agent_approvals.sqlite3")
os.environ["MODEL_PROVIDER"] = "none"
os.environ["OPENAI_API_KEY"] = ""
os.environ["ANTHROPIC_API_KEY"] = ""
