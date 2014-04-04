twitter-gardenhose-dualwriter
==============================

Requirements:
-------------
 - A running and configured instance of the Cloudera VM
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
    hdfs {
        host = "localhost"
        port = "50070"
        writerPath = "/streaming/twitter/example"
        path = "/user/cloudera"
        user = "cloudera"
        password = "cloudera"
        writerFilePrefix = "streams-"
    }

You will need to change the Twitter keys to reflect the contents your personal token

Running:
--------

From within the virtual machine run:
`java -cp target/twitter-gardenhose-dualwriter-0.1-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.conf org.apache.streams.twitter.example.TwitterGardenhoseDualWriter`

Verification:
-------------
**NOTE:** It may take some time for enough tweets to come through before the buffers get flushed to ElasticSearch

Once this example has run for long enough, you should see the index you specified filled with data and that same data
should be mirrored on your HDFS instance.

