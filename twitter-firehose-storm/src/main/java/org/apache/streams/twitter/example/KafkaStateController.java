package org.apache.streams.twitter.example;

import storm.trident.tuple.TridentTuple;

import java.io.Serializable;

/**
 * Created by sblackmon on 12/14/13.
 */
public class KafkaStateController implements Serializable {

    private String fieldName;

    public KafkaStateController() {
        this.fieldName = "json";
    }

    public KafkaStateController(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getMessage(TridentTuple tuple) {
        return tuple.getStringByField(this.fieldName);
    }
}
