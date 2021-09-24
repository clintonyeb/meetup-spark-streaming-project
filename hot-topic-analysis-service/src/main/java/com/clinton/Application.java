package com.clinton;

import com.clinton.models.Record;
import com.clinton.models.SentimentResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
    //    private final static String SPARK_MASTER_URL = "spark://spark-master:7077";
    private final static String SERVICE_NAME = "hot-topic-analysis-service";
    private static final String KAFKA_SERVER = "LISTENER_DOCKER_INTERNAL://kafka1:19092";
    private static final String KAFKA_CLIENT_ID = "3";
    private static final String SENTIMENT_KAFKA_TOPIC = "sentiment-analysis";
    private static final String KAFKA_GROUP_ID = "3";
    private static final String HDFS_HOST = "file://";

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setAppName(SERVICE_NAME);

        Map<String, Object> kafkaParams = kafkaConfiguration();
        List<String> topics = Collections.singletonList(SENTIMENT_KAFKA_TOPIC);


        JavaStreamingContext scc = new JavaStreamingContext(conf, Durations.seconds(1));
        scc.checkpoint(HDFS_HOST + "/checkpoint");

        JavaInputDStream<ConsumerRecord<byte[], byte[]>> articleAndSentiments =
                KafkaUtils.createDirectStream(
                        scc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaParams));

        articleAndSentiments
                .map(Record::parse)
                .window(Durations.seconds(30), Durations.seconds(10))
                .reduce((record1, record2) -> {
                    SentimentResponse response1 = record1.getArticleSentiment().getSentimentResponse();
                    SentimentResponse response2 = record2.getArticleSentiment().getSentimentResponse();
                    if (response1.compareTo(response2) > 0) return record1;
                    return record2;
                })
                .foreachRDD(rdd -> rdd
                        .map(Record::toString)
                        .saveAsTextFile("file://" + "/output")
                );

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
        params.put("enable.auto.commit", false);
        params.put(ConsumerConfig.CLIENT_ID_CONFIG, KAFKA_CLIENT_ID);
        return params;
    }
}
