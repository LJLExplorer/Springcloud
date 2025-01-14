package com.ljl.consumer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.consumer.domain.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.ljl.consumer.utils.constant.LimterConstant.*;


@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    private Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/user/{id}")
    // @CircuitBreaker：应用断路器，指定断路器名称和回退方法。
    // fallback(Throwable t)：当断路器打开或服务调用失败时调用的回退方法。
    // 熔断器和限流器时需要将fallbackMethod加在熔断器上,否则不走熔断器了（原因未知 试出来的）
    // 熔断器、限流器和重试都在时需要将fallbackMethod加在重试器上 否则不走重试器（原因未知 试出来的）
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @RateLimiter(name = RATE_LIMITER_NAME)
    @Retry(name = RETRY_NAME, fallbackMethod = "fallback")
    public User queryById(@PathVariable Long id) throws JsonProcessingException {
        if (id < 0) {
            logger.error("id不能为负");
            throw new RuntimeException("Invalid id");
        }

        String url = "http://user-service/user/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    @GetMapping("/root/{id}")
    public User queryRootById(@PathVariable Long id) {
        String url = "http://root-service/root/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    // 回退方法，当熔断器触发时调用
    public User fallback(Long id, Throwable t) {
        logger.error("服务不可用，进入回退方法: {}", t.getMessage());
        User user = new User();
        if( t instanceof RequestNotPermitted)
            user.setName("请求过于频繁，请稍后再试。");
        else
            user.setName("服务暂时不可用，请稍后再试。");
        return user;
    }
}