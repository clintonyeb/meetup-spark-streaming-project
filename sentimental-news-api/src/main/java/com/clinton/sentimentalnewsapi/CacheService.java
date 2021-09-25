package com.clinton.sentimentalnewsapi;

//@Service
//public class CacheService {
//
//    private final LoadingCache<String, List<Record>> graphs;
//
//    @Autowired
//    public CacheService(FetchNewsService fetchNewsService) {
//        graphs = Caffeine.newBuilder()
//                .maximumSize(10_000)
//                .expireAfterWrite(Duration.ofMinutes(5))
//                .refreshAfterWrite(Duration.ofMinutes(1))
//                .build(key -> fetchNewsService.get());
//    }
//
//    public List<Record> get() {
//        return graphs.get("");
//    }
//}
