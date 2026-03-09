package com.maf.auth.controller;

import com.maf.auth.entity.User;
import com.maf.auth.service.AuthService;
import com.maf.auth.dto.RegisterRequest;
import com.maf.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("Registering user controller: " + request.getEmail());
        try {
            User user = authService.register(request);
            UserResponse response = new UserResponse(user.getId().toString(), user.getEmail());
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getRootCause() != null && ex.getRootCause().getMessage().contains("duplicate key")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error"));
        }
    }
}