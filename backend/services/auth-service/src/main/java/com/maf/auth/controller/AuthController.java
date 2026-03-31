package com.maf.auth.controller;

import com.maf.auth.dto.*;
import com.maf.auth.entity.User;
import com.maf.auth.exception.InvalidTokenException;
import com.maf.auth.service.AuthService;
import com.maf.auth.service.JwtService;
import com.maf.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        UserResponse response = new UserResponse(user.getEmail(), user.getId().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = refreshTokenService.findByToken(token).get().getUser();
                    String newJwt = jwtService.generateToken(user);
                    String newRefreshToken = refreshTokenService.createRefreshToken(user).getToken();
                    return ResponseEntity.ok(new RefreshTokenResponse(newJwt, newRefreshToken));
                })
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or expired"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = refreshTokenService.findByToken(token).get().getUser();
                    String jwt = jwtService.generateToken(user);
                    return ResponseEntity.ok(new RefreshTokenResponse(jwt, token));
                })
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new BadCredentialsException("User not authenticated");
        }

        return new UserResponse(user.getId().toString(), user.getEmail());
    }
}