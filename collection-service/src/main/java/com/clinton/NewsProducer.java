package com.clinton;

import com.clinton.models.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class NewsProducer {
    private static final String MESSAGE_TOPIC_ENV = "KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static final int delay = 1000;
    private static final String topic = Utils.getEnv(MESSAGE_TOPIC_ENV);
    private final Properties properties;
    private final KafkaProducer<byte[], byte[]> producer;

    public NewsProducer() {
        properties = new Properties();
        properties.put("bootstrap.servers", Utils.getEnv(KAFKA_SERVER));
        properties.put("client.id", Utils.getEnv(KAFKA_CLIENT_ID));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put("acks", "all");
        properties.put("retries", "3");

        producer = new KafkaProducer<>(properties);
    }


    void sendMessage(List<Article> articles) {
        for (Article article : articles) {
            send(producer, article);
        }
//        producer.flush();
    }

    private void send(KafkaProducer<byte[], byte[]> producer, Article article) {
        String messageKey = UUID.randomUUID().toString();
        producer.send(
                topic,
                Utils.serializeStr(messageKey),
                Utils.serializeObj(article)
        );
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        producer.close();
    }
}
