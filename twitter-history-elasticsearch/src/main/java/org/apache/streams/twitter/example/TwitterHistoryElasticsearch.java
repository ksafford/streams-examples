package org.apache.streams.twitter.example;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamConfigurator;
import org.apache.streams.twitter.provider.TwitterTimelineProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterHistoryElasticsearch implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterHistoryElasticsearch.class);

    ListenableFuture providerTaskComplete;
    ListenableFuture writerTaskComplete;

    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        TwitterHistoryElasticsearch twitterHistoryElasticsearch = new TwitterHistoryElasticsearch();

        (new Thread(twitterHistoryElasticsearch)).start();
        // run until no more data?  TODO: confirm
    }

    @Override
    public void run() {

        Config twitter = StreamsConfigurator.config.getConfig("twitter");
        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        TwitterStreamConfiguration twitterStreamConfiguration = TwitterStreamConfigurator.detectConfiguration(twitter);
        ElasticsearchConfiguration hdfsConfiguration = ElasticsearchConfigurator.detectConfiguration(elasticsearch);

        TwitterTimelineProvider provider = new TwitterTimelineProvider(twitterStreamConfiguration, Activity.class);
        ElasticsearchPersistWriter writer = new ElasticsearchPersistWriter(hdfsConfiguration, provider.getProviderQueue(), "activity_apache", "activity");

        Thread providerThread = new Thread(provider);
        Thread writerThread = new Thread(writer);
        try {
            providerTaskComplete = executor.submit(providerThread);
            writerTaskComplete = executor.submit(writerThread);
        } catch( Exception x ) {
            LOGGER.info(x.getMessage());
        }

        try {
            providerTaskComplete.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }

        try {
            writerTaskComplete.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }
    }

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
