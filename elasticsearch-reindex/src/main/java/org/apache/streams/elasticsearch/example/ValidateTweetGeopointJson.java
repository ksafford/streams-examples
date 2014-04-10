package org.apache.streams.elasticsearch.example;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by rebanks on 4/9/14.
 */
public class ValidateTweetGeopointJson implements StreamsProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(ValidateTweetGeopointJson.class);

    @Override
    public List<StreamsDatum> process(StreamsDatum datum) {
        List<StreamsDatum> result = Lists.newLinkedList();
        datum.setDocument(validateGeoPointJson(datum.getDocument()));
        result.add(datum);
        return result;
    }

    @Override
    public void prepare(Object o) {

    }

    @Override
    public void cleanUp() {

    }

    private Object validateGeoPointJson(Object doc) {
        String twitterJsonString = doc.toString();
        try {
            JSONObject twitterObject = new JSONObject(twitterJsonString);
            JSONObject coordinates = twitterObject.optJSONObject("coordinates");
            if(coordinates != null) {
                JSONArray coords = coordinates.getJSONArray("coordinates");
                if(coords == null || coords.length() != 2) {
                    twitterObject.remove("coordinates");
                } else {
                    for(int i=0; i < coords.length(); ++i) {
                        try {
                            double coord = coords.getDouble(i);
                        } catch (Exception e) {
                            twitterObject.remove("coordinates");
                        }
                    }
                    if(!coordinates.getString("type").equals("Point")) {
                        coordinates.put("type", "Point");
                    }
                }
            }
            return twitterObject.toString();
        } catch (JSONException je) {
            LOGGER.error("Excpetion validating geopoint JSON data : {}", je);

        }
        LOGGER.error("Returning orignal doc because of parsing error.");
        return doc;
    }
}
