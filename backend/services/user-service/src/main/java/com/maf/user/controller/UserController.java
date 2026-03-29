package com.maf.user.controller;

import com.maf.common.entity.CustomUserPrincipal;
import com.maf.user.dto.UpdateUserRequest;
import com.maf.user.dto.UserResponse;
import com.maf.user.entity.User;
import com.maf.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
@Slf4j

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateById(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        return ResponseEntity.ok(userService.updateUser(id, updateUserRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        if (principal == null || principal.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = userService.getUserById(principal.getUserId());

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(Authentication authentication, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        if (principal == null || principal.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = userService.updateUser(principal.getUserId(), updateUserRequest);

        return ResponseEntity.ok(UserResponse.from(user));
    }

}