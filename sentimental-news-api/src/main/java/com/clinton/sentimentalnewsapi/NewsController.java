package com.clinton.sentimentalnewsapi;

import com.clinton.models.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
public class NewsController {

    private final FetchNewsService fetchNewsService;

    @Autowired
    public NewsController(FetchNewsService fetchNewsService) {
        this.fetchNewsService = fetchNewsService;
    }

    @GetMapping("/")
    public List<Record> get() {
        try {
            return fetchNewsService.get();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
