package com.clinton.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleSentiment implements Serializable {
    private Article article;
    private SentimentResponse sentimentResponse;
}
