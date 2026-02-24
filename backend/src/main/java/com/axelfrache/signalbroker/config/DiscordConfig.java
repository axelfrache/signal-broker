package com.axelfrache.signalbroker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DiscordConfig {

    @Bean
    public WebClient.Builder discordWebClientBuilder() {
        return WebClient.builder();
    }
}
