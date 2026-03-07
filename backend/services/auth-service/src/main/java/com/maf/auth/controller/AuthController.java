package com.maf.auth.controller;

import com.maf.auth.entity.User;
import com.maf.auth.service.AuthService;
import com.maf.auth.dto.RegisterRequest;
import com.maf.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request.getEmail(), request.getPassword());

        // повертаємо DTO, а не entity
        UserResponse response = new UserResponse(user.getId().toString(), user.getEmail());

        return ResponseEntity.ok(response);
    }
}