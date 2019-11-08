package com.example.substandard.database.network;

import android.util.Log;

import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for parsing JSON objects received as responses to Subsonic queries.
 */
class SubsonicJsonParseUtils {
    private static final String TAG = SubsonicNetworkUtils.class.getSimpleName();

    /*
     ********************************************************************************
     * General use parsing methods which apply to all requests, regardless of service.
     ********************************************************************************
     */

    // here's a test url for grabbing json
    // rest/getArtists?u=dev&t=656f279a8e9b273f9b3deaadb9f5f864&s=mkfs12&v=1.14.0&c=substandard&f=json

    /*
     * Various constants for names of fields in subsonic-response JSON objects
     */
    private static final String SUBSONIC_RESPONSE_KEY = "subsonic-response";
    private static final String RESPONSE_STATUS_KEY = "status";
    private static final String RESPONSE_SUCCESS = "ok";
    private static final String RESPONSE_FAILED = "failed";
    private static final String RESPONSE_ERROR_KEY = "error";
    private static final String RESPONSE_ERROR_MESSAGE_KEY = "message";

    /**
     * When the request fails, pass the JSON response here to retrieve the error message
     * @param json JSON object returned from service,
     * @return The error message from the server request. Returns null if the request didn't actually
     * fail.
     * @throws JSONException if JSON is malformed
     */
    private static String parseErrorCode(JSONObject json) throws JSONException {
        JSONObject responseObject = json.getJSONObject(SUBSONIC_RESPONSE_KEY);
        // This should never be true. If it is, you fucked up.
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
    static boolean requestSuccessful(JSONObject json) throws JSONException {
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
     ************************************************************************************
     * Parsing methods to handle request for the getArtists service.
     ************************************************************************************
     */

    /*
     *  An example response is the following:
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

    /*
     * Constants needed to access fields in JSON meeting this format
     */

    private static final String ARTISTS_KEY = "artists";
    private static final String ARTISTS_INDEX_ARRAY_KEY = "index";

    private static final String ARTIST_ARRAY_KEY = "artist";
    private static final String ARTIST_ID_KEY = "id";
    private static final String ARTIST_NAME_KEY = "name";
    private static final String ARTIST_ART_KEY = "coverArt";
    private static final String ARTIST_IMAGE_URL_KEY = "artistImageUrl";
    private static final String ARTIST_ALBUM_COUNT = "albumCount";

    /**
     * Parses a JSON object returned from a call to the getArtists service.
     * @param json The JSON object returned from the service
     * @return A list of artist name strings. Will return null if getArtists request failed or if
     * the JSON object received did not come from a getArtists call.
     * @throws JSONException If JSON is malformed
     */
    public static List<Artist> parseGetArtists(JSONObject json) throws JSONException {
        Log.d(TAG, "parsing JSON from getArtists request");
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
                // These two fields throw JSON exceptions??? Remove them I guess.
//                String coverArt = "";
//                if (artistObject.has(ARTIST_ART_KEY)) {
//                    coverArt = artistObject.getString(ARTIST_ART_KEY);
//                }
//                String imageUrl = "";
//                if (artistObject.has(ARTIST_IMAGE_URL_KEY)) {
//                    imageUrl = artistObject.getString(ARTIST_IMAGE_URL_KEY);
//                }
                int albumCount = artistObject.getInt(ARTIST_ALBUM_COUNT);

                artistList.add(new Artist(id, name, albumCount));
            }
        }

        return artistList;
    }

    /*
     ************************************************************************************
     * Parsing methods to handle request for the getArtist service.
     ************************************************************************************
     */

    /*
     *  An example response is the following:
        {
           "subsonic-response" : {
              "status" : "ok",
              "version" : "1.16.1",
              "artists" : {
                 "ignoredArticles" : "The El La Los Las Le Les",
                 "index" : [ {
                    "name" : "A",
                    "artist" : [ {
                       "id" : "306",
                       "name" : "A$AP Rocky",
                       "coverArt" : "ar-306",
                       "artistImageUrl" : "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                       "albumCount" : 2
                    }, {
                       "id" : "123",
                       "name" : "Adventures",
                       "coverArt" : "ar-123",
                       "artistImageUrl" : "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                       "albumCount" : 1
                    }, {
                       "id" : "447",
                       "name" : "Against Me!",
                       "coverArt" : "ar-447",
                       "artistImageUrl" : "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                       "albumCount" : 3
                    }, {
                       "id" : "528",
                       "name" : "Agent Orange",
                       "coverArt" : "ar-528",
                       "artistImageUrl" : "https://lastfm-img2.akamaized.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                       "albumCount" : 1
                    } ]
                 } ]
              }
           }
        }
     *
     */

    /*
     * Field names for JSON object
     */
    private static final String ARTIST_KEY = "artist";
    private static final String ALBUM_ARRAY_KEY = "album";
    private static final String ALBUM_ID_KEY = "id";
    private static final String ALBUM_NAME_KEY = "name";
    private static final String ALBUM_COVER_ART_KEY = "coverArt";
    private static final String ALBUM_SONG_COUNT_KEY = "songCount";
    private static final String ALBUM_DURATION_KEY = "duration";
    private static final String ALBUM_ARTIST_ID_KEY = "artistId";
    private static final String ALBUM_CREATED_DATE_KEY = "created";

    /**
     * Parses a request to the GetArtist service
     * @param json Response object from the server
     * @return List of albums belonging to chosen artist
     * @throws JSONException malformed json object
     */
    static List<Album> parseGetArtist(JSONObject json) throws JSONException {
        Log.d(TAG, "parsing JSON from getArtists request");
        List<Album> albumList = new ArrayList<>();
        if (!requestSuccessful(json)) {
            return null;
        }

        JSONObject responseObject = json.getJSONObject(SUBSONIC_RESPONSE_KEY);
        if (!responseObject.has(ARTIST_KEY)) {
            Log.d(TAG, "parse: incorrect JSON format. Did this come from getArtist?");
            return null;
        }

        JSONObject artistJson = responseObject.getJSONObject(ARTIST_KEY);
        JSONArray albumsArray = artistJson.getJSONArray(ALBUM_ARRAY_KEY);

        for (int i = 0; i < albumsArray.length(); i++) {
            JSONObject albumAtIndex = albumsArray.getJSONObject(i);
            albumList.add(parseAlbumObject(albumAtIndex));
        }

        return albumList;
    }

    /**
     * Parses an Album JSONObject from Subsonic server
     * @param albumObject an artist JSON object, containing top level fields id, artists, etc
     * @return Album object containing info in JSON object. Some fields may be null
     * @throws JSONException malformed JSON
     */
    private static Album parseAlbumObject(JSONObject albumObject) throws JSONException{
        Log.d(TAG, "parsing album JSON object");

        int albumId = albumObject.getInt(ALBUM_ID_KEY);

        String albumName = "";
        if (albumObject.has(ALBUM_NAME_KEY)) {
            albumName = albumObject.getString(ALBUM_NAME_KEY);
        }

        int duration = -1;
        if (albumObject.has(ALBUM_DURATION_KEY)) {
            duration = albumObject.getInt(ALBUM_DURATION_KEY);
        }

        int artistId = -1;
        if (albumObject.has(ALBUM_ARTIST_ID_KEY)) {
            artistId = albumObject.getInt(ALBUM_ARTIST_ID_KEY);
        }

        int numTracks = -1;
        if (albumObject.has(ALBUM_SONG_COUNT_KEY)) {
            numTracks = albumObject.getInt(ALBUM_SONG_COUNT_KEY);
        }

        Date dateCreated = null;
        if (albumObject.has(ALBUM_CREATED_DATE_KEY)) {
            dateCreated = parseDateString(albumObject.getString(ALBUM_CREATED_DATE_KEY));
        }

        return new Album(albumId, albumName, numTracks, dateCreated, duration, artistId);
    }

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /**
     * Turns weird date Strings passed by server into nice Date objects
     * @param dateString in the format yyy-MM-dd'T'hh:mm:ss.SSS'Z', e.g. 2004-11-27T20:23:22.000Z
     * @return nice parsed Date object, or null if malformed date string
     */
    private static Date parseDateString(String dateString) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat(DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return parsedDate;
    }


    /*
     ************************************************************************************
     * Parsing methods to handle request for the getAlbumList2 service.
     ************************************************************************************
     */

    private static final String ALBUM_LIST_KEY = "albumList2";

    /**
     * Parses JSON response from getAlbumList2 service
     * @param json JSON response from the server
     * @return List of albums returned by the service (I'm not entirely sure how this service works)
     * @throws JSONException JSON is in wrong form
     */
    static List<Album> parseGetAlbumList(JSONObject json) throws JSONException {
        Log.d(TAG, "parsing JSON from getArtists request");
        List<Album> albumList = new ArrayList<>();
        if (!requestSuccessful(json)) {
            return null;
        }

        JSONObject responseObject = json.getJSONObject(SUBSONIC_RESPONSE_KEY);
        if (!responseObject.has(ALBUM_LIST_KEY)) {
            Log.d(TAG, "parse: incorrect JSON format. Did this come from getAlbumList2?");
            return null;
        }

        JSONArray albumsArray = responseObject.getJSONArray(ALBUM_LIST_KEY);

        for (int i = 0; i < albumsArray.length(); i++) {
            JSONObject albumAtIndex = albumsArray.getJSONObject(i);
            albumList.add(parseAlbumObject(albumAtIndex));
        }

        return albumList;
    }

}
