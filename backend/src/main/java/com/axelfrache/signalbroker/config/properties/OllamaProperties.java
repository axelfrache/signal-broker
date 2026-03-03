package com.axelfrache.signalbroker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(
        String baseUrl,
        Model model) {

    public record Model(String classifier) {
    }
}
