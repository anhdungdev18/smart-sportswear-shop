import uvicorn

from app.config.settings import settings


if __name__ == "__main__":
    uvicorn.run("app.main:app", host=settings.ADMIN_COPILOT_HOST, port=settings.ADMIN_COPILOT_PORT, reload=True)
