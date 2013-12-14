package org.apache.streams.twitter.example;

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.kafka.repository.impl.ConsoleRepository;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterSampleStandalone {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterSampleStandalone.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config twitter = StreamsConfigurator.config.getConfig("twitter");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);

        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration, Activity.class);
        ConsoleRepository console = new ConsoleRepository(stream.getOutQueue());

        try {
            Thread provider = (new Thread(stream));
            Thread persister = (new Thread(console));
            provider.start();
            persister.start();
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }

        // run until user exits
    }
}
