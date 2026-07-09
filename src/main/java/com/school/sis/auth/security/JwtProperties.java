package com.school.sis.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sis.jwt")
public record JwtProperties(
        String secret,
        long accessExpirationSeconds,
        long refreshExpirationSeconds
) {
}
