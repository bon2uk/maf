package com.maf.auth.service;

import com.maf.auth.entity.User;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;


@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // сучасний метод
                .compact();
    }


    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // сучасний метод
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    private Key getSigningKey() {
        assert secretKey != null;
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
}
}
