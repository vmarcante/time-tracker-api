package com.vmarcante.time_tracker.core.infraestructure.user.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.JwtPort;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAdapter implements JwtPort {

    @Value("${jwt.secret:time-tracker-secret-key-change-in-production}")
    private String secret;

    @Value("${jwt.access-token.expiration:300000}")
    private Long accessTokenExpirationMs; // 5 min default

    @Value("${jwt.refresh-token.expiration:604800000}")
    private Long refreshTokenExpirationMs; // 7 days default

    @Value("${jwt.issuer:time-tracker-api}")
    private String issuer;

    @Value("${jwt.audience:time-tracker-client}")
    private String audience;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(UserAuth userAuth) {
        return generateToken(userAuth, accessTokenExpirationMs, "access");
    }

    @Override
    public String generateRefreshToken(UserAuth userAuth) {
        return generateToken(userAuth, refreshTokenExpirationMs, "refresh");
    }

    private String generateToken(UserAuth userAuth, Long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        String jti = UUID.randomUUID().toString();

        // Get locale from Person if available, otherwise default to "pt"
        String locale = "pt";
        if (userAuth.getPerson() != null && userAuth.getPerson().getLocale() != null) {
            locale = userAuth.getPerson().getLocale();
        }

        return Jwts.builder()
                .subject(userAuth.getId().toString())
                .claim("username", userAuth.getUsername())
                .claim("seqId", userAuth.getSeqId())
                .claim("type", tokenType)
                .claim("locale", locale)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .notBefore(now)
                .expiration(expiryDate)
                .id(jti)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    @Override
    public Integer extractSeqId(String token) {
        Claims claims = parseToken(token);
        return claims.get("seqId", Integer.class);
    }

    @Override
    public Long getExpirationTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime();
    }

    @Override
    public String extractTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    @Override
    public String extractIssuer(String token) {
        Claims claims = parseToken(token);
        return claims.getIssuer();
    }

    @Override
    public String extractAudience(String token) {
        Claims claims = parseToken(token);
        return claims.getAudience().stream().findFirst().orElse(null);
    }

    @Override
    public String extractJti(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    @Override
    public Date extractNotBefore(String token) {
        Claims claims = parseToken(token);
        return claims.getNotBefore();
    }

    @Override
    public String extractLocale(String token) {
        Claims claims = parseToken(token);
        return claims.get("locale", String.class);
    }
}
