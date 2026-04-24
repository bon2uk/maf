# llm-service

Parses free-form user messages (e.g. "iPhone 13 Pro 128GB, синій, $650, майже новий")
into structured product attributes using a local LLM via [Ollama](https://ollama.com/).

The service is a thin FastAPI app that forwards the message to an Ollama
instance using **structured outputs**: a JSON schema is passed as the
`format` parameter so the model is constrained to return JSON matching our
Pydantic shape. No regex/string-munging fallbacks — either the model returns
valid JSON or the endpoint responds with a 4xx/5xx.

## API

### `POST /internal/llm/parse`

Request:

```json
{ "message": "Продам iPhone 13 Pro 128GB, майже новий, 27000 грн" }
```

Response:

```json
{
  "parsed": {
    "title": "iPhone 13 Pro 128GB",
    "description": "майже новий",
    "price": "27000",
    "currency": "UAH",
    "category": "ELECTRONICS"
  },
  "model": "llama3.2:3b"
}
```

Any field the model can't infer is returned as `null`.

### Category enum

`category` is a strict enum (see `app/models.py::Category`). Current values:

```
ELECTRONICS, CLOTHING, HOME, FURNITURE, APPLIANCES, VEHICLES, REAL_ESTATE,
BOOKS, SPORTS, TOYS, BEAUTY, HEALTH, PETS, SERVICES, JOBS, OTHER
```

This set is intentionally small and stable so it can be used directly as a
filter value on the product service. Later it will move to a dedicated
`categories` DB table; the uppercase labels will map 1:1 to a `code` column
there, so existing records stay valid.

### `GET /actuator/health`

Matches the shape used by the Java services so the same compose healthcheck
wrapper works.

## Configuration

All env vars are prefixed with `LLM_`:

| Variable              | Default                  | Description                                                                 |
| --------------------- | ------------------------ | --------------------------------------------------------------------------- |
| `LLM_HOST`            | `0.0.0.0`                | Bind host.                                                                  |
| `LLM_PORT`            | `8091`                   | Bind port.                                                                  |
| `LLM_OLLAMA_HOST`     | `http://ollama:11434`    | Ollama HTTP endpoint.                                                       |
| `LLM_OLLAMA_MODEL`    | `llama3.2:3b`            | Model tag. Must already be pulled on the Ollama instance.                   |
| `LLM_OLLAMA_TIMEOUT_S`| `60`                     | Request timeout to Ollama in seconds.                                       |
| `LLM_TEMPERATURE`     | `0.1`                    | Sampling temperature. Keep low for deterministic extraction.                |
| `LLM_MAX_MESSAGE_CHARS`| `4000`                  | Input messages are truncated to this length before being sent to the model. |
| `LLM_INTERNAL_TOKEN`  | _(unset)_                | If set, requests must include `X-Internal-Token: <value>`.                  |

## Running locally

```bash
# 1. Start Ollama and pull a model (first time only; the model is ~2 GB)
ollama serve &                    # or: docker run -p 11434:11434 ollama/ollama
ollama pull llama3.2:3b

# 2. Run the service
cd backend/services/llm
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
LLM_OLLAMA_HOST=http://localhost:11434 uvicorn app.main:app --reload --port 8091

# 3. Try it
curl -s -X POST http://localhost:8091/internal/llm/parse \
  -H 'Content-Type: application/json' \
  -d '{"message": "iPhone 13 Pro 128GB, $650, майже новий"}' | jq
```

## Running with docker-compose

Pre-pull the model into the `ollama` container once (the model itself is
stored in a named volume so this survives container recreation):

```bash
cd backend
docker compose up -d ollama
docker compose exec ollama ollama pull llama3.2:3b
docker compose up -d llm-service
```

From another service on the same network, call:

```
POST http://llm-service:8091/internal/llm/parse
X-Internal-Token: <matches LLM_INTERNAL_TOKEN>
```
