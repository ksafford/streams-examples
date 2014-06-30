package org.apache.streams.instagram.example;

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.InstagramConfigurator;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.apache.streams.instagram.processor.InstagramTypeConverter;
import org.apache.streams.instagram.provider.InstagramTimelineProvider;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;


public class InstagramTimelineConsole {

    private final static Logger LOGGER = LoggerFactory.getLogger(InstagramTimelineConsole.class);

    public static void main(String[] args) {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config instagram = StreamsConfigurator.config.getConfig("instagram");

        InstagramUserInformationConfiguration instagramConfiguration = InstagramConfigurator.detectInstagramUserInformationConfiguration(instagram);

        StreamBuilder builder = new LocalStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(100));

        InstagramTimelineProvider stream = new InstagramTimelineProvider(instagramConfiguration);
        InstagramTypeConverter converter = new InstagramTypeConverter(ObjectNode.class, String.class);
        ConsolePersistWriter console = new ConsolePersistWriter();

        builder.newPerpetualStream(InstagramTimelineProvider.STREAMS_ID, stream);
        builder.addStreamsProcessor("converter", converter, 2, InstagramTimelineProvider.STREAMS_ID);
        builder.addStreamsPersistWriter("console", console, 1, "converter");
        builder.start();

    }
}



