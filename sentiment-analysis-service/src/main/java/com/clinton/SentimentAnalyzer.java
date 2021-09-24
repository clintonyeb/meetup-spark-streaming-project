package com.clinton;

import com.clinton.models.Article;
import com.clinton.models.ArticleSentiment;
import com.clinton.models.SentimentResponse;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SentimentAnalyzer implements Analyzer {
    private final String SENTIMENT_URL = "SENTIMENT_URL";
    private final String API_HOST = "API_HOST";
    private final String API_KEY = "API_KEY";

    private final HttpClient client = new HttpClient();
    private final SentimentProducer sentimentProducer;

    public SentimentAnalyzer(SentimentProducer sentimentProducer) {
        this.sentimentProducer = sentimentProducer;
    }

    @Override
    public void process(byte[] key, byte[] article) {
        try {
            Article parsedArticle = DI.OBJECT_MAPPER.readValue(article, Article.class);

            Map<String, String> params = new HashMap<>();
            params.put("text", parsedArticle.getDescription());

            GetMethod method = new GetMethod(Utils.from(Utils.getEnv(SENTIMENT_URL), Utils.getEnv(API_KEY), params));
            method.setRequestHeader("x-rapidapi-host", Utils.getEnv(API_HOST));
            method.setRequestHeader("x-rapidapi-key", Utils.getEnv(API_KEY));
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();
            SentimentResponse response = DI.OBJECT_MAPPER.readValue(responseBody, SentimentResponse.class);

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
