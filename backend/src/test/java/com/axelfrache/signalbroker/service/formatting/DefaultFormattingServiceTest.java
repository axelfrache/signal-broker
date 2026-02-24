package com.axelfrache.signalbroker.service.formatting;

import com.axelfrache.signalbroker.exception.FormattingException;
import com.axelfrache.signalbroker.model.enums.ChannelType;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFormattingServiceTest {

    private DefaultFormattingService formattingService;

    @BeforeEach
    void setUp() {
        formattingService = new DefaultFormattingService();
    }

    @Test
    void testValidFormatting() {
        var originalDate = Instant.parse("2026-02-22T14:32:18Z");
        var raw = new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.MAIL,
                "msg-123",
                originalDate,
                "test@example.com",
                "Hello world\n\nThis is a test message.",
                Map.of(),
                1);

        var formatted = formattingService.format(raw);

        assertNotNull(formatted);
        assertNotNull(formatted.ticketId());
        assertEquals(ChannelType.MAIL, formatted.channel());
        assertEquals(originalDate, formatted.receivedAt());
        assertNotNull(formatted.createdAt());
        assertEquals("Hello world This is a test message.", formatted.body());
        assertEquals("test@example.com", formatted.contact());
        assertEquals(1, formatted.schemaVersion());
    }

    @Test
    void testEmptyBodyThrowsException() {
        var raw = new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.WHATSAPP,
                "msg-456",
                Instant.now(),
                "+1234567890",
                "   \n  ",
                Map.of(),
                1);

        var exception = assertThrows(FormattingException.class, () -> formattingService.format(raw));
        assertEquals("Body cannot be empty", exception.getMessage());
    }

    @Test
    void testBodyIsNormalized() {
        var raw = new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.MAIL,
                "msg-789",
                Instant.now(),
                "test2@test.com",
                "Line one\n\n   Line two   \n\nLine three",
                Map.of(),
                1);

        var formatted = formattingService.format(raw);

        assertEquals("Line one Line two Line three", formatted.body());
    }
}
