import logging

from app.config.settings import settings
from app.services.redaction_service import redact_value


def setup_logging() -> None:
    logging.basicConfig(level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO))


def get_logger(name: str) -> logging.Logger:
    return logging.getLogger(name)


def log_run(logger: logging.Logger, run: dict) -> None:
    if settings.OBSERVABILITY_ENABLED:
        logger.info("admin-copilot-run %s", redact_value(run))
