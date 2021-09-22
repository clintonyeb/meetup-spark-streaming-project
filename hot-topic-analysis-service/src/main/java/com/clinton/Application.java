package com.clinton;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Application {
    private static final String MESSAGE_TOPIC_ENV = "KAFKA_TOPIC";
    private static final String KAFKA_SERVER = "KAFKA_LISTENER";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static final String KAFKA_TOPIC = "KAFKA_TOPIC";

    final static String sparkMasterUrl = Util.getEnv("SPARK_MASTER_URL");
    final static String SERVICE_NAME = Util.getEnv("SERVICE_NAME");

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder().appName(SERVICE_NAME).master(sparkMasterUrl).getOrCreate();
//        JavaSparkContext spark = new JavaSparkContext(new SparkConf().setAppName(SERVICE_NAME).setMaster(sparkMasterUrl));

        Dataset<Row> df = spark
                .read()
                .format("kafka")
                .option("kafka.bootstrap.servers", Util.getEnv(KAFKA_SERVER))
                .option("subscribe", Util.getEnv(KAFKA_TOPIC))
                .load();
        df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");

        spark.stop();
    }
}
