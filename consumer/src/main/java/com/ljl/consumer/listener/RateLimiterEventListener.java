package com.ljl.consumer.listener;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.ljl.consumer.utils.constant.LimterConstant.RATE_LIMITER_NAME;

@Component
public class RateLimiterEventListener {

    Logger logger = LoggerFactory.getLogger(RateLimiterEventListener.class);

    public RateLimiterEventListener(RateLimiterRegistry rateLimiterRegistry) {
        rateLimiterRegistry.rateLimiter(RATE_LIMITER_NAME).getEventPublisher()
                .onSuccess(event -> {
                    logger.debug("成功通过限流器，限流器状态关闭: " + event);
                }).onFailure(event -> {
                    logger.error("请求过多，限流器开启" + event);
                });
    }
}
