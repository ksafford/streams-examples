REGISTER /home/cloudera/Desktop/hdfs-converttoactivity-0.1-SNAPSHOT.jar

DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer');

tweets = LOAD 'w2o/twitter/statuses/*' USING PigStorage('\t') AS (tweetid: chararray, source: chararray, timestamp: chararray, object: chararray);

activities = FOREACH tweets GENERATE
    tweetid AS tweetid,
    source AS source,
    timestamp AS timestamp,
    SERIALIZER(object) AS activity;

result = FILTER activities BY activity IS NOT NULL;

STORE result INTO 'w2o/twitter/activities';
