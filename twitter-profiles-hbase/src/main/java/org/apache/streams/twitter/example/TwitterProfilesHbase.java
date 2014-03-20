package org.apache.streams.twitter.example;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.hbase.HbasePersistWriter;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterProfileProcessor;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterProfilesHbase {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterProfilesHbase.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config twitter = StreamsConfigurator.config.getConfig("twitter");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);

        TwitterStreamProvider provider = new TwitterStreamProvider(twitterStreamConfiguration, ObjectNode.class);
        TwitterProfileProcessor profile = new TwitterProfileProcessor(provider.getProviderQueue());
        HbasePersistWriter writer = new HbasePersistWriter(profile.getProcessorOutputQueue());

        Thread providerThread = new Thread(provider);
        Thread profileThread = new Thread(profile);
        Thread writerThread = new Thread(writer);

        try {
            profileThread.start();
            writerThread.start();
            providerThread.start();
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }

        while( providerThread.isAlive() ) {
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) { }
        }
        profileThread.stop();
        writerThread.stop();
    }
}
