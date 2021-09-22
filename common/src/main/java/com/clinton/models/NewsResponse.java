package com.clinton.models;

import lombok.Data;

import java.util.List;

@Data
public class NewsResponse {
    private int status;
    int numResults;
    List<Article> hits;
}
