package com.axelfrache.signalbroker.service.labeling;

import com.axelfrache.signalbroker.exception.LabelingException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import com.axelfrache.signalbroker.service.ClassifierClient;
import com.axelfrache.signalbroker.service.LabelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultLabelingService implements LabelingService {

    private final ClassifierClient classifierClient;

    @Override
    public LabeledTicketEvent label(@lombok.NonNull FormattedTicketEvent formatted) throws LabelingException {
        try {
            var result = classifierClient.classify(formatted);

            if (result.confidence() < 0.0 || result.confidence() > 1.0) {
                throw new LabelingException("Confidence score out of bounds: " + result.confidence());
            }

            if (result.labels() != null && result.labels().size() > 8) {
                throw new LabelingException("Too many labels returned by classifier (max 8)");
            }

            if (result.labels() != null && result.labels().stream().anyMatch(l -> l.length() > 20)) {
                throw new LabelingException("Label exceeds maximum length of 20 characters");
            }

            return new LabeledTicketEvent(
                    UUID.randomUUID(),
                    formatted.ticketId(),
                    Instant.now(),
                    result.category(),
                    result.priority(),
                    result.labels(),
                    result.summary(),
                    result.confidence(),
                    1);
        } catch (Exception e) {
            throw new LabelingException("Failed to label ticket: " + e.getMessage(), e);
        }
    }
}
