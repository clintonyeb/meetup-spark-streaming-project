package com.clinton.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentimentResponse {
    private List<Keyword> keywords;
    private int ratio;
    private double score;
    private String type;

    @Data
    private static class Keyword {
        private double score;
        private String word;
    }
}
