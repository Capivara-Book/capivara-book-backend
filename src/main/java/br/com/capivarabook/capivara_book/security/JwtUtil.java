package br.com.capivarabook.capivara_book.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


// ── JwtUtil ────────────────────────────────────────────────────
// Gera e valida tokens JWT.
// Claims incluídos: sub (email), role (ex: ROLE_ADMIN), id (id_user)
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ── gerarToken() ───────────────────────────────────────────
    public String gerarToken(Long userId, String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("id",   userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key())
                .compact();
    }

    // ── extrairEmail() ─────────────────────────────────────────
    public String extrairEmail(String token) {
        return claims(token).getSubject();
    }

    // ── extrairRole() ──────────────────────────────────────────
    public String extrairRole(String token) {
        return claims(token).get("role", String.class);
    }

    // ── isTokenValido() ────────────────────────────────────────
    public boolean isTokenValido(String token) {
        try {
            claims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
