package com.example.substandard.database.network.musicbrainz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicBrainzJSONUtils {
    private final static String RELATIONS_KEY = "relations";
    private final static String RESOURCE_TYPE_KEY = "type";
    private final static String IMAGE_VALUE = "image";
    private final static String URL_KEY = "url";
    private final static String URL_RESOURCE_KEY = "resource";

    public static String getImageUrl(JSONObject jsonObject) throws JSONException{
        JSONArray relationsArray = jsonObject.getJSONArray(RELATIONS_KEY);
        JSONObject imageObject = getImageResource(relationsArray);

        // null means no image resource returned from MB
        return null == imageObject ?
                null : imageObject.getJSONObject(URL_KEY).getString(URL_RESOURCE_KEY);
    }

    private static JSONObject getImageResource(JSONArray relations) throws JSONException {
        for (int i = 0; i < relations.length(); i++) {
            JSONObject resource = relations.getJSONObject(i);
            if (isImageResource(resource)) {
                return resource;
            }
        }

        return null;
    }

    private static boolean isImageResource(JSONObject resource) throws JSONException {
        String resourceType = resource.getString(RESOURCE_TYPE_KEY);
        return IMAGE_VALUE.equals(resourceType);
    }
}
