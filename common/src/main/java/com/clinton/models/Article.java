package com.clinton.models;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Article implements Serializable {
    private List<String> authors;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String content;
    private String source;
    private String pubDate;
    private String country;
    private String language;

}
