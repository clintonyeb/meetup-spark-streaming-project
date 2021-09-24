package com.clinton;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumer<K extends Serializable, V extends Serializable> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final String topic;
    private final Processor<K, V> processor;
    private String clientId;
    private org.apache.kafka.clients.consumer.KafkaConsumer consumer;
    private AtomicBoolean closed = new AtomicBoolean();
    private CountDownLatch shutdownlatch = new CountDownLatch(1);

    public KafkaConsumer(Properties configs, String topic, Processor<K, V> processor) {
        this.clientId = configs.getProperty(ConsumerConfig.CLIENT_ID_CONFIG);
        this.consumer = new org.apache.kafka.clients.consumer.KafkaConsumer(configs);
        this.topic = topic;
        this.processor = processor;
    }

    @Override
    public void run() {

        try {
            logger.info("Starting the Consumer : {}", clientId);
            consumer.subscribe(Collections.singletonList(topic));

            logger.info("C : {}, Started to process records", clientId);

            while (!closed.get()) {
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(5000));

                if (records.isEmpty()) {
                    logger.info("C : {}, Found no records", clientId);
                    continue;
                }

                logger.info("C : {} Total No. of records received : {}", clientId, records.count());
                for (ConsumerRecord<K, V> record : records) {
                    logger.info("C : {}, Record received topic : {}, partition : {}, offset : {}",
                            clientId, record.topic(), record.partition(),
                            record.offset());
                    processor.process(record.key(), record.value());
                    Thread.sleep(1000);
                }
                // User has to take care of committing the offsets
                consumer.commitSync();
            }
        } catch (Exception e) {
            logger.error("Error while consuming messages", e);
        } finally {
            consumer.close();
            shutdownlatch.countDown();
            logger.info("C : {}, consumer exited", clientId);
        }
    }

    public void close() {
        try {
            closed.set(true);
            shutdownlatch.await();
        } catch (InterruptedException e) {
            logger.error("Error", e);
        }
    }
}
