package com.clinton;

import com.clinton.models.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class NewsProducer {
    private static final String MESSAGE_TOPIC_ENV = "KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static final int delay = 1000;
    private final Properties properties;
    private static final String topic = Util.getEnv(MESSAGE_TOPIC_ENV);
    private final ObjectMapper objectMapper;
    private final  SimpleProducer<byte[], byte[]> producer;

    public NewsProducer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        properties = new Properties();
        properties.put("bootstrap.servers", Util.getEnv(KAFKA_SERVER));
        properties.put("client.id", Util.getEnv(KAFKA_CLIENT_ID));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put("acks", "all");
        properties.put("retries", "3");

        producer = new SimpleProducer<>(properties);
    }


    void sendMessage(List<Article> articles) {
        for (Article article : articles) {
            send(producer, article);
        }
//        producer.flush();
    }

    private void send(SimpleProducer<byte[], byte[]> producer, Article article) {
        String messageKey = UUID.randomUUID().toString();
        producer.send(
                topic,
                Util.serializeStr(objectMapper, messageKey),
                Util.serializeObj(objectMapper, article)
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
