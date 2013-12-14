package org.apache.streams.twitter.example;

import backtype.storm.task.IMetricsContext;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.tuple.TridentTuple;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sblackmon on 12/14/13.
 */
public class KafkaWriterState implements State {

    private Logger logger;
    private KafkaStateController controller;
    private KafkaConfiguration kafkaConfig;

    private BlockingQueue<Object> outQueue;

    public KafkaWriterState(KafkaConfiguration kafkaConfig, KafkaStateController controller) {
        this.logger = LoggerFactory.getLogger(KafkaWriterState.class);
        this.kafkaConfig = kafkaConfig;
        this.controller = controller;
        this.logger.info("Starting Kafka stat with brokers : {}", kafkaConfig.getBrokerlist());
        outQueue = new LinkedBlockingQueue<Object>(1000);
        KafkaPersistWriter kafkaPersistWriter = new KafkaPersistWriter(this.kafkaConfig, outQueue);
        new Thread(kafkaPersistWriter).start();
    }

    public void bulkMessagesToKafka(List<TridentTuple> tuples) {
        for (TridentTuple tuple : tuples) {
            String message = this.controller.getMessage(tuple);
            try {
                outQueue.offer(message);
            } catch (Exception e) {
                this.logger.error("Exception while passing message to kafka : {}", e, message);
            }
        }
        this.logger.debug("******** Ending commit to kafka . . .");
    }

    @Override
    public void beginCommit(Long aLong) {

    }

    @Override
    public void commit(Long aLong) {

    }

    public static class Factory implements StateFactory {

        private Logger logger;
        private KafkaConfiguration config;
        private KafkaStateController controller;

        public Factory(KafkaConfiguration config, KafkaStateController controller) {
            this.config = config;
            this.controller = controller;
            this.logger = LoggerFactory.getLogger(Factory.class);
        }

        @Override
        public State makeState(Map map, IMetricsContext iMetricsContext, int i, int i2) {
            this.logger.debug("Called makeState. . . ");
            return new KafkaWriterState(config, controller);
        }

    }

}
