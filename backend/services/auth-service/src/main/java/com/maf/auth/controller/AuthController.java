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
 try {
        User user = authService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new UserResponse(user.getId().toString(), user.getEmail()));
    } catch (Exception e) {
        e.printStackTrace(); 
        return ResponseEntity.status(500).body(null);
    }
    }
}