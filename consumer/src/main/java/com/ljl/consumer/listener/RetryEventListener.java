package com.ljl.consumer.listener;

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
                    logger.debug("尝试重试: " + event);
                }).onSuccess(event -> {
                    logger.debug("重试成功:" + event);
                }).onError(event -> {
                    logger.error("重试失败: " + event);
                }).onIgnoredError(event -> {
                    logger.debug("忽略错误:" + event);
                });

    }

}
