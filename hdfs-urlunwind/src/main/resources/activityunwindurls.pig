REGISTER /home/cloudera/Desktop/hdfs-urlunwind-0.1-SNAPSHOT.jar

DEFINE UNWINDER org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.urls.LinkUnwinderProcessor');

activities = LOAD 'twitter-activity/*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

unwound = FOREACH activities GENERATE activityid, source, timestamp, UNWINDER(object);

result = FILTER unwound BY $3 IS NOT NULL;

STORE result INTO 'twitter-unwound';