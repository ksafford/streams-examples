SET job.name 'biohistory';
SET default_parallel 1;

REGISTER streams.jar

DEFINE USEREXTRACTOR org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.json.JsonPathExtractor','\u0024.user.id_str');
DEFINE DATEEXTRACTOR org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.json.JsonPathExtractor','\u0024.created_at');
DEFINE BIOEXTRACTOR org.apache.streams.pig.StreamsProcessDocumentExec('org.apache.streams.json.JsonPathExtractor','\u0024.user');
DEFINE TWITTERTOMILLIS org.apache.pig.builtin.InvokeForLong('org.apache.streams.twitter.serializer.StreamsTwitterMapper.getMillis', 'String');
RMF target/timestampedbios
tweets = LOAD '$tribe/twitter/statuses-dedup-gz' USING PigStorage('\t') AS (id: chararray, source: chararray, timestamp: long, object: chararray);
allbios = FOREACH tweets GENERATE
    USEREXTRACTOR(object) AS user_str,
    DATEEXTRACTOR(object) AS date_str,
    BIOEXTRACTOR(object) AS bio_json,
    TWITTERTOMILLIS(DATEEXTRACTOR(object)) AS millis,
    source AS source;
userbios = FILTER allbios BY user_str IS NOT NULL;
datebios = FILTER userbios BY date_str IS NOT NULL;
biobios = FILTER datebios BY bio_json IS NOT NULL;
millibios = FILTER datebios BY millis IS NOT NULL;
streamsbios = FOREACH millibios GENERATE
    CONCAT(user_str,CONCAT('-',(chararray)millis)) AS id,
    source AS source,
    millis AS timestamp,
    bio_json AS object;
STORE streamsbios INTO '$tribe/twitter/biohistory' USING PigStorage('\t');

