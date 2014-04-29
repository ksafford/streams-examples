twitter-userstream-local
==============================

Requirements:
-------------
 - An active datasift account
 - At least one active datasift stream

This example connects to an active datasift stream and displays messages received in real-time

Example Configuration:
----------------------

    include "reference"
    datasift {
        apiKey = ""
        userName = ""
        hashes = [
            03ab029f120f989bf75b0b9b8f118467   
        ]
    }

Example CSDL:
-------------

interaction.title contains_any "Apache"

Running:
--------

Create your stream from the Datasift console, making a note of the stream hash.

Once the configuration file has been completed this example can be run with:
`java -cp target/datasift-streaming-console-0.1-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.conf org.apache.streams.twitter.example.TwitterUserstreamLocal`

Verification:
-------------
You should see message matching the CSDL of any hash in your config file presented immediately in the console.