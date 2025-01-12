package com.ljl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 自定义根据ip进行限流
 */
@Configuration
public class RateLimiterConfig {

    private final static Logger logger = LoggerFactory.getLogger(RateLimiterConfig.class);

    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(getClientIp(exchange));
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwardedHeader = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        logger.debug("forwardedHeader: " + forwardedHeader);
        if (forwardedHeader != null) {
            return forwardedHeader.split(",")[0];
        }
        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
    }
}
