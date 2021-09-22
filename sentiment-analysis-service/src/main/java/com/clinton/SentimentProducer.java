package com.clinton;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class SentimentProducer {

    private static final String MESSAGE_TOPIC_ENV = "SENTIMENT_KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private final Properties properties;
    private static final String topic = Util.getEnv(MESSAGE_TOPIC_ENV);
    private final SimpleProducer<byte[], byte[]> producer;

    SentimentProducer() {
        properties = new Properties();
        properties.put("bootstrap.servers", Util.getEnv(KAFKA_SERVER));
        properties.put("client.id", Util.getEnv(KAFKA_CLIENT_ID));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put("acks", "all");
        properties.put("retries", "3");

        producer = new SimpleProducer<>(properties);
    }

    public void sendMessage(byte[] messageKey, byte[] message) {
        producer.send(
                topic,
                messageKey,
                message
        );
    }

    public void close() {
        producer.close();
    }
}
