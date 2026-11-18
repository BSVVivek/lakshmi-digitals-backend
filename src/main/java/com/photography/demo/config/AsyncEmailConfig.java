
package com.photography.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.task.TaskRejectedException;

import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncEmailConfig implements AsyncConfigurer {

    private static final Logger logger =
            LoggerFactory.getLogger(AsyncEmailConfig.class);

    // Dedicated thread pool for email delivery
    @Bean(name = "emailTaskExecutor")
    public ThreadPoolTaskExecutor emailTaskExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        // One email worker to preserve task ordering
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);

        // Allow email tasks to wait in the queue
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("lakshmi-email-");

        // Allow queued tasks to finish during graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);

        // Reject new tasks if the queue is full
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.AbortPolicy()
        );

        return executor;
    }

    // Log exceptions from asynchronous void methods
    @Override
    public AsyncUncaughtExceptionHandler
    getAsyncUncaughtExceptionHandler() {

        return (exception, method, parameters) -> {

            logger.error(
                    "Asynchronous email task failed: {}",
                    method.getName(),
                    exception
            );
        };
    }
}
