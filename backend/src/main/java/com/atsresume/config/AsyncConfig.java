package com.atsresume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Resume processing (parsing + scoring + Gemini call) runs on its own bounded
 * pool so upload requests always return immediately and never queue behind it.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "resumeProcessingExecutor")
    public Executor resumeProcessingExecutor(AsyncProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.corePoolSize());
        executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setThreadNamePrefix("resume-processing-");
        // Once the queue is full, run the task on the caller's thread instead of
        // rejecting it outright — applies natural backpressure to callers under load.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
