package com.maf.common.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Instant occurredAt
) {
}