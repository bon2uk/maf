package com.maf.auth.service;

import com.maf.auth.dto.LoginRequest;
import com.maf.auth.dto.LoginResponse;
import com.maf.auth.dto.RegisterRequest;
import com.maf.auth.entity.Role;
import com.maf.auth.entity.User;
import com.maf.auth.exception.UserAlreadyExistsException;
import com.maf.auth.outbox.OutboxService;
import com.maf.auth.repository.RoleRepository;
import com.maf.auth.repository.UserRepository;
import com.maf.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String USER_REGISTERED_TOPIC = "auth.user-registered";
    private static final String USER_REGISTERED_EVENT_TYPE = "UserRegistered";
    private static final String USER_AGGREGATE_TYPE = "User";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final OutboxService outboxService;

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
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), Instant.now());
        outboxService.save(
                USER_AGGREGATE_TYPE,
                user.getId().toString(),
                USER_REGISTERED_EVENT_TYPE,
                USER_REGISTERED_TOPIC,
                event);

        return user;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new LoginResponse(token, refreshToken);
    }
}