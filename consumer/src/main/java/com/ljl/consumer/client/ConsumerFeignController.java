package com.ljl.consumer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.ljl.consumer.utils.constant.HttpConstant.CODE;
import static com.ljl.consumer.utils.constant.HttpConstant.MESSAGE;
import static com.ljl.consumer.utils.constant.LimterConstant.*;

@RestController
@RequestMapping("/cf")
public class ConsumerFeignController {

    private final Logger logger = LoggerFactory.getLogger(ConsumerFeignController.class);

    @Resource
    private UserClient userClient;

    @GetMapping("/{id}")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @RateLimiter(name = RATE_LIMITER_NAME)
    @Retry(name = RETRY_NAME, fallbackMethod = "fallback")
    public String queryById(@PathVariable("id") Long id) {
        if(id < 0){
            logger.error("id不能为负");
            throw new RuntimeException();
        }
        return userClient.queryById(id);
    }

    // 回退方法，当熔断器触发时调用
    public String fallback(Long id, Throwable t) {
        logger.error("服务不可用，进入回退方法: {}", t.getMessage());
        Map<String, Object> map = new HashMap<>();
        if (t instanceof RequestNotPermitted){
            map.put(CODE, 429);
            logger.error( "请求过于频繁，请稍后再试。");
            map.put(MESSAGE, "请求过于频繁，请稍后再试。");
        }
        else{
            map.put(CODE, 500);
            map.put(MESSAGE, "服务暂时不可用，请稍后再试。");
            logger.error("服务暂时不可用，请稍后再试。");
        }
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
