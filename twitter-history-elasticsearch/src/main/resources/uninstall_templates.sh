#! /bin/bash
curl -X DELETE 'localhost:9200/_template/base' -d @templates/base.json
curl -X DELETE 'localhost:9200/_template/char_filters' -d @templates/char_filters.json
curl -X DELETE 'localhost:9200/_template/filter' -d @templates/filters.json
curl -X DELETE 'localhost:9200/_template/stop_filter_english' -d @templates/stop_filter_english.json
curl -X DELETE 'localhost:9200/_template/streams_keyword_analyzer' -d @templates/streams_keyword_analyzer.json
curl -X DELETE 'localhost:9200/_template/streams_lowercase_analyzer' -d @templates/streams_lowercase_analyzer.json
curl -X DELETE 'localhost:9200/_template/streams_standard_analyzer' -d @templates/streams_standard_analyzer.json
curl -X DELETE 'localhost:9200/_template/streams_tag_analyzer' -d @templates/streams_tag_analyzer.json
curl -X DELETE 'localhost:9200/_template/tweet' -d @templates/tweet.json
curl -X DELETE 'localhost:9200/_template/activity' -d @templates/activity.json
curl -X DELETE 'localhost:9200/_template/twitteractivity' -d @templates/twitteractivity.json
