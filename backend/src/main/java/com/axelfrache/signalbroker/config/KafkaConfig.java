package com.axelfrache.signalbroker.config;

import com.axelfrache.signalbroker.config.properties.KafkaAppProperties;
import com.axelfrache.signalbroker.model.kafka.FormattedTicketEvent;
import com.axelfrache.signalbroker.model.kafka.RawInboundEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;

import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaAppProperties kafkaProperties;

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper mapper) {
        var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var jsonSerializer = new JsonSerializer<>(mapper);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ObjectMapper mapper) {
        return new KafkaTemplate<>(producerFactory(mapper));
    }

    @Bean
    public ProducerFactory<String, Object> schemaRegistryProducerFactory() {
        var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer.class);
        configProps.put("schema.registry.url", kafkaProperties.schemaRegistry().url());
        configProps.put("auto.register.schemas", true);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> schemaRegistryKafkaTemplate() {
        return new KafkaTemplate<>(schemaRegistryProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, RawInboundEvent> rawConsumerFactory(ObjectMapper mapper) {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.groups().formatter());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        var jsonDeserializer = new JsonDeserializer<>(RawInboundEvent.class, mapper);
        jsonDeserializer.addTrustedPackages("com.axelfrache.signalbroker.*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RawInboundEvent> rawKafkaListenerContainerFactory(
            ObjectMapper mapper) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RawInboundEvent>();
        factory.setConsumerFactory(rawConsumerFactory(mapper));
        factory.setConcurrency(kafkaProperties.concurrency().formatter());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, FormattedTicketEvent> formattedConsumerFactory(ObjectMapper mapper) {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.groups().labeler());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        var jsonDeserializer = new JsonDeserializer<>(FormattedTicketEvent.class, mapper);
        jsonDeserializer.addTrustedPackages("com.axelfrache.signalbroker.*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FormattedTicketEvent> formattedKafkaListenerContainerFactory(
            ObjectMapper mapper) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, FormattedTicketEvent>();
        factory.setConsumerFactory(formattedConsumerFactory(mapper));
        factory.setConcurrency(kafkaProperties.concurrency().labeler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
