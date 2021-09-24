package com.clinton.models;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NewsResponse implements Serializable {
    int numResults;
    List<Article> hits;
    private int status;
}
