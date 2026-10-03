package com.atsresume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(String apiKey, String model, int timeoutSeconds, int maxConcurrentCalls) {
}
