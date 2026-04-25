package com.maf.parser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParseMessageRequest(
        @NotBlank
        @Size(max = 4000, message = "message must be at most 4000 characters")
        String message
) {
}
