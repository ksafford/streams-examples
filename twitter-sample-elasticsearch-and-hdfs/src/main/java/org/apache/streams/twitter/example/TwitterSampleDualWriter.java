package org.apache.streams.twitter.example;

import com.typesafe.config.Config;
import org.apache.hadoop.fs.Path;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.hdfs.HdfsConfiguration;
import org.apache.streams.hdfs.HdfsConfigurator;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.kafka.KafkaPersistReader;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterSampleDualWriter implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterSampleDualWriter.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        TwitterSampleDualWriter twitterSampleDualWriter = new TwitterSampleDualWriter();

        (new Thread(twitterSampleDualWriter)).start();

    }

    @Override
    public void run() {

        Config twitter = StreamsConfigurator.config.getConfig("twitter");
        Config kafka = StreamsConfigurator.config.getConfig("kafka");
        Config hdfs = StreamsConfigurator.config.getConfig("hdfs");
        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);
        HdfsConfiguration hdfsConfiguration = HdfsConfigurator.detectConfiguration(hdfs);
        ElasticsearchConfiguration elasticsearchConfiguration = ElasticsearchConfigurator.detectConfiguration(elasticsearch);
        KafkaConfiguration kafkaConfiguration = KafkaConfigurator.detectConfiguration(kafka);

        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration, Activity.class);

        KafkaPersistWriter kafkaWriter = new KafkaPersistWriter(kafkaConfiguration, stream.getProviderQueue());

        KafkaPersistReader kafkaHdfsReader = new KafkaPersistReader(kafkaConfiguration);
        KafkaPersistReader kafkaElasticsearchReader = new KafkaPersistReader(kafkaConfiguration);

        WebHdfsPersistWriter hdfsWriter = new WebHdfsPersistWriter(hdfsConfiguration, kafkaHdfsReader.getPersistQueue(), new Path("timeline"), "data");
        ElasticsearchPersistWriter elasticsearchWriter = new ElasticsearchPersistWriter(elasticsearchConfiguration, kafkaElasticsearchReader.getPersistQueue(), "activity_apache", "activity");

        Thread providerThread = new Thread(stream);
        Thread kafkaWriterThread = new Thread(kafkaWriter);
        Thread kafkaHdfsReaderThread = new Thread(kafkaHdfsReader);
        Thread kafkaElasticsearchReaderThread = new Thread(kafkaHdfsReader);
        Thread hdfsWriterThread = new Thread(hdfsWriter);
        Thread elasticsearchWriterThread = new Thread(elasticsearchWriter);
        try {
            hdfsWriterThread.start();
            elasticsearchWriterThread.start();
            kafkaHdfsReaderThread.start();
            kafkaElasticsearchReaderThread.start();
            kafkaWriterThread.start();
            providerThread.start();
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }

        while( providerThread.isAlive() ) {
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) { }
        }
        kafkaWriterThread.stop();
        kafkaHdfsReaderThread.stop();
        kafkaElasticsearchReaderThread.stop();
        hdfsWriterThread.stop();
        elasticsearchWriterThread.stop();
    }
}
