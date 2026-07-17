from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    CHATBOT_ENV: str = "development"
    CHATBOT_HOST: str = "0.0.0.0"
    CHATBOT_PORT: int = 8002

    MODEL_PROVIDER: str = "anthropic"
    MODEL_NAME: str = "claude-sonnet-4-6"

    DB_READ_URL: str = ""
    DB_WRITE_URL: str = ""       # Optional write connection for durable chat history
    BACKEND_API_BASE_URL: str = "http://localhost:8080"
    REDIS_URL: str = "redis://localhost:6379"
    SESSION_TTL_SECONDS: int = 3600          # Phase 9: Redis session TTL
    OBSERVABILITY_ENABLED: bool = True       # Phase 9: structured trace/tool logging
    EVALUATION_LOGGING_ENABLED: bool = True  # Phase 9: evaluation event logging

    CORS_ALLOWED_ORIGINS: str = "http://localhost:3000,http://localhost:3001"
    LOG_LEVEL: str = "INFO"
    ANTHROPIC_API_KEY: str = ""
    OPENAI_API_KEY: str = ""        # Used for LLM (when MODEL_PROVIDER=openai) AND embeddings

    # Vector search — OpenAI embeddings (independent of MODEL_PROVIDER)
    EMBEDDING_MODEL: str = "text-embedding-3-small"
    EMBEDDING_DIMS: int = 1536

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
