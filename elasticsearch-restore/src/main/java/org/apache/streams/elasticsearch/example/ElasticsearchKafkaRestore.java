package org.apache.streams.elasticsearch.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
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
import org.apache.streams.hdfs.HdfsReaderConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistReader;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaConfigurator;
import org.apache.streams.kafka.KafkaPersistReader;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by sblackmon on 12/10/13.
 */
public class ElasticsearchKafkaRestore {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchKafkaRestore.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    private static String index;
    private static String type;

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        detectConfiguration();

        Config hdfs = StreamsConfigurator.config.getConfig("hdfs");
        HdfsConfiguration hdfsConfiguration = HdfsConfigurator.detectConfiguration(hdfs);

        HdfsReaderConfiguration hdfsReaderConfiguration  = mapper.convertValue(hdfsConfiguration, HdfsReaderConfiguration.class);
        hdfsReaderConfiguration.setReaderPath(index + "/twitter/statusdedup");

        WebHdfsPersistReader hdfsReader = new WebHdfsPersistReader(hdfsReaderConfiguration);

        Config kafka = StreamsConfigurator.config.getConfig("kafka");
        KafkaConfiguration kafkaConfiguration = KafkaConfigurator.detectConfiguration(kafka);
        KafkaPersistWriter kafkaPersistWriter = new KafkaPersistWriter();
        kafkaPersistWriter.setConfig(kafkaConfiguration);

        KafkaPersistReader kafkaPersistReader = new KafkaPersistReader();
        kafkaPersistReader.setConfig(kafkaConfiguration);

        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");
        ElasticsearchConfiguration elasticsearchConfiguration = ElasticsearchConfigurator.detectConfiguration(elasticsearch);
        ElasticsearchWriterConfiguration elasticsearchWriterConfiguration  = mapper.convertValue(elasticsearchConfiguration, ElasticsearchWriterConfiguration.class);
        elasticsearchWriterConfiguration.setIndex(index + "_" + type);
        elasticsearchWriterConfiguration.setType(type);

        ElasticsearchPersistWriter elasticsearchWriter = new ElasticsearchPersistWriter(elasticsearchWriterConfiguration);

        StreamBuilder builder = new LocalStreamBuilder(new ConcurrentLinkedQueue<StreamsDatum>());

        builder.newPerpetualStream(WebHdfsPersistReader.STREAMS_ID, hdfsReader);
        builder.addStreamsPersistWriter(kafkaConfiguration.getTopic(), kafkaPersistWriter, 1, WebHdfsPersistReader.STREAMS_ID);
        builder.newPerpetualStream(kafkaConfiguration.getTopic(), kafkaPersistReader);
        builder.addStreamsPersistWriter(ElasticsearchPersistWriter.STREAMS_ID, elasticsearchWriter, 1, kafkaConfiguration.getTopic());
        builder.start();

    }

    private static void detectConfiguration() {

        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        Config restore = elasticsearch.getConfig("restore");

        index = restore.getString("index");

        type = restore.getString("type");

    }


}
