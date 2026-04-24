from __future__ import annotations

import json
import logging
import re
from decimal import Decimal, InvalidOperation
from typing import Any

from ollama import AsyncClient, ResponseError

from .config import Settings
from .models import Category, CurrencyCode, ParsedProduct

log = logging.getLogger(__name__)


OLLAMA_OUTPUT_SCHEMA: dict[str, Any] = {
    "type": "object",
    "properties": {
        "title": {"type": ["string", "null"]},
        "description": {"type": ["string", "null"]},
        "price": {"type": ["string", "null"]},
        "currency": {
            "type": ["string", "null"],
            "enum": [*(c.value for c in CurrencyCode), None],
        },
        "category": {
            "type": ["string", "null"],
            "enum": [*(c.value for c in Category), None],
        },
    },
    "required": ["title", "description", "price", "currency", "category"],
    "additionalProperties": False,
}


SYSTEM_PROMPT = """You are a strict JSON extractor for an online marketplace.
Given a user's message describing something they want to sell, extract the
product attributes and return them as JSON matching the provided schema.

HARD RULES:
- Return JSON only. No prose, no markdown, no commentary.
- Every field value must be either (a) an actual extracted value, or (b) null.
- NEVER write explanations into a field. Do not write things like
  "not specified", "unknown", "no price given", "n/a", or similar.
  In those cases the value MUST be null.

FIELD RULES:
- `title`: a concise product headline. Do not invent brand names.
- `description`: a longer description if the user wrote one; otherwise null.
- `price`: the numeric price as digits only (e.g. "27000", "199.99").
  No currency symbol, no thousand separators, no words. null if not stated.
- `currency`: one of "UAH", "USD", "EUR". Infer from symbols
  ($ -> USD, € -> EUR, ₴/грн/uah -> UAH) or words in the message.
  null if ambiguous.
- `category`: MUST be one of the predefined enum values below. Pick the best
  fit. If the message is product-ish but doesn't fit any category, use
  "OTHER". null only when the message carries no sellable item at all.

Category guide (choose exactly one):
  ELECTRONICS  - phones, laptops, tablets, cameras, audio, TVs, gadgets, accessories
  CLOTHING     - apparel, shoes, bags, accessories you wear
  HOME         - kitchenware, decor, textiles, tools, garden
  FURNITURE    - sofas, beds, tables, chairs, shelves, wardrobes
  APPLIANCES   - fridges, washers, ovens, vacuum cleaners, small kitchen appliances
  VEHICLES     - cars, motorbikes, bicycles, scooters, parts
  REAL_ESTATE  - apartments, houses, land, commercial space
  BOOKS        - books, textbooks, comics, magazines
  SPORTS       - sports gear, fitness equipment, outdoor
  TOYS         - toys, games, board games, kids' items
  BEAUTY       - cosmetics, perfume, personal care
  HEALTH       - medical devices, supplements, hygiene
  PETS         - pet food, accessories, animals
  SERVICES     - someone offering a service (cleaning, tutoring, repair, etc.)
  JOBS         - job offers / hiring posts
  OTHER        - fallback when nothing above fits

- Do not translate the title or description: preserve the original language.
"""


class LlmParser:
    """Thin async wrapper around the Ollama client."""

    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._client = AsyncClient(
            host=settings.ollama_host,
            timeout=settings.ollama_timeout_s,
        )

    async def parse(self, message: str) -> ParsedProduct:
        truncated = message[: self._settings.max_message_chars]

        try:
            response = await self._client.chat(
                model=self._settings.ollama_model,
                messages=[
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user", "content": truncated},
                ],
                format=OLLAMA_OUTPUT_SCHEMA,
                options={"temperature": self._settings.temperature},
            )
        except ResponseError as exc:
            log.error("Ollama responded with error: %s", exc)
            raise

        content = response["message"]["content"]
        try:
            data: dict[str, Any] = json.loads(content)
        except json.JSONDecodeError as exc:
            log.error("Ollama returned non-JSON content despite schema: %r", content)
            raise ValueError("Model returned invalid JSON") from exc

        return ParsedProduct.model_validate(_normalize(data))


_PRICE_RE = re.compile(r"[+-]?\d+(?:[.,]\d+)?")


def _normalize(data: dict[str, Any]) -> dict[str, Any]:
    """Post-process the model's output before handing it to Pydantic.

    Small local models sometimes stuff explanations into string-typed fields
    (e.g. "there is no price, set to null" ends up in ``price``) or emit
    empty strings instead of ``null``. We clean those up so the endpoint
    stays useful instead of 500-ing on edge cases.
    """

    for key in ("title", "description", "price", "currency", "category"):
        value = data.get(key)
        if isinstance(value, str) and not value.strip():
            data[key] = None

    # Price: try to pull a numeric token out of whatever the model produced.
    raw_price = data.get("price")
    if raw_price is None:
        pass
    elif isinstance(raw_price, (int, float)):
        data["price"] = Decimal(str(raw_price))
    else:
        match = _PRICE_RE.search(str(raw_price))
        if not match:
            data["price"] = None
        else:
            token = match.group(0).replace(",", ".")
            try:
                data["price"] = Decimal(token)
            except InvalidOperation:
                data["price"] = None

    return data
