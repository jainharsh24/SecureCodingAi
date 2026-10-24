package com.securecode.ai.evaluation.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(SecurityEvaluationProperties.class)
public class SecurityEvaluationExecutionConfig {
    @Bean(destroyMethod = "shutdown")
    ExecutorService securityEvaluatorExecutor(SecurityEvaluationProperties properties) {
        return new ThreadPoolExecutor(properties.concurrency(), properties.concurrency(), 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.concurrency()), Thread.ofPlatform().name("security-evaluator-", 0).factory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
