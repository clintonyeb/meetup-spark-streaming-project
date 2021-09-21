package com.clinton;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class NewsConsumer {
    private static final String MESSAGE_TOPIC_ENV = "KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";

    private final KafkaConsumer<byte[], byte[]> kafkaConsumer;

    public NewsConsumer() {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Util.getEnv(KAFKA_SERVER));
        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, Util.getEnv(KAFKA_CLIENT_ID));
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");

        kafkaConsumer = new KafkaConsumer<>(producerProperties);
    }

    public void start() {
        kafkaConsumer.subscribe(Collections.singletonList(Util.getEnv(MESSAGE_TOPIC_ENV)));

        final int giveUp = 100;
        int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<byte[], byte[]> consumerRecords = kafkaConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

            if (consumerRecords.count() == 0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record:(%s, %s, %d, %d)\n",
                        toString(record.key()), toString(record.value()),
                        record.partition(), record.offset());
            });

            kafkaConsumer.commitAsync();
        }
    }

    private static String toString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
