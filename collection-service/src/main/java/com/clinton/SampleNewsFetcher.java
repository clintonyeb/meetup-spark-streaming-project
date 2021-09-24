package com.clinton;

import com.clinton.models.Article;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class SampleNewsFetcher implements APIFetcher<List<Article>> {
    private final String fileLocation;

    public SampleNewsFetcher(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public void fetch(Consumer<List<Article>> consumer) {
        try {
            List<Article> articles = DI.OBJECT_MAPPER.readValue(
                    Paths.get(fileLocation).toFile(),
                    new TypeReference<List<Article>>() {
                    }
            );
            consumer.accept(articles);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
