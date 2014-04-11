REGISTER /home/cloudera/Desktop/hdfs-urlunwind-0.1-SNAPSHOT.jar

DEFINE JsonPathExtract InvokeForString('com.jayway.jsonpath.JsonPath', 'String String');

activities = LOAD 'w2o/twitter/activities/*' USING PigStorage('\t') AS (activityid: chararray, source: chararray, timestamp: long, object: chararray);

langs = FOREACH activities GENERATE activityid, source, timestamp, JsonPathExtract.read(object, '$.lang');

result = FILTER langs BY $3 IS NOT NULL;

STORE result INTO 'w2o/twitter/langs';