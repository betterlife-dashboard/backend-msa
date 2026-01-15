package com.betterlife.auth.util;

import com.betterlife.auth.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessValidityInMilliseconds;
    private final long refreshValidityInMilliseconds;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.accessExpiration}") long accessValidityInMilliseconds,
                       @Value("${jwt.refreshExpiration}") long refreshValidityInMilliseconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessValidityInMilliseconds = accessValidityInMilliseconds;
        this.refreshValidityInMilliseconds = refreshValidityInMilliseconds;
    }

    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId, String sessionId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(sessionId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("잘못된 형식의 토큰입니다.");
        }
    }

    public String getSessionId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("잘못된 형식의 토큰입니다.");
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("잘못된 형식의 토큰입니다.");
        }
    }
}

