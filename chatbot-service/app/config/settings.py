from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    CHATBOT_ENV: str = "development"
    CHATBOT_HOST: str = "0.0.0.0"
    CHATBOT_PORT: int = 8002

    MODEL_PROVIDER: str = "anthropic"
    MODEL_NAME: str = "claude-sonnet-4-6"

    DB_READ_URL: str = ""
    BACKEND_API_BASE_URL: str = "http://localhost:8080"
    REDIS_URL: str = "redis://localhost:6379"

    LOG_LEVEL: str = "INFO"
    ANTHROPIC_API_KEY: str = ""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
