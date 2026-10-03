package com.atsresume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(int corePoolSize, int maxPoolSize, int queueCapacity) {
}
