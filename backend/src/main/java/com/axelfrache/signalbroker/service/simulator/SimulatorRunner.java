package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.config.properties.SimulatorProperties;
import com.axelfrache.signalbroker.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatorRunner implements CommandLineRunner {

    private final List<MessageSource> messageSources;
    private final EventPublisher eventPublisher;
    private final SimulatorProperties simulatorProperties;

    @Override
    public void run(String... args) throws Exception {
        if (!simulatorProperties.enabled() || messageSources.isEmpty()) {
            log.info("Simulator is disabled or no sources available.");
            return;
        }

        log.info("Starting simulator. Generating {} messages with {} ms delay...", simulatorProperties.count(),
                simulatorProperties.rateMs());
        var random = ThreadLocalRandom.current();

        for (var i = 0; i < simulatorProperties.count(); i++) {
            var source = messageSources.get(random.nextInt(messageSources.size()));
            var event = source.next();

            switch (source.channelType()) {
                case WHATSAPP -> eventPublisher.publishWhatsappRaw(event);
                case MAIL -> eventPublisher.publishMailRaw(event);
            }

            if (simulatorProperties.rateMs() > 0) {
                Thread.sleep(simulatorProperties.rateMs());
            }
        }

        log.info("Simulator finished emitting {} messages.", simulatorProperties.count());
    }
}
