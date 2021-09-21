package com.clinton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;
import java.util.StringJoiner;

public class Application {
    private static final String NEWS_URL = "NEWS_URL";
    private static final String NEWS_API_KEY = "NEWS_API_KEY";

    public static void main(String[] args) {
        try {
            HybridMessageLogger.initialize();
        } catch (Exception exception) {
            System.err.println("Could not initialize HybridMessageLogger!");
            exception.printStackTrace();
            System.exit(-1);
        }


        final NewsProducer newsProducer = new NewsProducer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown started...");
                newsProducer.close();
                HybridMessageLogger.close();
                System.out.println("Shutdown finished");
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }));

        Map<String, String> params = Map.of(
                "country", "us",
                "language", "en"
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        NewsFetcher newsFetcher = new NewsFetcher(from(Util.getEnv(NEWS_URL), Util.getEnv(NEWS_API_KEY), params), objectMapper, newsProducer);
        newsFetcher.start();
    }

    private static String from(String endpoint, String apiKey, Map<String, String> params) {
        return String.format("%s?%s&apiKey=%s", endpoint, mapToParam(params), apiKey);
    }

    private static String mapToParam(Map<String, String> param) {
        StringJoiner join = new StringJoiner("&");
        for (String key : param.keySet()) {
            join.add(String.format("%s=%s", key, param.get(key)));
        }
        return join.toString();
    }
}
