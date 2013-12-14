package org.apache.streams.twitter.example;

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterFirehoseStorm {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFirehoseStorm.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config twitterConfig = StreamsConfigurator.config.getConfig("twitter");
        Config kafkaConfig = StreamsConfigurator.config.getConfig("kafka");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitterConfig);
        KafkaConfiguration kafkaConfiguration = KafkaConfigurator.detectConfiguration(kafkaConfig);

        // replace with trident topology create / submit
        /*
        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration, Activity.class);
        KafkaPersister kafka = new KafkaPersister(stream.getOutQueue());

        try {
            Thread provider = (new Thread(stream));
            Thread persister = (new Thread(kafka));
            provider.start();
            persister.start();
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }
        */

        // run until user exits
    }
}
