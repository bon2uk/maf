"""FastAPI entrypoint for the LLM parsing service."""

from __future__ import annotations

import hmac
import logging
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, Header, HTTPException, status
from ollama import ResponseError

from .config import Settings, get_settings
from .models import ParseRequest, ParseResponse
from .parser import LlmParser

log = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    app.state.parser = LlmParser(settings)
    log.info(
        "llm-service started (model=%s, ollama=%s)",
        settings.ollama_model,
        settings.ollama_host,
    )
    yield


app = FastAPI(
    title="maf-llm-service",
    description="Parses free-form user messages into structured product attributes via Ollama.",
    version="0.1.0",
    lifespan=lifespan,
)


def _require_internal_token(
    settings: Settings = Depends(get_settings),
    x_internal_token: str | None = Header(default=None, alias="X-Internal-Token"),
) -> None:

    if settings.internal_token is None:
        return
    provided = x_internal_token or ""
    if not hmac.compare_digest(provided, settings.internal_token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing X-Internal-Token",
        )


@app.get("/actuator/health", tags=["health"])
async def health() -> dict[str, str]:
    """Matches the health endpoint shape used by the Java services."""
    return {"status": "UP"}


@app.post(
    "/internal/llm/parse",
    response_model=ParseResponse,
    dependencies=[Depends(_require_internal_token)],
    tags=["llm"],
)
async def parse_message(
    request: ParseRequest,
    settings: Settings = Depends(get_settings),
) -> ParseResponse:
    parser: LlmParser = app.state.parser
    try:
        parsed = await parser.parse(request.message)
    except ResponseError as exc:
        log.exception("Upstream Ollama error")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Ollama error: {exc}",
        ) from exc
    except ValueError as exc:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(exc),
        ) from exc

    return ParseResponse(parsed=parsed, model=settings.ollama_model)
