package com.betterlife.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    @Test
    void apply_addsUserIdHeaderOnValidToken() {
        String secret = "01234567890123456789012345678901";
        String token = Jwts.builder()
                .subject("123")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        JwtAuthFilter filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "secret", secret);

        MockServerHttpRequest request = MockServerHttpRequest.get("/todo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        StepVerifier.create(filter.apply(config).filter(exchange, ex -> {
            captured.set(ex);
            return Mono.empty();
        })).verifyComplete();

        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("123");
    }

    @Test
    void apply_rejectsWhenMissingAuthorization() {
        JwtAuthFilter filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "secret", "01234567890123456789012345678901");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/todo").build()
        );

        // 가짜 체인 생성
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        // 반환값이 null일 경우 에러 발생할 수 있으므로 반환값만 지정
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        StepVerifier.create(filter.apply(config).filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }
}
