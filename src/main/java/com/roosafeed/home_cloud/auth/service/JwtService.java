package com.roosafeed.home_cloud.auth.service;

import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final AppProperties appProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                appProperties.getSecurity().getJwt().getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(
                appProperties.getSecurity().getJwt().getTokenTtlHours() * 3600L
        );

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(appProperties.getSecurity().getJwt().getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .signWith(getSigningKey())
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }
}
