package org.apache.streams.gmail.example;

import com.google.gmail.GMailConfiguration;
import com.google.gmail.GMailConfigurator;
import com.google.gmail.provider.GMailProvider;
import com.googlecode.gmail4j.GmailMessage;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.console.ConsolePersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GMailExportStandalone {

    private final static Logger LOGGER = LoggerFactory.getLogger(GMailExportStandalone.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config gmail = StreamsConfigurator.config.getConfig("gmail");

        GMailConfiguration gmailConfiguration = GMailConfigurator.detectConfiguration(gmail);

        GMailProvider stream = new GMailProvider(gmailConfiguration, GmailMessage.class);
        ConsolePersistWriter console = new ConsolePersistWriter(stream.getProviderQueue());

        try {
            stream.start();
            console.start();
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }

        // run until user exits
    }
}
