package com.maf.auth.dto;
import jakarta.validation.constraints.Email;

import lombok.Data;

@Data
public class RegisterRequest {
    @Email
    private String email;
    private String password;
}