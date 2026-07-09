package com.school.sis.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(SisUserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of(
                        "userId", userDetails.id().toString(),
                        "email", userDetails.email(),
                        "fullName", userDetails.fullName()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.accessExpirationSeconds())))
                .signWith(signingKey)
                .compact();
    }

    public String subject(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, SisUserDetails userDetails) {
        Claims claims = claims(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(Date.from(Instant.now()));
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
