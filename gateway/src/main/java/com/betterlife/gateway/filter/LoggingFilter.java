package com.betterlife.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("[LoggingFilter] incoming: " + exchange.getRequest().getMethod() + " " + exchange.getRequest().getURI());
        long start = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getRawPath();

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    long elapsed = System.currentTimeMillis() - start;
                    System.out.println("Gateway Log - " + path + " took " + elapsed + "ms");
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
