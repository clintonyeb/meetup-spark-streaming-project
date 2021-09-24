package com.clinton;

import com.clinton.models.Article;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
    private static final String NEWS_URL = "NEWS_URL";
    private static final String NEWS_API_KEY = "NEWS_API_KEY";
    private static final String DEBUG_MODE = Utils.getEnv("DEBUG_MODE");
    private static final String SAMPLE_FILE = Utils.getEnv("SAMPLE_FILE");

    public static void main(String[] args) {
        boolean debug = "true".equals(DEBUG_MODE);

        final NewsProducer newsProducer = new NewsProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown started...");
                newsProducer.close();
                System.out.println("Shutdown finished");
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }));

        Map<String, String> params = new HashMap<>();
        params.put("country", "us");
        params.put("language", "en");
        params.put("topic", "entertainment");

        APIFetcher<List<Article>> apiFetcher;

        if (debug) {
            apiFetcher = new SampleNewsFetcher(SAMPLE_FILE);
        } else {
            apiFetcher = new NewsFetcher(Utils.from(Utils.getEnv(NEWS_URL), Utils.getEnv(NEWS_API_KEY), params));
        }

        apiFetcher.fetch(newsProducer::sendMessage);
    }
}
