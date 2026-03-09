package com.maf.auth.service;

import com.maf.auth.dto.RegisterRequest;
import com.maf.auth.entity.User;
import com.maf.auth.exception.UserAlreadyExistsException;
import com.maf.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest registerRequest) {
        System.out.println("Registering user: " + userRepository.existsByEmail(registerRequest.getEmail()));
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registerRequest.getEmail() + " already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .status(User.Status.ACTIVE)
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }
}