package com.clinton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Application {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SentimentProducer sentimentProducer = new SentimentProducer();
        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer(objectMapper, sentimentProducer);
        NewsConsumer newsConsumer = new NewsConsumer(sentimentAnalyzer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown started...");
                sentimentProducer.close();
                newsConsumer.close();
                System.out.println("Shutdown finished");
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }));

        newsConsumer.start();
    }
}
