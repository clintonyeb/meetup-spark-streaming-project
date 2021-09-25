package com.clinton;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

public class NewsConsumer {
    private static final String MESSAGE_TOPIC_ENV = "NEWS_KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static final String KAFKA_GROUP_ID = "KAFKA_GROUP_ID";

    private final KafkaConsumer<byte[], byte[]> consumer;

    public NewsConsumer(Analyzer sentimentAnalyzer) {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Utils.getEnv(KAFKA_SERVER));
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, Utils.getEnv(KAFKA_CLIENT_ID));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, Utils.getEnv(KAFKA_GROUP_ID));
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "1024");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        consumer = new KafkaConsumer<>(
                properties,
                Utils.getEnv(MESSAGE_TOPIC_ENV),
                sentimentAnalyzer::process
        );
    }

    public void start() {
        consumer.run();
    }

    public void close() {
        consumer.close();
    }
}
