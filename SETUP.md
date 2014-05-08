# Create a twitter account if you don't have one #

Do you tweet?  And know your twitter password?  If so skip this step.

# Find out your twitter numeric ID #

You can find this out at http://mytwitterid.com/

# Pick an account to analyze #

If you haven't tweeted very often, or don't follow many people, it's OK!

Make a short list of accounts you think might be interesting, and go find their numeric IDs

Collect this list into a snippet that looks like this:

    follow = [
        42232950
    ]

You can have multiple accounts in the array - delimited by new-lines, no commas necessary.

# Create a developer account: Set yourself up a developer account on Twitter #

You need to visit the official Twitter developer site and register for
a developer account. This is a free and necessary step to make
requests for the v1.1 API.

https://dev.twitter.com/

# Create an application: Create an application on the Twitter developer site #

Click the "Create Application" button.

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


Note: bookmark this page, you'll use it again soon.

# Download the tutorial VM #

You can download the VM here: https://dl.dropboxusercontent.com/u/9422012/Apache%20Streams%20Tutorial.zip

# Start the tutorial VM in virtualbox #

Configure the VM for bridged networking on an active connection

log in as root : streamstutorial

# Confirm that you have an ip address #

'ifconfig' and look for inet address on eth0

if you don't see one, 'ifup eth0' should fix this

# Confirm that you have a working internet connection #

curl -X GET https://api.twitter.com
should output a web page

# Open an ssh session to the virtual machine #

this will allow you to copy/paste into the terminal

# Check that your credentials are authorized #

Reopen the page where you created tokens.

Set Request URI: to the suggested value
https://api.twitter.com/1.1/statuses/home_timeline.json

Click 'See OAuth signature for this request'

Copy the cURL command returned into your VM shell and submit

If all is well, you will get a response from twitter containing tweets.
