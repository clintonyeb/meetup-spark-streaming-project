package com.clinton;

import com.clinton.models.Article;
import com.clinton.models.NewsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.common.protocol.types.Field;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NewsFetcher {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private final String url;
    private final ObjectMapper objectMapper;
    private final NewsProducer newsProducer;

    public NewsFetcher(String url, ObjectMapper objectMapper, NewsProducer newsProducer) {
        this.url = url;
        this.objectMapper = objectMapper;
        this.newsProducer = newsProducer;
    }

    public void start() {
        HttpClient client = new HttpClient();

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Fetching news data");
            try {
                getNews(client, url);
            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                // Release the connection.
                newsProducer.close();
            }
        }, 10, 60 * 10, TimeUnit.SECONDS);
    }

    private void getNews(HttpClient client, String url) throws IOException {
        // Execute the method.
        GetMethod method = new GetMethod(url);

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));


        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();
        NewsResponse newsResponse = objectMapper.readValue(responseBody, NewsResponse.class);

        for (Article article : newsResponse.getHits()) {
            sendToKafka(article);
        }
    }

    private void sendToKafka(Article article) {
        final String messageKey = article.getTitle();
        newsProducer.sendMessage(messageKey, SerializationUtils.serialize(article));
    }
}
