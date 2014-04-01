REGISTER /home/cloudera/Desktop/hdfs-converttoactivity-0.1-SNAPSHOT.jar

DEFINE SERIALIZER org.apache.streams.pig.StreamsSerializerExec('org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer');

articles = LOAD 'MoreoverProvider/*' USING PigStorage('\t') AS (moreoverid: chararray, source: chararray, timestamp: chararray, object: chararray);

activities = FOREACH articles GENERATE
    moreoverid AS moreoverid,
    source AS source,
    timestamp AS timestamp,
    SERIALIZER(object) AS activity;

result = FILTER activities BY activity IS NOT NULL;

STORE result INTO 'moreover-activity';
