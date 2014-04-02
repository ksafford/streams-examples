package org.apache.streams.twitter.example;

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterUserstreamLocal {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterUserstreamLocal.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config twitter = StreamsConfigurator.config.getConfig("twitter");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);

        StreamBuilder builder = new LocalStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(100));

        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration, String.class);
        ConsolePersistWriter console = new ConsolePersistWriter();

        builder.newPerpetualStream("stream", stream);
        builder.addStreamsPersistWriter("console", console, 1, "stream");
        builder.start();

    }
}
