"""Runtime configuration for the LLM service.

All values can be overridden via environment variables (see README). Defaults
are tuned for the docker-compose setup where an Ollama instance runs in a
sibling container named ``ollama``.
"""

from functools import lru_cache

from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="LLM_", case_sensitive=False)

    # Server
    host: str = "0.0.0.0"
    port: int = 8091

    # Ollama
    ollama_host: str = "http://ollama:11434"
    ollama_model: str = "llama3.2:3b"
    ollama_timeout_s: float = 60.0

    internal_token: str | None = None

    # LLM behaviour knobs
    temperature: float = 0.1
    max_message_chars: int = 4000

    @field_validator("internal_token", mode="before")
    @classmethod
    def _blank_token_is_none(cls, v: str | None) -> str | None:
        """Treat empty/whitespace env values as unset.

        docker-compose expands ``${LLM_INTERNAL_TOKEN:-}`` to an empty string
        when the var is not defined, which would otherwise activate the guard
        with a zero-length expected token and reject every request.
        """

        if isinstance(v, str) and not v.strip():
            return None
        return v


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
