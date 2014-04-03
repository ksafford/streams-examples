twitter-userstream-local
==============================

Requirements:
-------------
 - An active twitter account

This example connects to an active twitter account and displays the userstream

Example Configuration:
----------------------

    include "reference"
    twitter {
        host = "api.twitter.com"
        endpoint = "userstream"
        oauth {
                consumerKey = "bcg14JThZEGoZ3MZOoT2HnJS7"
                consumerSecret = "S4dwxnZni58CIJaoupGnUrO4HRHmbBGOb28W6IqOJBx36LPw2z"
                accessToken = ""
                accessTokenSecret = ""
            }
        follow = [
                2189174101
        ]
    }

The consumerKey and consumerSecret are set for our streams-example application
The accessToken and accessTokenSecret can be obtained by navigating to:
 https://api.twitter.com/oauth/authenticate?oauth_token=UIJ0AUxCJatpKDUyFt0OTSEP4asZgqxRwUCT0AMSwc&oauth_callback=http%3A%2F%2Foauth.streamstutorial.w2odata.com%3A8080%2Fsocialauthdemo%2FsocialAuthSuccessAction.do

**NOTE:** At the moment this will require assistance from a member of the W2O team

Running:
--------

Once the configuration file has been completed this example can be run with:
`java -cp ~/git/streams-examples/twitter-userstream-local/target/twitter-userstream-standalone-0.1-SNAPSHOT.jar -Dconfig.file=application.conf org.apache.streams.twitter.example.TwitterUserstreamLocal`

Verification:
-------------
You should see this Twitter account's history in addition to any live tweets that come through displayed in your console