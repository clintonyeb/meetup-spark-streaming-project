package com.clinton.sentimentalnewsapi;

import com.clinton.DI;
import com.clinton.models.Article;
import com.clinton.models.ArticleSentiment;
import com.clinton.models.Record;
import com.clinton.models.SentimentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FetchNewsService {

    private static final String HBASE_TABLE_NAME = "sentimental-news-highlights-table";
    private static final String HBASE_COLUMN_FAMILY = "sentimental-news-highlights-cf";
    private static final String HBASE_CONFIG_FILE = "/app/hbase-site.xml";

    private static Table table;
    private static final byte[] columnFamily = Bytes.toBytes(HBASE_COLUMN_FAMILY);

    public FetchNewsService() {
    }

    private static Table getTable() throws IOException {
        if (table == null) {
            Configuration hbaseConfig = HBaseConfiguration.create();
            hbaseConfig.addResource(new Path(HBASE_CONFIG_FILE));
            Connection connection = ConnectionFactory.createConnection(hbaseConfig);
            table = connection.getTable(TableName.valueOf(HBASE_TABLE_NAME));
        }

        return table;
    }

    private Record from(Result result) throws JsonProcessingException {
        byte[] id = result.getRow();
        byte[] articleTitleByte = result.getValue(columnFamily, Bytes.toBytes("article_title"));
        byte[] articleDescriptionByte = result.getValue(columnFamily, Bytes.toBytes("article_description"));
        byte[] articleContentByte = result.getValue(columnFamily, Bytes.toBytes("article_content"));
        byte[] articlePubDateByte = result.getValue(columnFamily, Bytes.toBytes("article_pub_date"));
        byte[] articleUrlByte = result.getValue(columnFamily, Bytes.toBytes("article_url"));
        byte[] articleImageUrlByte = result.getValue(columnFamily, Bytes.toBytes("article_image_url"));
        byte[] articleSourceByte = result.getValue(columnFamily, Bytes.toBytes("article_source"));
        byte[] articleCountryByte = result.getValue(columnFamily, Bytes.toBytes("article_country"));
        byte[] articleLanguageByte = result.getValue(columnFamily, Bytes.toBytes("article_language"));
        byte[] articleAuthorsByte = result.getValue(columnFamily, Bytes.toBytes("article_authors"));

        String articleId = Bytes.toString(id);

        String articleTitle = Bytes.toString(articleTitleByte);
        String articleDescription = Bytes.toString(articleDescriptionByte);
        String articleContent = Bytes.toString(articleContentByte);
        String articlePubDate = Bytes.toString(articlePubDateByte);
        String articleUrl = Bytes.toString(articleUrlByte);
        String articleImageUrl = Bytes.toString(articleImageUrlByte);
        String articleSource = Bytes.toString(articleSourceByte);
        String articleCountry = Bytes.toString(articleCountryByte);
        String articleLanguage = Bytes.toString(articleLanguageByte);
        String articleAuthors = Bytes.toString(articleAuthorsByte);

        byte[] sentimentRatioByte = result.getValue(columnFamily, Bytes.toBytes("sentiment_ratio"));
        byte[] sentimentScoreByte = result.getValue(columnFamily, Bytes.toBytes("sentiment_score"));
        byte[] sentimentTypeByte = result.getValue(columnFamily, Bytes.toBytes("sentiment_type"));
        byte[] sentimentKeywordsByte = result.getValue(columnFamily, Bytes.toBytes("sentiment_keywords"));

        int sentimentRatio = Bytes.toInt(sentimentRatioByte);
        double sentimentScore = Bytes.toDouble(sentimentScoreByte);
        String sentimentType = Bytes.toString(sentimentTypeByte);
        String sentimentKeywords = Bytes.toString(sentimentKeywordsByte);

        Record record = new Record();
        record.setId(articleId);

        Article article = new Article();
        article.setTitle(articleTitle);
        article.setDescription(articleDescription);
        article.setContent(articleContent);
        article.setPubDate(articlePubDate);
        article.setUrl(articleUrl);
        article.setImageUrl(articleImageUrl);
        article.setSource(articleSource);
        article.setCountry(articleCountry);
        article.setLanguage(articleLanguage);
        article.setAuthors(DI.OBJECT_MAPPER.readValue(articleAuthors, new TypeReference<List<String>>() {
        }));

        SentimentResponse sentimentResponse = new SentimentResponse();
        sentimentResponse.setRatio(sentimentRatio);
        sentimentResponse.setScore(sentimentScore);
        sentimentResponse.setType(sentimentType);
        sentimentResponse.setKeywords(DI.OBJECT_MAPPER.readValue(sentimentKeywords, new TypeReference<List<SentimentResponse.Keyword>>() {
        }));

        ArticleSentiment articleSentiment = new ArticleSentiment();
        articleSentiment.setArticle(article);
        articleSentiment.setSentimentResponse(sentimentResponse);

        record.setArticleSentiment(articleSentiment);

        return record;
    }

    public List<Record> get() throws IOException {
        Scan scan = new Scan();

        scan.addColumn(columnFamily, Bytes.toBytes("article_title"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_description"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_content"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_pub_date"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_url"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_image_url"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_source"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_country"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_language"));
        scan.addColumn(columnFamily, Bytes.toBytes("article_authors"));

        scan.addColumn(columnFamily, Bytes.toBytes("sentiment_ratio"));
        scan.addColumn(columnFamily, Bytes.toBytes("sentiment_score"));
        scan.addColumn(columnFamily, Bytes.toBytes("sentiment_type"));
        scan.addColumn(columnFamily, Bytes.toBytes("sentiment_keywords"));
        
        
        Table table = getTable();

        ResultScanner scanner = table.getScanner(scan);

        List<Record> results = new ArrayList<>();
        for (Result result = scanner.next(); result != null; result = scanner.next()) {
            results.add(from(result));
        }
        return results;
    }
}
