package com.clinton;

import com.clinton.models.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;

public class SentimentAnalyzer {
    private final HttpClient client = new HttpClient();
    private final String url;
    private final ObjectMapper objectMapper;
    private final SentimentProducer sentimentProducer;

    public SentimentAnalyzer(String url, ObjectMapper objectMapper, SentimentProducer sentimentProducer) {
        this.url = url;
        this.objectMapper = objectMapper;
        this.sentimentProducer = sentimentProducer;
    }

    private void process() throws IOException {
        // Execute the method.
        GetMethod method = new GetMethod(url);

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));


        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();
        System.out.println("Sentiment response " + responseBody);
//        NewsResponse newsResponse = objectMapper.readValue(responseBody, NewsResponse.class);

//        for (Article article : newsResponse.getHits()) {
//            sendToKafka(article);
//        }
    }

    private void sendToKafka(Article article) {
        final String messageKey = article.getTitle();
        sentimentProducer.sendMessage(messageKey, SerializationUtils.serialize(article));
    }
}
