package org.apache.streams.elasticsearch.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.*;
import org.apache.streams.hdfs.*;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by sblackmon on 12/10/13.
 */
public class ElasticsearchBackup {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchBackup.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    private static String index;
    private static String type;

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        detectConfiguration();

        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");
        ElasticsearchConfiguration elasticsearchConfiguration = ElasticsearchConfigurator.detectConfiguration(elasticsearch);
        ElasticsearchReaderConfiguration elasticsearchReaderConfiguration  = mapper.convertValue(elasticsearchConfiguration, ElasticsearchReaderConfiguration.class);
        elasticsearchReaderConfiguration.setIndex(index + "_" + type);
        elasticsearchReaderConfiguration.setType(type);

        ElasticsearchPersistReader elasticsearchReader = new ElasticsearchPersistReader(elasticsearchReaderConfiguration);

        Config hdfs = StreamsConfigurator.config.getConfig("hdfs");
        HdfsConfiguration hdfsConfiguration = HdfsConfigurator.detectConfiguration(hdfs);

        HdfsWriterConfiguration hdfsWriterConfiguration  = mapper.convertValue(hdfsConfiguration, HdfsWriterConfiguration.class);
        hdfsWriterConfiguration.setWriterPath(index + "/esbackup/" + index + "/" + type);

        WebHdfsPersistWriter hdfsWriter = new WebHdfsPersistWriter(hdfsWriterConfiguration);

        StreamBuilder builder = new LocalStreamBuilder(new ConcurrentLinkedQueue<StreamsDatum>());

        builder.newPerpetualStream(ElasticsearchPersistReader.STREAMS_ID, elasticsearchReader);
        builder.addStreamsPersistWriter(WebHdfsPersistReader.STREAMS_ID, hdfsWriter, 1, ElasticsearchPersistWriter.STREAMS_ID);
        builder.start();

    }

    private static void detectConfiguration() {

        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        Config restore = elasticsearch.getConfig("backup");

        index = restore.getString("index");

        type = restore.getString("type");

    }


}
