package org.apache.streams.twitter.example;

import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.task.IMetricsContext;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.data.moreover.MoreoverConfigurator;
import org.apache.streams.data.moreover.MoreoverProvider;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.storm.trident.StreamsTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;
import storm.trident.state.BaseStateUpdater;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.tuple.TridentTuple;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sblackmon on 12/10/13.
 */
public class MoreoverMetabaseTopology extends StreamsTopology {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverMetabaseTopology.class);

    public static void main(String[] args)
    {

        Config config = StreamsConfigurator.config;
        Config moreover = StreamsConfigurator.config.getConfig("moreover");
        Config kafka = StreamsConfigurator.config.getConfig("kafka");

        MoreoverConfiguration moreoverConfiguration = MoreoverConfigurator.detectConfiguration(moreover);
        KafkaConfiguration kafkaConfiguration = KafkaConfigurator.detectConfiguration(kafka);

        MoreoverMetabaseTopology topology = new MoreoverMetabaseTopology();

        MoreoverProviderSpout spout = new MoreoverProviderSpout(moreoverConfiguration);
        KafkaWriterState.Factory writer = new KafkaWriterState.Factory(kafkaConfiguration, new KafkaStateController());

        topology.newStream("firehose", spout)
                .parallelismHint(1)
                .partitionPersist(writer, spout.getOutputFields(), new KafkaSendMessage())
                .parallelismHint(3);

        backtype.storm.Config stormConfig = topology.getStormConfig();
        LOGGER.info(stormConfig.toString());
        if(stormConfig.size() == 0 ) {
            LocalCluster cluster = new LocalCluster();
            stormConfig.setMessageTimeoutSecs(120);
            stormConfig.setNumWorkers(2);
            stormConfig.setMaxSpoutPending(1);
            stormConfig.put(backtype.storm.Config.WORKER_CHILDOPTS, "-Dfile.encoding=UTF-8");
            cluster.submitTopology("test", stormConfig, topology.build());
            Utils.sleep(10 * 1000);
            cluster.killTopology("test");
            cluster.shutdown();
        } else {
            stormConfig.setNumAckers(3);
            stormConfig.setMessageTimeoutSecs(90);
            try {
                StormSubmitter.submitTopology("moreover-metabase-kafka", stormConfig, topology.build());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                e.printStackTrace();
            }
        }

    }

    public static class MoreoverProviderSpout implements IBatchSpout {

        private Logger logger;
        MoreoverConfiguration moreoverConfiguration;

        public MoreoverProviderSpout(MoreoverConfiguration moreoverConfiguration) {
            this.moreoverConfiguration = moreoverConfiguration;
        }

        MoreoverProvider provider;
        @Override
        public void open(Map map, TopologyContext topologyContext) {
            provider = new MoreoverProvider(moreoverConfiguration);
            provider.start();
        }

        @Override
        public synchronized void emitBatch(long l, TridentCollector tridentCollector) {
            List<StreamsDatum> batch;
            batch = IteratorUtils.toList(provider.getProviderQueue().iterator());
            for( StreamsDatum datum : batch ) {
                tridentCollector.emit( Lists.newArrayList(
                    datum.getTimestamp(),
                    datum.getSequenceid(),
                    datum.getDocument()
                ));
            }
        }

        @Override
        public void ack(long l) {

        }

        @Override
        public void close() {
            provider.stop();
        }

        @Override
        public Map getComponentConfiguration() {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> mapConfig;
            mapConfig = mapper.convertValue(moreoverConfiguration, Map.class);
            return mapConfig;
        }

        @Override
        public Fields getOutputFields() {
            return new Fields("timestamp", "sequenceid", "datum");
        }
    };

    public static class KafkaWriterState implements State {

        private Logger logger;
        private KafkaStateController controller;
        private KafkaConfiguration kafkaConfig;

        private Queue<StreamsDatum> outQueue;

        public KafkaWriterState(KafkaConfiguration kafkaConfig, KafkaStateController controller) {
            this.logger = LoggerFactory.getLogger(KafkaWriterState.class);
            this.kafkaConfig = kafkaConfig;
            this.controller = controller;
            this.logger.info("Starting Kafka stat with brokers : {}", kafkaConfig.getBrokerlist());
            KafkaPersistWriter kafkaPersistWriter = new KafkaPersistWriter(this.kafkaConfig, outQueue);
            kafkaPersistWriter.start();
        }

        public void bulkMessagesToKafka(List<TridentTuple> tuples) {
            for (TridentTuple tuple : tuples) {
                StreamsDatum datum = this.controller.fromTuple(tuple);
                try {
                    outQueue.offer(datum);
                } catch (Exception e) {
                    this.logger.error("Exception while passing message to kafka : {}", e, datum);
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

    public static class KafkaStateController implements Serializable {

        private String fieldName;
        private ObjectMapper mapper = new ObjectMapper();

        public KafkaStateController() {
            this.fieldName = "datum";
        }

        public KafkaStateController(String fieldName) {
            this.fieldName = fieldName;
        }

        public StreamsDatum fromTuple(TridentTuple tuple) {
            return mapper.convertValue(tuple.getValueByField(this.fieldName), StreamsDatum.class);
        }

    }

    public static class KafkaSendMessage extends BaseStateUpdater<KafkaWriterState> {

        private Logger logger = LoggerFactory.getLogger(KafkaSendMessage.class);

        @Override
        public void updateState(KafkaWriterState kafkaState, List<TridentTuple> tridentTuples, TridentCollector tridentCollector) {
            this.logger.debug("****  calling send message. .  .");
            kafkaState.bulkMessagesToKafka(tridentTuples);
        }
    }
}
