package org.apache.streams.twitter.example;

import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.utils.Utils;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.data.moreover.MoreoverConfigurator;
import org.apache.streams.data.moreover.MoreoverProvider;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.storm.trident.StreamsPersistWriterState;
import org.apache.streams.storm.trident.StreamsProviderSpout;
import org.apache.streams.storm.trident.StreamsTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class MoreoverMetabaseTopology extends StreamsTopology {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverMetabaseTopology.class);

    public static void main(String[] args)
    {

        Config config = StreamsConfigurator.config;
        Config storm = StreamsConfigurator.config.getConfig("storm");
        Config moreover = StreamsConfigurator.config.getConfig("moreover");
        Config kafka = StreamsConfigurator.config.getConfig("kafka");

        MoreoverConfiguration moreoverConfiguration = MoreoverConfigurator.detectConfiguration(moreover);
        KafkaConfiguration kafkaConfiguration = KafkaConfigurator.detectConfiguration(kafka);

        MoreoverMetabaseTopology topology = new MoreoverMetabaseTopology();

        MoreoverProvider moreoverProvider = new MoreoverProvider(moreoverConfiguration);
        StreamsProviderSpout spout = new StreamsProviderSpout(moreoverProvider);
        KafkaPersistWriter writer = new KafkaPersistWriter(kafkaConfiguration);
        StreamsPersistWriterState.Factory state = new StreamsPersistWriterState.Factory(writer, new StreamsPersistWriterState.StreamsPersistStateController());

        topology.newStream("firehose", spout)
                .parallelismHint(1)
                .partitionPersist(state, spout.getOutputFields(), new StreamsPersistWriterState.StreamsPersistWriterSendMessage())
                .parallelismHint(3);

        backtype.storm.Config stormConfig = topology.getStormConfig();
        LOGGER.info(stormConfig.toString());
        if(storm.getString("runmode").equals("local") ) {
            LocalCluster cluster = new LocalCluster();
            stormConfig.setMessageTimeoutSecs(120);
            stormConfig.setNumWorkers(2);
            stormConfig.setMaxSpoutPending(1);
            stormConfig.put(backtype.storm.Config.WORKER_CHILDOPTS, "-Dfile.encoding=UTF-8");
            cluster.submitTopology("test", stormConfig, topology.build());
            Utils.sleep(60 * 1000);
            cluster.killTopology("test");
            cluster.shutdown();
        } else if(storm.getString("runmode").equals("deploy") ) {
            stormConfig.setNumAckers(3);
            stormConfig.setMessageTimeoutSecs(90);
            try {
                StormSubmitter.submitTopology("moreover-metabase-kafka", stormConfig, topology.build());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.error("Please specify storm.runmode := [local,deploy]");
            LOGGER.error("  StreamsConfig was {}", config.toString());
        }

    }

}
