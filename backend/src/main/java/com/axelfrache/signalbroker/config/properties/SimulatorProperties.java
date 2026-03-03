package com.axelfrache.signalbroker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(
        boolean enabled,
        int count,
        long rateMs) {
}
