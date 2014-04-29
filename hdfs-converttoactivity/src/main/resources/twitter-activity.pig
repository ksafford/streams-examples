REGISTER /home/cloudera/Desktop/hdfs-converttoactivity-0.1-SNAPSHOT.jar

DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer');

DEFINE KEYWORDS org.apache.streams.pig.StreamsProcessorDocumentExec('org.apache.streams.json.JsonPathCopier', '\u0024.content', '\u0024.extensions.keywords');

tweets = LOAD 'w2o/twitter/statuses/*' USING PigStorage('\t') AS (id: chararray, source: chararray, timestamp: chararray, object: chararray);

activities = FOREACH tweets GENERATE
    tweetid AS id,
    source AS source,
    timestamp AS timestamp,
    SERIALIZER(object) AS object;

keywords = FOREACH activities GENERATE
               id AS id,
               source AS source,
               timestamp AS timestamp,
               KEYWORDS(object) AS object;

result = FILTER keywords BY object IS NOT NULL;

STORE result INTO 'w2o/twitter/activities';
