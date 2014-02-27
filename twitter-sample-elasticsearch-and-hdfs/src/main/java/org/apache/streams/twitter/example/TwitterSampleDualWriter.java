package org.apache.streams.twitter.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.apache.hadoop.fs.Path;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.builders.LocalStreamBuilder;
import org.apache.streams.core.builders.StreamBuilder;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.hdfs.HdfsConfiguration;
import org.apache.streams.hdfs.HdfsConfigurator;
import org.apache.streams.hdfs.HdfsWriterConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.kafka.KafkaPersistReader;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.apache.streams.twitter.provider.TwitterTimelineProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterSampleDualWriter implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterSampleDualWriter.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        TwitterSampleDualWriter twitterSampleDualWriter = new TwitterSampleDualWriter();

        (new Thread(twitterSampleDualWriter)).start();

    }

    @Override
    public void run() {

        Config twitter = StreamsConfigurator.config.getConfig("twitter");
        Config hdfs = StreamsConfigurator.config.getConfig("hdfs");
        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        StreamBuilder builder = new LocalStreamBuilder(new LinkedBlockingQueue<StreamsDatum>());

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);
        HdfsConfiguration hdfsConfiguration = HdfsConfigurator.detectConfiguration(hdfs);
        ElasticsearchConfiguration elasticsearchConfiguration = ElasticsearchConfigurator.detectConfiguration(elasticsearch);

        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration, Activity.class);

        HdfsWriterConfiguration hdfsWriterConfiguration  = mapper.convertValue(hdfsConfiguration, HdfsWriterConfiguration.class);
        hdfsWriterConfiguration.setWriterPath(TwitterTimelineProvider.STREAMS_ID);
        hdfsWriterConfiguration.setWriterFilePrefix("data");

        WebHdfsPersistWriter hdfsWriter = new WebHdfsPersistWriter(hdfsConfiguration);

        ElasticsearchWriterConfiguration elasticsearchWriterConfiguration  = mapper.convertValue(elasticsearchConfiguration, ElasticsearchWriterConfiguration.class);
        elasticsearchWriterConfiguration.setIndex("activity_tweet_timeline");
        elasticsearchWriterConfiguration.setType("activity");

        ElasticsearchPersistWriter elasticsearchWriter = new ElasticsearchPersistWriter(elasticsearchWriterConfiguration);

        builder.newReadCurrentStream(TwitterStreamProvider.STREAMS_ID, stream);
        builder.addStreamsPersistWriter(WebHdfsPersistWriter.STREAMS_ID, hdfsWriter, 1, TwitterStreamProvider.STREAMS_ID);
        builder.addStreamsPersistWriter(ElasticsearchPersistWriter.STREAMS_ID, elasticsearchWriter, 1, TwitterStreamProvider.STREAMS_ID);
    }
}
