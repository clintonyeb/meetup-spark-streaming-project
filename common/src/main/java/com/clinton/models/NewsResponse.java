package com.clinton.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NewsResponse {
    private int status;
    int numResults;
    List<Article> hits;
}
