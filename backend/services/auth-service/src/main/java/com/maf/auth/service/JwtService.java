package com.maf.auth.service;

import com.maf.auth.entity.Role;
import com.maf.auth.entity.User;
import io.jsonwebtoken.Claims;
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
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;


@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    public String generateToken(User user) {
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .claim("roles", roles)
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(getSignignKey(), SignatureAlgorithm.HS512) // сучасний метод
                .compact();
    }


    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignignKey()) // сучасний метод
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    private Key getSignignKey() {
        assert secretKey != null;
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
}
public boolean isTokenValid(String token, User user) {
    final String username = extractUsername(token);

    return (username.equals(user.getEmail()) && !isTokenExpired(token));
}
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
