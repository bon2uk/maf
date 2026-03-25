package com.maf.user.dto;

import com.maf.user.model.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse (
   UUID id,
   String email,
   String firstName,
   String lastName,
   UserStatus status,
   Instant createdAt,
   Instant updatedAt
){}
