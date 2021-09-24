package com.clinton;

public class Application {
    private static final String DEBUG_MODE = Utils.getEnv("DEBUG_MODE");
    private static final String SAMPLE_FILE = Utils.getEnv("SAMPLE_FILE");

    public static void main(String[] args) {
        boolean debug = "true".equals(DEBUG_MODE);

        SentimentProducer sentimentProducer = new SentimentProducer();
        Analyzer sentimentAnalyzer;

        if (debug) {
            sentimentAnalyzer = new SampleSentimentAnalyzer(SAMPLE_FILE, sentimentProducer);
        } else {
            sentimentAnalyzer = new SentimentAnalyzer(sentimentProducer);
        }


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
