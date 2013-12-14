package org.apache.streams.twitter.example;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by sblackmon on 12/14/13.
 */
public class TwitterFirehoseSpout extends BaseRichSpout {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFirehoseSpout.class);

    TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("twitter"));

    TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration, Activity.class);

    Thread provider;

    SpoutOutputCollector collector;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //  TODO: change to use all three fields a persistwriter will need
        //outputFieldsDeclarer.declare( new Fields("id", "datetime", "json"));
        outputFieldsDeclarer.declare( new Fields("json"));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.collector = spoutOutputCollector;
        try {
            provider = (new Thread(stream));
            provider.start();
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }
    }

    @Override
    public void nextTuple() {
        try {
            collector.emit( new Values(stream.getOutQueue().take()) );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
