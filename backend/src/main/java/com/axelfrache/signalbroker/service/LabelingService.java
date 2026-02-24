package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.exception.LabelingException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;

public interface LabelingService {
    LabeledTicketEvent label(FormattedTicketEvent formatted) throws LabelingException;
}
