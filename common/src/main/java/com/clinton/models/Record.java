package com.clinton.models;

import com.clinton.DI;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record implements Serializable {
    private String id;
    private ArticleSentiment articleSentiment;

    public static Record parse(ConsumerRecord<byte[], byte[]> record) {
        try {
            String key = DI.objecMapper.writeValueAsString(record.key());
            ArticleSentiment articleSentiment = DI.objecMapper.readValue(record.value(), ArticleSentiment.class);
            return new Record(key, articleSentiment);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
