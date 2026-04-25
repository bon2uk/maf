package com.maf.parser.model;

/**
 * Mirrors {@code com.maf.product.model.CurrencyCode} and the Python
 * {@code app.models.CurrencyCode} in the llm-service.
 *
 * <p>Kept as a local enum for now to avoid a hard dependency on the product
 * module; once a shared catalogue enum lands in {@code libs/common} the
 * three copies should be collapsed into one.
 */
public enum CurrencyCode {
    UAH, USD, EUR
}
