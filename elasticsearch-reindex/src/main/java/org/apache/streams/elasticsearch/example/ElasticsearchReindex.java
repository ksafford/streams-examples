package org.apache.streams.elasticsearch.example;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchPersistReader;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class ElasticsearchReindex implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchReindex.class);

    private String[] sourceindices;
    private String[] sourcetypes;
    private String destinationindex;
    private String destinationtype;

    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

    ListenableFuture providerTaskComplete;
    ListenableFuture writerTaskComplete;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        ElasticsearchReindex elasticsearchReindex = new ElasticsearchReindex();

        (new Thread(elasticsearchReindex)).start();

    }

    @Override
    public void run() {

        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        ElasticsearchConfiguration elasticsearchConfiguration = ElasticsearchConfigurator.detectConfiguration(elasticsearch);

        this.detectConfiguration();

        ElasticsearchPersistReader elasticsearchPersistReader = new ElasticsearchPersistReader(elasticsearchConfiguration, sourceindices, sourcetypes);
        ElasticsearchPersistWriter elasticsearchPersistWriter = new ElasticsearchPersistWriter(elasticsearchConfiguration, elasticsearchPersistReader.getPersistQueue(), destinationindex, destinationtype);

        try {
            providerTaskComplete = executor.submit(elasticsearchPersistReader);
            writerTaskComplete = executor.submit(elasticsearchPersistWriter);
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

    private void detectConfiguration() {

        Config elasticsearch = StreamsConfigurator.config.getConfig("elasticsearch");

        Config reindex = elasticsearch.getConfig("reindex");

        Config source = reindex.getConfig("source");

        sourceindices = source.getStringList("indices").toArray(new String[0]);
        sourcetypes = source.getStringList("types").toArray(new String[0]);;

        Config destination = reindex.getConfig("destination");

        destinationindex = destination.getString("index");
        destinationtype = destination.getString("type");

    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
