package com.clinton;

import com.clinton.models.Article;
import com.clinton.models.ArticleSentiment;
import com.clinton.models.SentimentResponse;

import java.io.IOException;
import java.nio.file.Paths;

public class SampleSentimentAnalyzer implements Analyzer {
    private final String fileLocation;
    private final SentimentProducer sentimentProducer;

    public SampleSentimentAnalyzer(String fileLocation, SentimentProducer sentimentProducer) {
        this.fileLocation = fileLocation;
        this.sentimentProducer = sentimentProducer;
    }

    @Override
    public void process(byte[] key, byte[] article) {
        try {
            Article parsedArticle = DI.OBJECT_MAPPER.readValue(article, Article.class);
            SentimentResponse response = DI.OBJECT_MAPPER.readValue(Paths.get(fileLocation).toFile(), SentimentResponse.class);
            ArticleSentiment articleSentiment = new ArticleSentiment(parsedArticle, response);
            sendToKafka(key, DI.OBJECT_MAPPER.writeValueAsBytes(articleSentiment));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToKafka(byte[] key, byte[] response) {
        sentimentProducer.sendMessage(key, response);
    }
}
