package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.exception.FormattingException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;

public interface FormattingService {
    FormattedTicketEvent format(RawInboundEvent raw) throws FormattingException;
}
