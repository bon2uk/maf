package com.maf.parser.model;

/**
 * Closed set of product categories. Must stay in sync with the Python
 * {@code app.models.Category} enum used by the llm-service — that service
 * is configured to return only these strings.
 *
 * <p>When categories move to a dedicated DB table in the product service,
 * this enum should be deleted and replaced with a reference to the shared
 * entity / DTO living in {@code libs/common}.
 */
public enum Category {
    ELECTRONICS,
    CLOTHING,
    HOME,
    FURNITURE,
    APPLIANCES,
    VEHICLES,
    REAL_ESTATE,
    BOOKS,
    SPORTS,
    TOYS,
    BEAUTY,
    HEALTH,
    PETS,
    SERVICES,
    JOBS,
    OTHER
}
