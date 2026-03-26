package com.maf.auth.controller;

import com.maf.auth.dto.*;
import com.maf.auth.entity.User;
import com.maf.auth.service.AuthService;
import com.maf.auth.service.JwtService;
import com.maf.auth.service.RefreshTokenService;
import com.maf.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            UserResponse response = new UserResponse(user.getEmail(),user.getId().toString());
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = refreshTokenService.findByToken(token).get().getUser();
                    String newJwt = jwtService.generateToken(user);
                    String newRefreshToken = refreshTokenService.createRefreshToken(user).getToken();
                    return ResponseEntity.ok(new RefreshTokenResponse(newJwt, newRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token not found or expired"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = refreshTokenService.findByToken(token).get().getUser();
                    String jwt = jwtService.generateToken(user);
                    return ResponseEntity.ok(new RefreshTokenResponse(jwt, token));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token not found!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        return new UserResponse(user.getId().toString(), user.getEmail());

    }
}