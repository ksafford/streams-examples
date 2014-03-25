REGISTER /home/cloudera/Desktop/hdfs-converttoactivity-0.1-SNAPSHOT.jar

DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonTweetActivitySerializer');

tweet = LOAD 'testtweets/*' USING PigStorage('\t') AS (tweetid: chararray, source: chararray, timestamp: chararray, object: chararray);

activity = FOREACH tweet GENERATE tweetid, source, timestamp, SERIALIZER(object);



STORE activity INTO 'activity';

DUMP activity;

ILLUSTRATE activity;