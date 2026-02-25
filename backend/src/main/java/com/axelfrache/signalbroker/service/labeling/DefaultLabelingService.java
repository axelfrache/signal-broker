package com.axelfrache.signalbroker.service.labeling;

import com.axelfrache.signalbroker.exception.LabelingException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import com.axelfrache.signalbroker.service.ClassifierClient;
import com.axelfrache.signalbroker.service.LabelingService;
import com.axelfrache.signalbroker.service.TicketGroupingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DefaultLabelingService implements LabelingService {

    private final ClassifierClient classifierClient;
    private final TicketGroupingService ticketGroupingService;

    @Override
    public LabeledTicketEvent label(@lombok.NonNull FormattedTicketEvent formatted) throws LabelingException {
        try {
            var result = classifierClient.classify(formatted);

            if (result.confidence() < 0.0 || result.confidence() > 1.0) {
                throw new LabelingException("Confidence score out of bounds: " + result.confidence());
            }

            var labeledTicket = new LabeledTicketEvent(
                    formatted.ticketId(),
                    formatted.receivedAt(),
                    Instant.now(),
                    result.subject(),
                    formatted.body(),
                    formatted.contact(),
                    result.category(),
                    result.ticketType(),
                    result.priority(),
                    result.confidence(),
                    null,
                    1);

            var commonId = ticketGroupingService.assignCommonId(labeledTicket);

            return new LabeledTicketEvent(
                    labeledTicket.ticketId(),
                    labeledTicket.receivedAt(),
                    labeledTicket.labeledAt(),
                    labeledTicket.subject(),
                    labeledTicket.body(),
                    labeledTicket.contact(),
                    labeledTicket.category(),
                    labeledTicket.ticketType(),
                    labeledTicket.priority(),
                    labeledTicket.confidence(),
                    commonId,
                    labeledTicket.schemaVersion());
        } catch (Exception e) {
            throw new LabelingException("Failed to label ticket: " + e.getMessage(), e);
        }
    }
}
