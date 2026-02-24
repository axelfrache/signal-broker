package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;

public interface AlertingService {
    void formattingFailed(RawInboundEvent raw, Exception e);

    void labelingFailed(FormattedTicketEvent formatted, Exception e);
}
