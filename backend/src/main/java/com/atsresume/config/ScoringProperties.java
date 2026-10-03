package com.atsresume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scoring")
public record ScoringProperties(Weights weights) {

    public record Weights(
            double keywordMatch,
            double formatting,
            double structure,
            double impact,
            double contactInfo) {
    }
}
