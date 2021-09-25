package com.clinton;

import com.clinton.models.Article;
import com.clinton.models.NewsResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SampleNewsFetcher implements APIFetcher<List<Article>> {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private final String fileLocation;

    public SampleNewsFetcher(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public void fetch(Consumer<List<Article>> consumer) {

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Fetching news data");
            try {
                NewsResponse response = DI.OBJECT_MAPPER.readValue(Paths.get(fileLocation).toFile(), NewsResponse.class);
                consumer.accept(response.getHits());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 10, 60, TimeUnit.SECONDS);


    }
}
