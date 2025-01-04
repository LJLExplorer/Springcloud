package com.ljl.consumer.listener;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.ljl.consumer.utils.constant.LimterConstant.CIRCUIT_BREAKER_NAME;

//监听熔断器状态
@Component
public class CircuitBreakerEventListener {

    private final Logger logger = LoggerFactory.getLogger(CircuitBreakerEventListener.class);

    public CircuitBreakerEventListener(CircuitBreakerRegistry circuitBreakerRegistry) {
        // 获取注册的熔断器实例
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        // 注册监听器
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.debug("服务器状态: " + event);
                })
                .onFailureRateExceeded(event -> {
                    logger.debug("故障率超标: " + event);
                })
                .onCallNotPermitted(event -> {
                    logger.debug("超过调用失败次数，服务被禁止调用: " + event);
                })
                .onError(event -> {
                    logger.error("服务调用失败: " + event);
                })
                .onSuccess(event -> {
                    logger.debug("服务调用成功: " + event);
                });
    }
}
