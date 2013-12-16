package org.apache.streams.twitter.example;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.storm.trident.StreamsTopology;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterFirehoseTopology extends StreamsTopology {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFirehoseTopology.class);

    Config config;

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("twitter"));
        KafkaConfiguration kafkaConfiguration = KafkaConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("kafka"));

        TwitterFirehoseTopology topology = new TwitterFirehoseTopology();

        topology.newStream("firehose", new TwitterFirehoseSpout())
                .parallelismHint(1)
                .partitionPersist(new KafkaWriterState.Factory(kafkaConfiguration, new KafkaStateController()),
                        new Fields("json"), new KafkaSendMessage())
                .parallelismHint(3);

        String runmode = topology.getRunmode();
        Config stormConfig = topology.getStormConfig();

        if (runmode.equalsIgnoreCase("local")) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", stormConfig, topology.build());
            Utils.sleep(10 * 1000);
            cluster.killTopology("test");
            cluster.shutdown();
        } else if(runmode.equalsIgnoreCase("deploy")){
            stormConfig.setNumAckers(3);
            stormConfig.setMessageTimeoutSecs(90);
            try {
                StormSubmitter.submitTopology("twitter-firehose-kafka", stormConfig, topology.build());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                e.printStackTrace();
            }
        }

        // run until user exits
    }
}
