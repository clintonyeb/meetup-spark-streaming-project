package com.clinton;

import org.apache.kafka.clients.producer.*;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class RSVPProducer {
    private static final String MESSAGE_TOPIC_ENV = "KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static KafkaProducer<byte[], byte[]> kafkaProducer;

    RSVPProducer() {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Util.getEnv(KAFKA_SERVER));
        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, Util.getEnv(KAFKA_CLIENT_ID));
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");

        kafkaProducer = new KafkaProducer<>(producerProperties);
    }


    void sendMessage(final String messageKey, final byte[] message) {

        ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(Util.getEnv(MESSAGE_TOPIC_ENV),
                messageKey.getBytes(StandardCharsets.UTF_8), message);
        kafkaProducer.send(producerRecord, new TopicCallbackHandler(messageKey));
    }

    void close() {
        kafkaProducer.close();
    }


    private static final class TopicCallbackHandler implements Callback {
        final String eventKey;

        TopicCallbackHandler(final String eventKey) {
            this.eventKey = eventKey;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (null == metadata) {
                //mark record as failed
                HybridMessageLogger.moveToFailed(eventKey);
            } else {
                //remove the data from the localstate
                try {
                    HybridMessageLogger.removeEvent(eventKey);
                } catch (Exception e) {
                    //this should be logged...
                }
            }

        }
    }
}
