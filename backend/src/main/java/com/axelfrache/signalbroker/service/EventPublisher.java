package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.model.kafka.DlqEvent;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.LabeledTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;

public interface EventPublisher {
    void publishWhatsappRaw(RawInboundEvent event);

    void publishMailRaw(RawInboundEvent event);

    void publishFormatted(FormattedTicketEvent event);

    void publishLabeled(LabeledTicketEvent event);

    void publishWhatsappFormatDlq(DlqEvent event);

    void publishMailFormatDlq(DlqEvent event);

    void publishLabelDlq(DlqEvent event);
}
