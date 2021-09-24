package com.clinton;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducer<K extends Serializable, V extends Serializable> {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final org.apache.kafka.clients.producer.KafkaProducer producer;
    private final boolean syncSend;
    private volatile boolean shutDown = false;


    public KafkaProducer(Properties producerConfig) {
        this(producerConfig, true);
    }

    public KafkaProducer(Properties producerConfig, boolean syncSend) {
        this.syncSend = syncSend;
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer(producerConfig);
        logger.info("Started Producer.  sync  : {}", syncSend);
    }

    public void send(String topic, V v) {
        send(topic, -1, null, v, new DummyCallback());
    }

    public void send(String topic, K k, V v) {
        send(topic, -1, k, v, new DummyCallback());
    }

    public void send(String topic, int partition, V v) {
        send(topic, partition, null, v, new DummyCallback());
    }

    public void send(String topic, int partition, K k, V v) {
        send(topic, partition, k, v, new DummyCallback());
    }

    public void send(String topic, int partition, K key, V value, Callback callback) {

        if (shutDown) {
            throw new RuntimeException("Producer is closed.");
        }

        logger.info("Sending data to Kafka.");


        try {
            ProducerRecord<K, V> record;
            if (partition < 0)
                record = new ProducerRecord<>(topic, key, value);
            else
                record = new ProducerRecord<>(topic, partition, key, value);


            Future<RecordMetadata> future = producer.send(record, callback);
            if (!syncSend) return;
            future.get();
        } catch (Exception e) {
            logger.error("Error while producing event for topic : {}", topic, e);
        }

    }

    public void flush() {
        logger.info("Flushing kafka");
        try {
            producer.flush();
        } catch (Exception e) {
            logger.error("Exception occurred while flushing the producer", e);
        }
    }

    public void close() {
        logger.info("Closing kafka");
        shutDown = true;
        try {
            producer.close();
        } catch (Exception e) {
            logger.error("Exception occurred while stopping the producer", e);
        }
    }

    private class DummyCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            logger.info("Callback called");
            if (e != null) {
                logger.error("Error while producing message to topic : {}", recordMetadata.topic(), e);
            } else
                logger.info("sent message to topic:{} partition:{}  offset:{}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
        }
    }
}
