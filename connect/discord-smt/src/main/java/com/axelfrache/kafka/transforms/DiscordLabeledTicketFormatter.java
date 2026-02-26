package com.axelfrache.kafka.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;

/**
 * Transforms successfully labeled tickets into Discord webhook format
 */
public class DiscordLabeledTicketFormatter<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public R apply(R record) {
        if (record.value() == null) {
            return record;
        }

        try {
            String valueStr = record.value().toString();
            JsonNode ticketEvent;

            try {
                ticketEvent = mapper.readTree(valueStr);
            } catch (Exception e) {
                System.err.println("Could not parse Labeled Ticket event as JSON: " + valueStr);
                return record;
            }

            ObjectNode discordMessage = mapper.createObjectNode();
            discordMessage.put("content", "✅ **New Support Ticket Labeled**");

            ObjectNode embed = mapper.createObjectNode();
            embed.put("color", 3066993); // Green color

            if (ticketEvent.has("subject") && !ticketEvent.get("subject").isNull()) {
                embed.put("title", "#" + (ticketEvent.has("commonId") ? ticketEvent.get("commonId").asText() : "?")
                        + " " + ticketEvent.get("subject").asText());
            } else {
                embed.put("title", "New Support Ticket");
            }

            if (ticketEvent.has("labeledAt") && !ticketEvent.get("labeledAt").isNull()) {
                embed.put("timestamp", ticketEvent.get("labeledAt").asText());
            }

            ArrayNode fields = mapper.createArrayNode();

            if (ticketEvent.has("ticketId") && !ticketEvent.get("ticketId").isNull()) {
                addField(fields, "Ticket ID", ticketEvent.get("ticketId").asText(), true);
            }

            if (ticketEvent.has("category") && !ticketEvent.get("category").isNull()) {
                addField(fields, "Category", ticketEvent.get("category").asText(), true);
            }

            if (ticketEvent.has("priority") && !ticketEvent.get("priority").isNull()) {
                addField(fields, "Priority", ticketEvent.get("priority").asText(), true);
            }

            if (ticketEvent.has("ticketType") && !ticketEvent.get("ticketType").isNull()) {
                addField(fields, "Type", ticketEvent.get("ticketType").asText(), true);
            }

            if (ticketEvent.has("confidence") && !ticketEvent.get("confidence").isNull()) {
                double conf = ticketEvent.get("confidence").asDouble();
                String formattedConf = String.format("%.2f%%", conf * 100);
                addField(fields, "AI Confidence", formattedConf, true);
            }

            embed.set("fields", fields);

            ArrayNode embeds = mapper.createArrayNode();
            embeds.add(embed);
            discordMessage.set("embeds", embeds);

            return record.newRecord(
                    record.topic(),
                    record.kafkaPartition(),
                    record.keySchema(),
                    record.key(),
                    Schema.STRING_SCHEMA,
                    discordMessage.toString(),
                    record.timestamp());

        } catch (Exception e) {
            System.err.println("Failed to transform Labeled Ticket to Discord format: " + e.getMessage());
            e.printStackTrace();
            return record;
        }
    }

    private void addField(ArrayNode fields, String name, String value, boolean inline) {
        ObjectNode field = mapper.createObjectNode();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", inline);
        fields.add(field);
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
