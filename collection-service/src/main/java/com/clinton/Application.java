package com.clinton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

public class Application {
    private static final String NEWS_URL = "NEWS_URL";
    private static final String NEWS_API_KEY = "NEWS_API_KEY";

    public static void main(String[] args) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        final NewsProducer newsProducer = new NewsProducer(objectMapper);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown started...");
                newsProducer.close();
                System.out.println("Shutdown finished");
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }));

        Map<String, String> params = Map.of(
                "country", "us",
                "language", "en"
        );

        NewsFetcher newsFetcher = new NewsFetcher(Util.from(Util.getEnv(NEWS_URL), Util.getEnv(NEWS_API_KEY), params), objectMapper, newsProducer);
        newsFetcher.start();
    }
}
