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
