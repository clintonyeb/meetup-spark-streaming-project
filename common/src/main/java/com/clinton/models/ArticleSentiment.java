package com.clinton.models;

import lombok.Data;

@Data
public class ArticleSentiment {
    private final Article article;
    private final SentimentResponse sentimentResponse;
}
