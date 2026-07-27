from functools import lru_cache

from pydantic import AnyHttpUrl, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ADMIN_COPILOT_ENV: str = "development"
    ADMIN_COPILOT_HOST: str = "0.0.0.0"
    ADMIN_COPILOT_PORT: int = 8003

    CORE_BACKEND_API_BASE_URL: AnyHttpUrl = "http://localhost:8082"
    FORECASTING_API_BASE_URL: AnyHttpUrl = "http://localhost:8081"

    JWT_ACCESS_SECRET: str = "replace-with-exact-same-value-as-backend"
    JWT_ACCESS_ALGORITHM: str = "HS256"

    REDIS_URL: str = "redis://localhost:6379"
    SESSION_TTL_SECONDS: int = 3600

    MODEL_PROVIDER: str = "none"
    MODEL_NAME: str = "deterministic-readonly-v1"
    ANTHROPIC_API_KEY: str = ""
    OPENAI_API_KEY: str = ""

    MAX_AGENT_STEPS: int = 4
    MAX_TOOL_CALLS_PER_RUN: int = 6
    AGENT_TIMEOUT_SECONDS: int = 30
    TOOL_TIMEOUT_SECONDS: float = 8.0
    MAX_INPUT_CHARS: int = 4000

    READ_ONLY_MODE: bool = True
    WRITE_TOOLS_ENABLED: bool = False
    APPROVALS_ENABLED: bool = False
    APPROVAL_STORAGE_BACKEND: str = "sqlite"
    APPROVAL_SQLITE_PATH: str = "data/admin_agent_approvals.sqlite3"
    APPROVAL_DB_HOST: str = ""
    APPROVAL_DB_PORT: int = 5432
    APPROVAL_DB_NAME: str = "postgres"
    APPROVAL_DB_USERNAME: str = ""
    APPROVAL_DB_PASSWORD: str = ""
    APPROVAL_DB_SSLMODE: str = "require"

    AGENT_ORCHESTRATION_ENABLED: bool = False
    CONTROLLED_AI_JOBS_ENABLED: bool = False
    AI_JOB_REQUIRE_FRESHNESS_CHECK: bool = True
    AI_JOB_MAX_POLL_SECONDS: int = 120
    AI_JOB_POLL_INTERVAL_SECONDS: int = 3
    AI_JOB_IDEMPOTENCY_TTL_SECONDS: int = 3600
    AI_JOB_STALE_AFTER_MINUTES: int = 720
    AI_JOB_ALLOWED_DATA_SOURCES: str = "DEMO"
    PRODUCT_LOOKUP_ENABLED: bool = True
    BEST_SELLER_LOOKUP_ENABLED: bool = True
    DEFAULT_REPORT_LOOKBACK_DAYS: int = 30
    MAX_REPORT_LOOKBACK_DAYS: int = 180
    MAX_AGENT_RESULT_ROWS: int = 20

    OBSERVABILITY_ENABLED: bool = True
    EVALUATION_LOGGING_ENABLED: bool = True
    LOG_LEVEL: str = "INFO"
    CORS_ALLOWED_ORIGINS: str = "http://localhost:3001"

    RATE_LIMIT_PER_MINUTE: int = Field(default=60, ge=1)

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
