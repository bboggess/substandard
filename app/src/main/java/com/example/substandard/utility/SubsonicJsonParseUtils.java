package com.example.substandard.utility;

import android.util.Log;

import com.example.substandard.database.Artist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing JSON objects received as responses to Subsonic queries.
 */
public class SubsonicJsonParseUtils {
    private static final String TAG = SubsonicNetworkUtils.class.getSimpleName();

    /**
     * Various constants for names of fields in JSON objects received from server
     */
    private static final String SUBSONIC_RESPONSE_KEY = "subsonic-response";
    private static final String RESPONSE_STATUS_KEY = "status";
    private static final String RESPONSE_SUCCESS = "ok";
    private static final String RESPONSE_FAILED = "failed";
    private static final String RESPONSE_ERROR_KEY = "error";
    private static final String RESPONSE_ERROR_MESSAGE_KEY = "message";

    private static final String ARTISTS_KEY = "artists";
    private static final String ARTISTS_INDEX_ARRAY_KEY = "index";

    private static final String ARTIST_ARRAY_KEY = "artist";
    private static final String ARTIST_ID_KEY = "id";
    private static final String ARTIST_NAME_KEY = "name";
    private static final String ARTIST_ART_KEY = "coverArt";
    private static final String ARTIST_IMAGE_URL_KEY = "artistImageUrl";
    private static final String ARTIST_ALBUM_COUNT = "albumCount";

    /**
     * General use parsing methods which apply to all requests, regardless of service.
     */

    /**
     * When the request fails, pass the JSON response here to retrieve the error message
     * @param json JSON object returned from service,
     * @return The error message from the server request. Returns null if the request didn't actually
     * fail.
     * @throws JSONException if JSON is malformed
     */
    public static String parseErrorCode(JSONObject json) throws JSONException {
        JSONObject responseObject = json.getJSONObject(SUBSONIC_RESPONSE_KEY);
        if (responseObject.getString(RESPONSE_STATUS_KEY).equals(RESPONSE_SUCCESS)) {
            Log.d(TAG, "parseErrorCode: the request didn't fail");
            return null;
        }
        JSONObject errorObject = responseObject.getJSONObject(RESPONSE_ERROR_KEY);
        return errorObject.getString(RESPONSE_ERROR_MESSAGE_KEY);
    }

    /**
     * Check whether the Subsonic request was successfully handled. If not, the error message
     * is logged
     * @param json JSON object returned from Subsonic service.
     * @return true if request was successfully handled
     * @throws JSONException The input JSON is malformed
     */
    // TODO create SubsonicConnectionException (or something) and throw if request fails
    public static boolean requestSuccessful(JSONObject json) throws JSONException {
        if (!json.has(SUBSONIC_RESPONSE_KEY)) {
            Log.d(TAG, "requestSuccessful: Not even a valid subsonic response. Wtf are you doing?");
            return false;
        }

        JSONObject responseObject = json.getJSONObject(SUBSONIC_RESPONSE_KEY);
        if (responseObject.getString(RESPONSE_STATUS_KEY).equals(RESPONSE_FAILED)) {
            Log.d(TAG, "requestSuccessful: Subsonic request failed. Message: " + parseErrorCode(json));
            return false;
        }

        return true;
    }

    /*
     * Parsing methods to handle request for the getArtists service. An example response is the following:
     *
     *  * {
     *  *     "subsonic-response":
     *  *     {
     *  *         "status": "ok",
     *  *         "version": "1.16.1",
     *  *         "artists": {
     *  *             "ignoredArticles": "The El La Los Las Le Les",
     *  *             "index":[
     *  *                 {
     *  *                     "name": "A",
     *  *                     "artist": [
     *  *                         {
     *  *                             "id": "447",
     *  *                             "name": "Against Me!",
     *  *                             "coverArt": "ar-447",
     *  *                             "artistImageUrl": "someurl.png",
     *  *                             "albumCount": 3
     *  *                         },
     *  *                         {
     *  *                             "id": "528",
     *  *                             "name": "Agent Orange,
     *  *                             "coverArt": "ar-528",
     *  *                             "artistImageUrl": "someurl.png,
     *  *                             "albumCount": 1
     *  *                         }
     *  *                     ]
     *  *                 }
     *  *             ]
     *  *         }
     *  *     }
     *  * }
     */

    /**
     * Parses a JSON object returned from a call to the getArtists service.
     * @param json The JSON object returned from the service
     * @return A list of artist name strings. Will return null if getArtists request failed or if
     * the JSON object received did not come from a getArtists call.
     * @throws JSONException If JSON is malformed
     */
    public static List<Artist> parseGetArtists(JSONObject json) throws JSONException {
        List<Artist> artistList = new ArrayList<>();
        if (!requestSuccessful(json)) {
            return null;
        }

        JSONObject responseObject = json.getJSONObject(SUBSONIC_RESPONSE_KEY);
        if (!responseObject.has(ARTISTS_KEY)) {
            Log.d(TAG, "parse: incorrect JSON format. Did this come from getArtists?");
            return null;
        }

        JSONObject artistsJson = responseObject.getJSONObject(ARTISTS_KEY);
        JSONArray artistsArrayIndexedByAbc = artistsJson.getJSONArray(ARTISTS_INDEX_ARRAY_KEY);

        for (int i = 0; i < artistsArrayIndexedByAbc.length(); i++) {
            JSONArray artistsArrayAtIndex = artistsArrayIndexedByAbc.getJSONObject(i).getJSONArray(ARTIST_ARRAY_KEY);
            for (int j = 0; j < artistsArrayAtIndex.length(); j++) {
                JSONObject artistObject = artistsArrayAtIndex.getJSONObject(j);
                int id = artistObject.getInt(ARTIST_ID_KEY);
                String name = artistObject.getString(ARTIST_NAME_KEY);
                String coverArt = artistObject.getString(ARTIST_ART_KEY);
                String imageUrl = artistObject.getString(ARTIST_IMAGE_URL_KEY);
                int albumCount = artistObject.getInt(ARTIST_ALBUM_COUNT);

                artistList.add(new Artist(id, name, coverArt, imageUrl, albumCount));
            }
        }

        return artistList;
    }
}
