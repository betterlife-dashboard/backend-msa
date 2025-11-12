package com.betterlife.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    public static class Config {}

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/renew")) {
                return chain.filter(exchange);
            }

            String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (header == null || !header.startsWith("Bearer ")) {
                return reject(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = Jwts.parser().verifyWith(
                                Keys.hmacShaKeyFor(secret.getBytes()))
                        .build()
                        .parseSignedClaims(header.substring(7))
                        .getPayload();

                String userId = claims.get("userId", String.class);
                exchange = exchange.mutate()
                        .request(r -> r.headers(h -> {
                            h.add("X-User-Id", userId);
                        }))
                        .build();
                return chain.filter(exchange);
            } catch (JwtException e) {
                return reject(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> reject(ServerWebExchange ex, HttpStatus s) {
        ex.getResponse().setStatusCode(s);
        return ex.getResponse().setComplete();
    }
}
