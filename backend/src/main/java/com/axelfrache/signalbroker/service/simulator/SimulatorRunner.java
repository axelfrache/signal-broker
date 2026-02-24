package com.axelfrache.signalbroker.service.simulator;

import com.axelfrache.signalbroker.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatorRunner implements CommandLineRunner {

    private final List<MessageSource> messageSources;
    private final EventPublisher eventPublisher;

    @Value("${simulator.enabled:false}")
    private boolean enabled;

    @Value("${simulator.count:50}")
    private int count;

    @Value("${simulator.rateMs:200}")
    private long rateMs;

    @Override
    public void run(String... args) throws Exception {
        if (!enabled || messageSources.isEmpty()) {
            log.info("Simulator is disabled or no sources available.");
            return;
        }

        log.info("Starting simulator. Generating {} messages with {} ms delay...", count, rateMs);
        var random = new Random();

        for (var i = 0; i < count; i++) {
            var source = messageSources.get(random.nextInt(messageSources.size()));
            var event = source.next();

            switch (source.channelType()) {
                case WHATSAPP -> eventPublisher.publishWhatsappRaw(event);
                case MAIL -> eventPublisher.publishMailRaw(event);
            }

            if (rateMs > 0) {
                Thread.sleep(rateMs);
            }
        }

        log.info("Simulator finished emitting {} messages.", count);
    }
}
