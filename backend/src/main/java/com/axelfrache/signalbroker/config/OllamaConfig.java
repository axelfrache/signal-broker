package com.axelfrache.signalbroker.config;

import com.axelfrache.signalbroker.config.properties.OllamaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class OllamaConfig {

    private final OllamaProperties ollamaProperties;

    @Bean
    public WebClient ollamaWebClient(WebClient.Builder builder) {
        return builder.baseUrl(ollamaProperties.baseUrl()).build();
    }
}
