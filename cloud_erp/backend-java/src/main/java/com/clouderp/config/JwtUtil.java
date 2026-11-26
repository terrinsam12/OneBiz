package com.clouderp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret:cloud-erp-secure-jwt-secret-key-32bytes-for-sha256-production-msme}") String secret,
            @Value("${jwt.expiration.ms:86400000}") long jwtExpirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", principal.getId());
        claims.put("companyId", principal.getCompanyId());
        claims.put("companyName", principal.getCompanyName());
        claims.put("role", principal.getRole());

        return Jwts.builder()
                .subject(principal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getCompanyIdFromToken(String token) {
        Object cid = getClaims(token).get("companyId");
        if (cid instanceof Number) {
            return ((Number) cid).longValue();
        }
        return null;
    }

    public Long getUserIdFromToken(String token) {
        Object uid = getClaims(token).get("userId");
        if (uid instanceof Number) {
            return ((Number) uid).longValue();
        }
        return null;
    }
}
