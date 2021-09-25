package com.clinton;

import com.clinton.models.Article;
import com.clinton.models.ArticleSentiment;
import com.clinton.models.Record;
import com.clinton.models.SentimentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
    private final static String SERVICE_NAME = "hot-topic-analysis-service";
    private static final String KAFKA_SERVER = "LISTENER_DOCKER_INTERNAL://kafka1:19092";
    private static final String KAFKA_CLIENT_ID = "3";
    private static final String SENTIMENT_KAFKA_TOPIC = "sentiment-analysis";
    private static final String KAFKA_GROUP_ID = "3";
    private static final String HDFS_HOST = "hdfs://namenode:9000";

    private static final String HBASE_TABLE_NAME = "sentimental-news-highlights-table";
    private static final String HBASE_COLUMN_FAMILY = "sentimental-news-highlights-cf";
    private static final String HBASE_CONFIG_FILE = "/app/hbase-site.xml";

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName(SERVICE_NAME);

        JavaSparkContext jsc = new JavaSparkContext(conf);

        Map<String, Object> kafkaParams = kafkaConfiguration();
        List<String> topics = Collections.singletonList(SENTIMENT_KAFKA_TOPIC);

        JavaStreamingContext scc = new JavaStreamingContext(jsc, Durations.seconds(10));
        scc.checkpoint(HDFS_HOST + "/checkpoint");
        setUpHbaseTable();

        JavaInputDStream<ConsumerRecord<byte[], byte[]>> inputDStream =
                KafkaUtils.createDirectStream(
                        scc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaParams));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown started...");
                HBaseConnectionPool.shutdown();
                jsc.stop();
                System.out.println("Shutdown finished");
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }));

        inputDStream
                .map(Record::parse)
                .window(Durations.seconds(60), Durations.seconds(30))
                .reduce((record1, record2) -> {
                    SentimentResponse response1 = record1.getArticleSentiment().getSentimentResponse();
                    SentimentResponse response2 = record2.getArticleSentiment().getSentimentResponse();
                    if (response1.compareTo(response2) > 0) return record1;
                    return record2;
                })
                .foreachRDD(rdd -> rdd.foreachPartition(partition -> {
                    Configuration configuration = createConfiguration();
                    Connection connection = ConnectionFactory.createConnection(configuration);
                    Table table = connection.getTable(TableName.valueOf(HBASE_TABLE_NAME));
                    partition.forEachRemaining(record -> saveRecord(table, record));
                    table.close();
                    connection.close();
                }));

//        hbaseContext.streamBulkPut(newsDStream, TableName.valueOf(HBASE_TABLE_NAME), new PutFunction());
        scc.start();
        scc.awaitTermination();
    }

    private static Map<String, Object> kafkaConfiguration() {
        Map<String, Object> params = new HashMap<>();
        params.put("bootstrap.servers", KAFKA_SERVER);
        params.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        params.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        params.put("group.id", KAFKA_GROUP_ID);
        params.put("auto.offset.reset", "earliest");
        params.put("enable.auto.commit", true);
        params.put(ConsumerConfig.CLIENT_ID_CONFIG, KAFKA_CLIENT_ID);
        return params;
    }

    private static void setUpHbaseTable() {
        Configuration hbaseConfig = createConfiguration();

        try (Connection connection = ConnectionFactory.createConnection(hbaseConfig);
             Admin admin = connection.getAdmin()
        ) {
            HTableDescriptor table = new HTableDescriptor(TableName.valueOf(HBASE_TABLE_NAME));
            table.addFamily(new HColumnDescriptor(HBASE_COLUMN_FAMILY).setCompressionType(Compression.Algorithm.NONE));

            System.out.println("Setting up table.... ");

            if (!admin.tableExists(table.getTableName())) {
                admin.createTable(table);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Put getPut(Record record) throws JsonProcessingException {
        final byte[] columnFamily = Bytes.toBytes(HBASE_COLUMN_FAMILY);
        ArticleSentiment articleSentiment = record.getArticleSentiment();
        Article article = articleSentiment.getArticle();
        SentimentResponse sentiment = articleSentiment.getSentimentResponse();

        Put put = new Put(Bytes.toBytes(record.getId()));

        put.addColumn(columnFamily, Bytes.toBytes("article_title"), Bytes.toBytes(article.getTitle()));
        put.addColumn(columnFamily, Bytes.toBytes("article_description"), Bytes.toBytes(article.getDescription()));
        put.addColumn(columnFamily, Bytes.toBytes("article_content"), Bytes.toBytes(article.getContent()));
        put.addColumn(columnFamily, Bytes.toBytes("article_pub_date"), Bytes.toBytes(article.getPubDate()));
        put.addColumn(columnFamily, Bytes.toBytes("article_url"), Bytes.toBytes(article.getUrl()));
        put.addColumn(columnFamily, Bytes.toBytes("article_image_url"), Bytes.toBytes(article.getImageUrl()));
        put.addColumn(columnFamily, Bytes.toBytes("article_source"), Bytes.toBytes(article.getSource()));
        put.addColumn(columnFamily, Bytes.toBytes("article_country"), Bytes.toBytes(article.getCountry()));
        put.addColumn(columnFamily, Bytes.toBytes("article_language"), Bytes.toBytes(article.getLanguage()));
        put.addColumn(columnFamily, Bytes.toBytes("article_authors"), Bytes.toBytes(DI.OBJECT_MAPPER.writeValueAsString(article.getAuthors())));

        put.addColumn(columnFamily, Bytes.toBytes("sentiment_ratio"), Bytes.toBytes(sentiment.getRatio()));
        put.addColumn(columnFamily, Bytes.toBytes("sentiment_score"), Bytes.toBytes(sentiment.getScore()));
        put.addColumn(columnFamily, Bytes.toBytes("sentiment_type"), Bytes.toBytes(sentiment.getType()));
        put.addColumn(columnFamily, Bytes.toBytes("sentiment_keywords"), Bytes.toBytes(DI.OBJECT_MAPPER.writeValueAsString(sentiment.getKeywords())));

        return put;
    }

    private static void saveRecord(Table table, Record record) {
        try {
            Put put = getPut(record);
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Configuration createConfiguration() {
        Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.addResource(new Path(HBASE_CONFIG_FILE));
        return hbaseConfig;
    }
}
