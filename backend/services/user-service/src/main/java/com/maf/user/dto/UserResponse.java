package com.maf.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class LoginRequest {
    @Getter
    @Email(message = "Email is not valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;
    @Getter
    @NotBlank(message = "Password cannot be blank")
    private String password;

}
