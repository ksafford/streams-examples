REGISTER /home/cloudera/Desktop/hdfs-pullarticles-0.1-SNAPSHOT.jar

DEFINE TIKAPULL org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.tika.TikaProcessor');

activities = LOAD 'twitter-unwound/*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

articles = FOREACH activities GENERATE activityid, source, timestamp, TIKAPULL(object);

result = FILTER articles BY $3 IS NOT NULL;

STORE result INTO 'twitter-articles';