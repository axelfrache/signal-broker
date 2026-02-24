package com.axelfrache.signalbroker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaConfig {

    @Value("${ollama.baseUrl}")
    private String ollamaBaseUrl;

    @Bean
    public WebClient ollamaWebClient(WebClient.Builder builder) {
        return builder.baseUrl(ollamaBaseUrl).build();
    }
}
