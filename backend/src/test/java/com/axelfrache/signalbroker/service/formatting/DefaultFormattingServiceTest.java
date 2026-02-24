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
        var raw = new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.MAIL,
                "msg-123",
                Instant.now(),
                "test@example.com",
                "Hello world\n\nThis is a test message.",
                Map.of(),
                1);

        var formatted = formattingService.format(raw);

        assertNotNull(formatted);
        assertNotNull(formatted.ticketId());
        assertEquals(raw.eventId(), formatted.rawEventId());
        assertEquals(ChannelType.MAIL, formatted.channel());
        assertEquals("Hello world", formatted.subject());
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
    void testLongSubjectIsTruncated() {
        var longLine = "This is a very long text that exceeds eighty characters so it should be truncated properly when the formatting service processes it.";
        var raw = new RawInboundEvent(
                UUID.randomUUID(),
                ChannelType.MAIL,
                "msg-789",
                Instant.now(),
                "test2@test.com",
                longLine + "\nAnd some more text below.",
                Map.of(),
                1);

        var formatted = formattingService.format(raw);

        assertTrue(formatted.subject().length() <= 80);
        assertTrue(longLine.startsWith(formatted.subject()));
    }
}
