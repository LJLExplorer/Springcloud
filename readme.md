

# Springboot3+Springcloud2023.0.4学习及踩坑记录

Spingboot3.2.12版本，对应springCloud2023.0.4，

springboot与springcloud版本对应：https://spring.io/projects/spring-cloud#overview

Spingboot3.4.1版本与idea中的gradle插件冲突无法启动，Spingboot3.3.7版本与eureka server冲突，无法启动

![image-20250101203911539](./image/image-20250101203911539.png)

## 负载均衡

springboot3中ribbon（负责负载均衡的）被弃用，使用spring-cloud-starter-loadbalancer进行负载均衡，修改负载策略只能通过配置类修改

pom

```
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
```

CustomLoadBalancerConfiguration

```
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class CustomLoadBalancerConfiguration {

    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment,
                                                            LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(loadBalancerClientFactory
                .getLazyProvider(name, ServiceInstanceListSupplier.class),
                name);
    }
}
```

ConsumerApplication

```
package com.ljl.consumer;

import com.ljl.consumer.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClient(value = "user-service", configuration = CustomLoadBalancerConfiguration.class)
public class ConsumerApplication {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}

```

value是被负载均衡的名，configuration就是对应写的类

修改策略教程：https://www.cnblogs.com/itxiaoshen/p/16247702.html

 [微服务生态组件之Spring Cloud LoadBalancer详解和源码分析 - itxiaoshen - 博客园.html](Notes/Spring Cloud LoadBalancer详解/微服务生态组件之Spring Cloud LoadBalancer详解和源码分析 - itxiaoshen - 博客园.html) 

测试时开了两个user-service，复制后改vm运行参数 `-Dport=9002` (意思就是更换端口运行第二个user-service)

## Resilience4j 可做熔断器和限流器：

Hystrix也被弃用，推荐使用**Resilience4j**

pom

```
				<!-- Resilience4j Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
        </dependency>
        <!-- 由于断路保护等需要AOP实现，所以必须导入AOP包 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

### 熔断器

Application.yml

```
resilience4j:
	# 熔断器
	circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
```

ConsumerApplication

<!--需要加LoadBalanced才能开启熔断器-->

```
import com.ljl.consumer.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients({
        @LoadBalancerClient(value = "user-service", configuration = CustomLoadBalancerConfiguration.class),
        @LoadBalancerClient(value = "root-service", configuration = CustomLoadBalancerConfiguration.class)
})
public class ConsumerApplication {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```

ConsumerController

```
import com.ljl.consumer.domain.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Resource
    private RestTemplate restTemplate;

    private static final String CIRCUIT_BREAKER_NAME = "myCircuitBreaker";
    private static final String USER_SERVICE_NAME = "user-service";
    private static final String ROOT_SERVICE_NAME = "root-service";

    // @CircuitBreaker：应用断路器，指定断路器名称和回退方法。
    // fallback(Throwable t)：当断路器打开或服务调用失败时调用的回退方法。
    @GetMapping("/user/{id}")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "myCircuitFallback")
    public User queryById(@PathVariable Long id) {
        if (id < 0) {
            log.error("id不能为负");
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

    // 回退方法，当熔断器触发时调用，参数和返回值需要与被熔断的方法相同（参数在原来基础上加Throwable）
    public User myCircuitFallback(Long id, Throwable t) {
        log.error("服务不可用，进入回退方法: {}", t.getMessage());
        User fallbackUser = new User();
        fallbackUser.setId(id);
        fallbackUser.setName("服务暂时不可用，请稍后再试。");
        return fallbackUser;
    }
}
```

断路器状态监测

```
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

```

### 限流器

application.yml

```
resilience4j:
	# 熔断器
  circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开(需要将限流器的错误排除故障率统计)
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
        ignore-exceptions:
          - io.github.resilience4j.ratelimiter.RequestNotPermitted
  # 限流器
  ratelimiter:
    instances:
      myRateLimiter:
        limitForPeriod: 5                 # 每个周期允许的最大请求数
        limitRefreshPeriod: 10s            # 限流刷新周期
        timeoutDuration: 500ms            # 超过限流等待的最大时间
```

ConsumerController

```
import com.ljl.consumer.domain.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.ljl.consumer.utils.constant.LimterConstant.CIRCUIT_BREAKER_NAME;
import static com.ljl.consumer.utils.constant.LimterConstant.RATE_LIMITER_NAME;


@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Resource
    private RestTemplate restTemplate;


    // @CircuitBreaker：应用断路器，指定断路器名称和回退方法。
    // fallback(Throwable t)：当断路器打开或服务调用失败时调用的回退方法。
    // 熔断器和限流器时需要将fallbackMethod加在熔断器上,否则不走熔断器了（原因未知 试出来的）
    @GetMapping("/user/{id}")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "Fallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    public User queryById(@PathVariable Long id) {
        if (id < 0) {
            log.error("id不能为负");
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
    public User Fallback(Long id, Throwable t) {
        log.error("服务不可用，进入回退方法: {}", t.getMessage());
        User fallbackUser = new User();
        fallbackUser.setId(id);
        if( t instanceof RequestNotPermitted)
            fallbackUser.setName("请求过于频繁，请稍后再试。");
        else
            fallbackUser.setName("服务暂时不可用，请稍后再试。");
        return fallbackUser;
    }
}
```

RateLimiterEventListener

```
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

```

### 重试

application.yml

```
resilience4j:
  # 熔断器
  circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开(需要将限流器的错误排除故障率统计)
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
        ignore-exceptions:
          - io.github.resilience4j.ratelimiter.RequestNotPermitted
  # 限流器
  ratelimiter:
    instances:
      myRateLimiter:
        limitForPeriod: 5                 # 每个周期允许的最大请求数
        limitRefreshPeriod: 1s            # 限流刷新周期
        timeoutDuration: 500ms            # 超过限流等待的最大时间
  # 重试
  retry:
      instances:
        myRetry:
          # 最大重试次数(3 就是1次调用+2次重试)
          maxAttempts: 3
          # 重试之间的等待时间
          wait-duration: 2s
          retryExceptions:
            - java.lang.IllegalStateException
            - java.io.IOException
            - java.util.concurrent.TimeoutException
            - org.springframework.web.client.ResourceAccessException
```

ConsumerController

```
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
    public User queryById(@PathVariable Long id) {
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
        user.setId(id);
        if( t instanceof RequestNotPermitted)
            user.setName("请求过于频繁，请稍后再试。");
        else
            user.setName("服务暂时不可用，请稍后再试。");
        return user;
    }
}
```

RetryEventListener

```
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.ljl.consumer.utils.constant.LimterConstant.RETRY_NAME;

@Component
public class RetryEventListener {

    private final Logger logger = LoggerFactory.getLogger(RetryEventListener.class);

    public RetryEventListener(RetryRegistry retryRegistry) {
        retryRegistry.retry(RETRY_NAME).getEventPublisher()
                .onRetry(event -> {
                    logger.debug("尝试重新调用: " + event);
                }).onSuccess(event -> {
                    logger.debug("重试后调用失败:" + event);
                }).onError(event -> {
                    logger.error("重试后仍然失败: " + event);
                }).onIgnoredError(event -> {
                    logger.debug("重试器忽略的异常:" + event);
                });

    }

}

```

