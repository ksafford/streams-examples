twitter-gardenhose-elasticsearch
==============================

Requirements:
-------------
 - A running instance of ElasticSearch

Configuration:
--------------
    include "reference"
    twitter {
        endpoint = "sample"
        oauth {
            consumerKey = ""
            consumerSecret = ""
            accessToken = ""
            accessTokenSecret = ""
        }
        track = [
            apache
            hadoop
            pig
            hive
            cassandra
            elasticsearch
            mongo
            data
            apache storm
            apache streams
            big data
            asf
            opensource
            open source
            apachecon
            apache con
        ]
    }
    elasticsearch {
        hosts = [
            localhost
        ]
        port = 9300
        clusterName = elasticsearch
        index = gardenhose_activity
        type = activity
    }

You will need to change the Twitter keys to reflect the contents your personal token

Running:
--------

From within the virtual machine run:
`java -cp target/twitter-gardenhose-elasticsearch-0.1-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.conf org.apache.streams.twitter.example.TwitterGardenhoseElasticsearch`

Verification:
-------------
**NOTE:** It may take some time for enough tweets to come through before the buffers get flushed to ElasticSearch

Once this example has run for long enough, you should see the index you specified filling with data.

