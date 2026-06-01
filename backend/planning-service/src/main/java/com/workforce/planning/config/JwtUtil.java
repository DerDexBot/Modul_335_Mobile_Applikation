package com.workforce.planning.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/** Utility-Klasse für die Verarbeitung und Validierung von JWT-Tokens. */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Extrahiert den Benutzernamen aus dem JWT-Token. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extrahiert die Rolle aus dem JWT-Token. */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /** Prüft, ob ein JWT-Token gültig ist. */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
