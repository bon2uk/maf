"""Request/response schemas for the LLM parsing endpoint."""

from decimal import Decimal
from enum import Enum

from pydantic import BaseModel, Field


class CurrencyCode(str, Enum):
    """Mirrors ``com.maf.product.model.CurrencyCode`` in the product service."""

    UAH = "UAH"
    USD = "USD"
    EUR = "EUR"


class Category(str, Enum):

    ELECTRONICS = "ELECTRONICS"
    CLOTHING = "CLOTHING"
    HOME = "HOME"
    FURNITURE = "FURNITURE"
    APPLIANCES = "APPLIANCES"
    VEHICLES = "VEHICLES"
    REAL_ESTATE = "REAL_ESTATE"
    BOOKS = "BOOKS"
    SPORTS = "SPORTS"
    TOYS = "TOYS"
    BEAUTY = "BEAUTY"
    HEALTH = "HEALTH"
    PETS = "PETS"
    SERVICES = "SERVICES"
    JOBS = "JOBS"
    OTHER = "OTHER"


class ParseRequest(BaseModel):
    message: str = Field(
        ...,
        min_length=1,
        description="Free-form user message to extract product attributes from.",
    )


class ParsedProduct(BaseModel):

    title: str | None = Field(
        None, description="Short product name or headline, <= 120 chars."
    )
    description: str | None = Field(
        None, description="Longer free-form product description."
    )
    price: Decimal | None = Field(
        None, description="Numeric price. No currency symbol, no thousand separators."
    )
    currency: CurrencyCode | None = Field(
        None, description="ISO-4217 currency code; one of UAH, USD, EUR."
    )
    category: Category | None = Field(
        None,
        description=(
            "Product category. MUST be one of the predefined Category enum "
            "values. If none fits, use OTHER. Use null only when the message "
            "carries no product-ish content at all."
        ),
    )


class ParseResponse(BaseModel):
    parsed: ParsedProduct
    model: str = Field(..., description="Ollama model identifier used for parsing.")
