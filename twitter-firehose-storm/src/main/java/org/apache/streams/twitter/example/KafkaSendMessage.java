package org.apache.streams.twitter.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

import java.util.List;

/**
 * Created by sblackmon on 12/14/13.
 */
public class KafkaSendMessage extends BaseStateUpdater<KafkaWriterState> {

    private Logger logger = LoggerFactory.getLogger(KafkaSendMessage.class);

    @Override
    public void updateState(KafkaWriterState kafkaState, List<TridentTuple> tridentTuples, TridentCollector tridentCollector) {
        this.logger.debug("****  calling send message. .  .");
        kafkaState.bulkMessagesToKafka(tridentTuples);
    }
}
