# Create a developer account: Set yourself up a developer account on Twitter #

You need to visit the official Twitter developer site and register for
a developer account. This is a free and necessary step to make
requests for the v1.1 API.

# Create an application: Create an application on the Twitter developer site #

What? You thought you could make unauthenticated requests? Not with
Twitter's v1.1 API. You need to visit http://dev.twitter.com/apps and
click the "Create Application" button.

On this page, fill in whatever details you want. For me, it didn't
matter, because I just wanted to make a load of block requests to get
rid of spam followers. The point is you are going to get yourself a
set of unique keys to use for your application.

So, the point of creating an application is to give yourself (and
Twitter) a set of keys. These are:

The consumer key
The consumer secret
The access token
The access token secret

There's a little bit of information here on what these tokens for.

# Create access tokens: You'll need these to make successful requests #

OAuth requests a few tokens. So you need to have them generated for you.

Click "create my access token" at the bottom. Then once you scroll to
the bottom again, you'll have some newly generated keys.

# Create a json snippet in a local text editor, using these keys #

    oauth {
            consumerKey = ""
            consumerSecret = ""
            accessToken = ""
            accessTokenSecret = ""
    }

You will be placing this snippet in your streams configuration files.

Also make a note of your numeric twitter ID like this:

    follow = [
        42232950
    ]

# Download the tutorial VM #

You can download the VM here: ...

# Start the tutorial VM in virtualbox #

Configure the VM for bridged networking on an active connection #

log in as root : streamstutorial

# Confirm that you have an ip address #

'ifconfig' and look for inet address on eth0

if you don't see one, 'ifup eth0' should fix this

# Confirm that you have a working internet connection #

curl -X GET https://api.twitter.com
should output a web page

# Open an ssh session to the virtual machine #

this will allow you to copy/paste into the terminal

# Prepare to check that your credentials are authorized #

cd streams-examples/twitter-userstream-console

nano src/main/resources/application.conf

provide your credentials under twitter.oauth, and your numeric userid via follow.

# Check that your credentials are authorized #

java -cp target/twitter-userstream-console-0.1-SNAPSHOT.jar org.apache.streams.twitter.example.TwitterUserstreamConsole

if everything is good to go, you will see debug statements as a connection is established, packets start moving, and then a list of all of the individuals you are following flash by.

