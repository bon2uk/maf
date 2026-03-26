package com.maf.auth.service;

import com.maf.auth.entity.RefreshToken;
import com.maf.auth.entity.User;
import com.maf.auth.repository.RefreshTokenRepository;
import com.maf.auth.repository.UserRepository;
import com.maf.auth.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000; // 7 днів
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshToken createRefreshToken(User user) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByUser(user);

        String token = generateSecureToken();

        if (existing.isPresent()) {
            RefreshToken refreshToken = existing.get();
            refreshToken.setToken(token);
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(refreshToken);
        } else {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setToken(token);
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(refreshToken);
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[64]; // 512 біт
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please login again.");
        }
        return token.getToken();
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByToken(String user) {
        refreshTokenRepository.deleteByToken(user);
    }
}
