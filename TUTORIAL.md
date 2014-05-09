# Perform setup steps to prepare #

[Setup instructions](https://github.com/w2ogroup/streams-examples/tree/master/SETUP.md "Setup")

# Switch to streamstutorial branch #

From home directory:

    cd incubator-streams
    git fetch
    git checkout streamstutorial
    mvn clean install
    cd ../streams-examples
    git pull
    mvn clean package

# Start up elasticsearch

From home directory:

    cd elasticsearch-1.1.1
    bin/elasticsearch &
    
# Run the twitter history example #

[Twitter History README](https://github.com/w2ogroup/streams-examples/tree/master/twitter-history-elasticsearch "Twitter History")

# Run the userstream example #

[Userstream README](https://github.com/w2ogroup/streams-examples/tree/master/twitter-userstream-elasticsearch "User Stream")

# Run the gardenhose example #

[Gardenhose README](https://github.com/w2ogroup/streams-examples/tree/master/twitter-gardenhose-elasticsearch "Gardenhose")
