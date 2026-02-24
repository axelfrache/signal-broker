package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.dto.ClassificationResult;
import com.axelfrache.signalbroker.exception.OllamaClientException;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;

public interface ClassifierClient {
    ClassificationResult classify(FormattedTicketEvent formatted) throws OllamaClientException;
}
