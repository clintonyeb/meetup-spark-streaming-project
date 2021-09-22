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

import java.io.IOException;
import java.util.List;
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
                newsProducer.sendMessage(getNews(client, url));
            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    private List<Article> getNews(HttpClient client, String url) throws IOException {
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

        return newsResponse.getHits();
    }
}
