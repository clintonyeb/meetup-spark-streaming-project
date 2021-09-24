package com.clinton.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentimentResponse implements Comparable<SentimentResponse>, Serializable {
    private List<Keyword> keywords;
    private int ratio;
    private double score;
    private String type;

    @Override
    public int compareTo(SentimentResponse o) {
        int scoreResults = Double.compare(Math.abs(score), Math.abs(o.score));
        if (scoreResults == 0) return scoreResults;
        return Integer.compare(Math.abs(ratio), Math.abs(o.ratio));
    }

    @Data
    private static class Keyword implements Serializable {
        private double score;
        private String word;
    }
}
