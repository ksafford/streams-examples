twitter-gardenhose-dualwriter
==============================

Requirements:
-------------
 - An running and configured instance of the Cloudera VM
 - A running ElasticSearch 1.0.0+ instance
 - An empty directory in HDFS to store activities

Example Configuration:
----------------------

    include "reference"
    twitter {
        host = "api.twitter.com"
        endpoint = "statuses/user_timeline"
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

n the Twitter section you should place all of your relevant authentication keys and whichever Twitter IDs you're looking to follow
Twitter IDs can be converted from screennames at http://www.gettwitterid.com

In the ElasticSearch section, you should put in all necessary configuration information. For each of these jars, you will need
to change the 'index' and 'type' attributes to whatever is appropriate. For example, if you were running the tweet jar the index attribute
would be 'userhistory_tweet' and the type attribute would be 'tweet'

Running:
--------

You will need to run `./install_templates.sh` in the resources folder in order to apply the templates to your ES cluster

Once the configuration file has been completed and the templates installed, this example can be run with:
`java -cp twitter-history-elasticsearch-0.1-SNAPSHOT.jar -Dconfig.file=userhistory.conf org.apache.streams.twitter.example.TwitterHistoryElasticsearch{Tweet|Retweet|Activity}`

Verification:
-------------
Open up http://localhost:9200/_plugin/head/ and confirm that all three indices now have data in them
Open up http://localhost:9200/_plugin/marvel and from the folder icon in the top right hand corner click
    Load -> Advanced -> Choose File and select either 'resources/reports/ActivityReport.json' or 'resources/reports/TweetReport.json'

Open up http://localhost:8888, log in as cloudera/cloudera, click to the HDFS browser tab, and confirm that data files are being written to the folder specified.

 
