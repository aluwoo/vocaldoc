from fastapi import FastAPI
from pydantic import BaseModel

from api.routes import router as analysis_router


app = FastAPI(title="VocalDoc Voice Analysis Service")


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str


@app.get("/health", response_model=HealthResponse)
async def health_check():
    return HealthResponse(status="ok", service="vocaldoc-analysis", version="1.0.0")


@app.get("/")
async def root():
    return {"message": "VocalDoc Voice Analysis Service"}


app.include_router(analysis_router)
