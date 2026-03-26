package com.maf.auth.service;

import com.maf.auth.dto.LoginRequest;
import com.maf.auth.dto.LoginResponse;
import com.maf.auth.dto.RegisterRequest;
import com.maf.auth.entity.Role;
import com.maf.auth.entity.User;
import com.maf.auth.exception.UserAlreadyExistsException;
import com.maf.auth.kafka.UserEventProducer;
import com.maf.auth.repository.RoleRepository;
import com.maf.auth.repository.UserRepository;
import com.maf.common.event.UserRegisteredEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final UserEventProducer userEventProducer;

    @Transactional
    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email " + registerRequest.getEmail() + " already exists"
            );
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .status(User.Status.ACTIVE)
                .createdAt(Instant.now())
                .roles(new HashSet<>(Set.of(userRole))) // додаємо роль одразу
                .build();

        userRepository.save(user);
        userEventProducer.publishUserRegisteredEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), Instant.now()));

        return user;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new LoginResponse(token, refreshToken);
    }
}