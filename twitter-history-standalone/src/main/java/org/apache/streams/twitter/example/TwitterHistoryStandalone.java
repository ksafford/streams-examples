package org.apache.streams.twitter.example;

import com.typesafe.config.Config;
import org.apache.hadoop.fs.Path;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.hdfs.HdfsConfiguration;
import org.apache.streams.hdfs.HdfsConfigurator;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterTimelineProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterHistoryStandalone implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterHistoryStandalone.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        TwitterHistoryStandalone twitterHistoryStandalone = new TwitterHistoryStandalone();

        (new Thread(twitterHistoryStandalone)).start();
        // run until no more data?  TODO: confirm
    }

    @Override
    public void run() {

        Config twitter = StreamsConfigurator.config.getConfig("twitter");
        Config hdfs = StreamsConfigurator.config.getConfig("hdfs");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);
        HdfsConfiguration hdfsConfiguration = HdfsConfigurator.detectConfiguration(hdfs);

        TwitterTimelineProvider provider = new TwitterTimelineProvider(twitterStreamConfiguration, Activity.class);
        WebHdfsPersistWriter writer = new WebHdfsPersistWriter(hdfsConfiguration, provider.getProviderQueue(), new Path("timeline"), "data");

        Thread providerThread = new Thread(provider);
        Thread writerThread = new Thread(writer);
        try {
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

        writer.terminate = true;

        while( writerThread.isAlive() ) {
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) { }
        }

    }
}
