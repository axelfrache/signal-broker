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
 * Transforms DLQ events into Discord webhook format
 */
public class DiscordDlqFormatter<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public R apply(R record) {
        if (record.value() == null) {
            return record;
        }

        try {
            // Parse the DLQ event (assuming it's JSON)
            String valueStr = record.value().toString();
            JsonNode dlqEvent = mapper.readTree(valueStr);

            // Create Discord message
            ObjectNode discordMessage = mapper.createObjectNode();
            
            // Main content
            discordMessage.put("content", "⚠️ **Processing Error Detected**");
            
            // Create embed
            ObjectNode embed = mapper.createObjectNode();
            embed.put("title", "Dead Letter Queue Event");
            embed.put("color", 15158332); // Red color
            
            if (dlqEvent.has("failedAt")) {
                embed.put("timestamp", dlqEvent.get("failedAt").asText());
            }
            
            // Create fields array
            ArrayNode fields = mapper.createArrayNode();
            
            if (dlqEvent.has("eventId")) {
                addField(fields, "Event ID", dlqEvent.get("eventId").asText(), true);
            }
            
            if (dlqEvent.has("stage")) {
                addField(fields, "Stage", dlqEvent.get("stage").asText(), true);
            }
            
            if (dlqEvent.has("originalTopic")) {
                addField(fields, "Original Topic", dlqEvent.get("originalTopic").asText(), false);
            }
            
            if (dlqEvent.has("reason")) {
                addField(fields, "Reason", dlqEvent.get("reason").asText(), false);
            }
            
            if (dlqEvent.has("payloadSnippet") && !dlqEvent.get("payloadSnippet").isNull()) {
                String snippet = dlqEvent.get("payloadSnippet").asText();
                if (snippet != null && !snippet.isEmpty()) {
                    if (snippet.length() > 1000) {
                        snippet = snippet.substring(0, 997) + "...";
                    }
                    addField(fields, "Payload Snippet", "```\n" + snippet + "\n```", false);
                }
            }
            
            embed.set("fields", fields);
            
            // Add embed to embeds array
            ArrayNode embeds = mapper.createArrayNode();
            embeds.add(embed);
            discordMessage.set("embeds", embeds);
            
            // Return new record with Discord-formatted message
            return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                Schema.STRING_SCHEMA,
                discordMessage.toString(),
                record.timestamp()
            );
            
        } catch (Exception e) {
            // If transformation fails, log and return original record
            System.err.println("Failed to transform DLQ event to Discord format: " + e.getMessage());
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
        // Nothing to close
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // No configuration needed
    }
}
