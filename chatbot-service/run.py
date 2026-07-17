import uvicorn
from app.config.settings import settings

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=settings.CHATBOT_HOST,
        port=settings.CHATBOT_PORT,
        reload=True,
    )
