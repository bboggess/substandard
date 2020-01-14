package com.example.substandard.database.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.substandard.data.SubstandardPreferences;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.Song;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * Provides static methods for interacting with the Subsonic server over the network. This class
 * only knows about things related to sending network requests to the server; it can not interpret
 * the info.
 */
/* TODO finish writing request methods. Need to work out some more implementation details first.
 */
public class SubsonicNetworkUtils {
    /*
     * Tag for debugging purposes.
     */
    private static final String TAG = SubsonicNetworkUtils.class.getSimpleName();

    /*
     * This is the version of the REST API that this app will use. This is value was chosen to be
     * backwards compatible with all version 6.*.* of Subsonic. Not compatible with any earlier versions.
     */
    private final static String SUBSONIC_PROTOCOL_VERSION = "1.14.0";

    private static final String SUBSONIC_REST_PATH = "rest";

    /*
     * I prefer to have the results returned in json rather than xml. I don't know why I prefer
     * this. Maybe I had a reason.
     */
    private final static String REQUEST_RETURN_FORMAT = "json";

    private final static String APP_NAME = "substandard";

    /*
     * Query parameters for building URL requests to server.
     *
     * All except for FORMAT_QUERY are required in all requests.
     * USERNAME_QUERY: Username sending the request
     * PASSWORD_QUERY: Technically not required if SALT_QUERY and AUTH_TOKEN_QUERY are both used.
     *                 Only to be used for testing purposes.
     * AUTH_TOKEN_QUERY: An authentication token = md5(password + salt)
     * SALT_QUERY: A random string used to compute the password hash. Must be at least 6 characters.
     * VERSION_QUERY: The Subsonic protocol used by the client. This is stored in SUBSONIC_PROTOCOL_VERSION
     * CLIENT_QUERY: A string uniquely identifying this app. Will be stored elsewhere.
     * FORMAT_QUERY: (Optional) The requested format of response. Supported values are "xml", "json", and
     *               "jsonp". The default value is "xml".
     */
    private final static String USERNAME_QUERY = "u";
    private final static String PASSWORD_QUERY = "p";
    private final static String AUTH_TOKEN_QUERY = "t";
    private final static String SALT_QUERY = "s";
    private final static String VERSION_QUERY = "v";
    private final static String CLIENT_QUERY = "c";
    private final static String FORMAT_QUERY = "f";

    /*
     * Optional parameters needed for various services.
     */
    // used in many services
    private final static String ID_QUERY = "id";
    // Optional in getIndexes, getArtists, getAlbumList, getRandomSongs, getSongsByGenre,
    // getStarred, search
    private final static String MUSIC_FOLDER_ID_QUERY = "musicFolderId";
    // Used in createPlaylist
    private final static String SONG_ID_QUERY = "songId";
    // Used if starring an artist in star and unstar
    private final static String ARTIST_ID_QUERY = "artistId";
    // Used if starring an album in star and unstar
    private final static String ALBUM_ID_QUERY = "albumId";
    // Optional in getArtistInfo, getSimilarSongs, getTopSongs, search, scrobble,
    private final static String MAX_COUNT_QUERY = "count";
    // Optional in getArtistInfo
    private final static String INCLUDE_NON_LIBRARY_QUERY = "includeNotPresent";
    // Optional in getAlbumList, getRandomSongs,
    private final static String NUM_TO_RETURN_QUERY = "size";
    // Optional in getCoverArt
    private final static String IMAGE_SIZE_QUERY = "size";
    // Optional in getAlbumList, getSongsByGenre, search
    private final static String OFFSET_QUERY = "offset";
    // Required if searching getAlbumList by genre, getSongsByGenre
    // Optional in getRandomSongs
    private final static String GENRE_QUERY = "genre";
    // Required in updatePlaylist and createPlaylist (if updating)
    private final static String PLAYLIST_ID_QUERY = "playlistId";
    // Required in createPlaylist if new playlist
    private final static String PLAYLIST_NAME_QUERY = "name";
    // Seems to be needed for getLyrics, getTopSongs
    // Optional in search
    private final static String ARTIST_QUERY = "artist";
    // Optional in search
    private final static String ALBUM_QUERY = "album";
    // Required for getLyrics
    // Optional in search
    private final static String SONG_QUERY = "title";
    // Required in search2, search3
    private final static String SEARCH_QUERY = "query";
    // Optional in stream
    private final static String MAX_BITRATE_QUERY = "maxBitRate";
    // Required in setRating
    private final static String RATING_QUERY = "rating";
    // Optional in scrobble
    private final static String SCROBBLE_TIME_QUERY = "time";
    // Optional in scrobble
    private final static String SCROBBLE_SUBMISSION_QUERY = "submission";
    // Used in search2 and search3, if searching for albums/artists/etc
    private final static String MAX_ALBUM_COUNT = "albumCount";
    private final static String ALBUM_OFFSET = "albumOffset";
    private final static String MAX_ARTIST_COUNT = "artistCount";
    private final static String ARTIST_OFFSET = "artistOffset";
    private final static String MAX_SONG_COUNT = "songCount";
    private final static String SONG_OFFSET = "songOffset";

    /*
     * Strings for choosing a service to call from the server. Used only in building URL.
     */
    private enum SubsonicService {
        PING ("ping.view"),
        GET_ARTISTS ("getArtists"),
        GET_ARTIST ("getArtist"),
        GET_GENRES ("getGenres"),
        GET_ALBUM ("getAlbum"),
        GET_SONG ("getSong"),
        GET_ARTIST_INFO ("getArtistInfo2"),
        GET_ALBUM_INFO ("getAlbumInfo"),
        GET_SIMILAR_SONGS ("getSimilarSongs"),
        GET_TOP_SONGS ("getTopSongs"),
        GET_NOW_PLAYING ("getNowPlaying"),
        GET_ALBUM_LIST ("getAlbumList2"),
        GET_STARRED ("getStarred"),
        SEARCH ("search3"),
        GET_PLAYLISTS ("getPlaylists"),
        GET_PLAYLIST ("getPlaylist"),
        CREATE_PLAYLIST ("createPlaylist"),
        UPDATE_PLAYLIST ("updatePlaylist"),
        DELETE_PLAYLIST ("deletePlaylist"),
        STREAM ("stream"),
        DOWNLOAD ("download"),
        GET_COVER_ART ("getCoverArt"),
        GET_LYRICS ("getLyrics"),
        STAR ("star"),
        UNSTAR ("unstar"),
        SCROBBLE ("scrobble"),
        GET_PLAY_QUEUE ("getPlayQueue"),
        SAVE_PLAY_QUEUE ("savePlayQueue"),
        GET_SCAN_STATUS ("getScanStatus"),
        START_SCAN ("startScan"),
        GET_MUSIC_FOLDERS ("getMusicFolders"),
        GET_INDEXES ("getIndexes"),
        GET_MUSIC_DIRECTORY ("getMusicDirectory");

        private final String text;
        SubsonicService(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Encapsulates all info needed to send requests on behalf of a Subsonic user.
     */
    // TODO some sort of sanitation on serverAddress
    // Perhaps useful to have Exception thrown if created user is unable to be authenticated?
    public static class SubsonicUser {
        private String serverAddress;
        private String username;
        private String authToken;
        private String salt;

        public SubsonicUser(String serverAddress, String username, String password) {
            this.serverAddress = serverAddress;
            this.username = username;
            this.salt = SubsonicNetworkUtils.createSalt();
            this.authToken = SubsonicNetworkUtils.createAuthToken(password, salt);
        }

        SubsonicUser(String serverAddress, String username, String authToken, String salt) {
            this.serverAddress = serverAddress;
            this.username = username;
            this.authToken = authToken;
            this.salt = salt;
        }

        public String getServerAddress() {
            return serverAddress;
        }

        void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        public String getUsername() {
            return username;
        }

        void setUsername(String username) {
            this.username = username;
        }

        public String getAuthToken() {
            return authToken;
        }

        void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public String getSalt() {
            return salt;
        }

        void setSalt(String salt) {
            this.salt = salt;
        }
    }

    /**
     * Encapsulates info needed to send a request to the Subsonic server. All requests should
     * go through a SubsonicServerRequest.
     */
    public static class SubsonicServerRequest {
        private SubsonicUser user;
        private SubsonicService service;
        /**
         * Every request needs a user and a service, but some need more information. This should
         * be put in additionalParameters.
         */
        private Map<String, String> additionalParameters;

        SubsonicServerRequest(SubsonicUser user, SubsonicService service) {
            this.user = user;
            this.service = service;
            this.additionalParameters = Collections.EMPTY_MAP;
        }

        SubsonicServerRequest(SubsonicUser user, SubsonicService service, Map<String, String> additionalParameters) {
            this.user = user;
            this.service = service;
            this.additionalParameters = additionalParameters;
        }

        SubsonicUser getUser() {
            return user;
        }

        public void setUser(SubsonicUser user) {
            this.user = user;
        }

        SubsonicService getService() {
            return service;
        }

        public void setService(SubsonicService service) {
            this.service = service;
        }

        Map<String, String> getAdditionalParameters() {
            return additionalParameters;
        }

        public void setAdditionalParameters(Map<String, String> additionalParameters) {
            this.additionalParameters = additionalParameters;
        }
    }

    public static SubsonicUser getSubsonicUserFromPreferences(Context context) {
        String username = SubstandardPreferences.getPreferredUsername(context);
        String server =  SubstandardPreferences.getPreferredServerAddress(context);
        String authToken = SubstandardPreferences.getPreferredAuthToken(context);
        String salt = SubstandardPreferences.getPreferredSalt(context);
        return new SubsonicUser(server, username, authToken, salt);
    }

    /**
     * Returns the md5 hash of password and salt. Should be used to create the authorization
     * token.
     *
     * @param password The user's Subsonic password
     * @param salt A randomly generated String with which to hash
     * @return The MD5 hash of the concatenated string password + hash
     */
    public static String createAuthToken(String password, String salt) {
        // At one point I did this by hand, but the Apache library is better
        // Weird that calling DigestUtils.md5Hex directly throws exception...
        return new String(Hex.encodeHex(DigestUtils.md5(password + salt)));
    }

    /**
     * Returns a random String of 6 characters to be used as salt in the hash.
     */
    public static String createSalt() {
        // This is a quick way to get a random string
        return UUID.randomUUID().toString().substring(0,6);
    }

    /**
     * Builds a URL formatted for request to Subsonic server.
     * @param request The request we are building a URL to send
     * @return A fully formed URL that can be authenticated by the server
     */
    private static URL buildUrl(SubsonicServerRequest request) {
        SubsonicUser requestUser = request.getUser();
        Uri.Builder uriBuilder = Uri.parse(requestUser.getServerAddress()).buildUpon()
                .appendPath(SUBSONIC_REST_PATH)
                .appendPath(request.getService().getText())
                .appendQueryParameter(USERNAME_QUERY, requestUser.getUsername())
                .appendQueryParameter(AUTH_TOKEN_QUERY, requestUser.getAuthToken())
                .appendQueryParameter(SALT_QUERY, requestUser.getSalt())
                .appendQueryParameter(CLIENT_QUERY, APP_NAME)
                .appendQueryParameter(VERSION_QUERY, SUBSONIC_PROTOCOL_VERSION)
                .appendQueryParameter(FORMAT_QUERY, REQUEST_RETURN_FORMAT);

        Map<String, String> additionalParams = request.getAdditionalParameters();
        for (String key : additionalParams.keySet()) {
            uriBuilder.appendQueryParameter(key, additionalParams.get(key));
        }

        URL builtUrl = null;
        try {
            builtUrl = new URL(uriBuilder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "buildUrl: Bad URL " + uriBuilder.build().toString());
        }
        return builtUrl;
    }

    /**
     * Helper method to fetch JSON from request to Subsonic server
     * @param requestUrl The fully formed request URL (with service + parameters)
     * @return JSON response from server
     * @throws IOException if I/O error reading from the server
     * @throws JSONException if request returns malformed JSON (will not happen unless you do
     * something weird like pass in a URL that doesn't point to a Subsonic server)
     */
    private static JSONObject sendRequest(URL requestUrl) throws
            IOException, JSONException {
        Log.d(TAG, "sending URL request to server: " + requestUrl.toString());
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

        String jsonString = null;
        try {
            InputStream in = connection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext()) {
                jsonString = scanner.next();
            }

        } finally {
            connection.disconnect();
        }

        return new JSONObject(jsonString);
    }

    /**
     * Sends an authentication request to the server. Used for testing.
     *
     * @param requestUser User on whose behalf we are sending request
     * @return true if the authentication request was successful.
     */
    // TODO Would like to share the error request when it fails
    public static boolean authenticate(SubsonicUser requestUser) {
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser, SubsonicService.PING);
        URL requestUrl = buildUrl(request);
        boolean requestSuccessful = false;
        try {
            requestSuccessful = SubsonicJsonParseUtils.requestSuccessful(sendRequest(requestUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return requestSuccessful;
    }

    /**
     * Sends request to the server to return a list containing all artists on server.
     *
     * @param requestUser User on whose behalf we are making request
     * @return List of all Artists on the server
     * @throws IOException Thrown if I/O error reading from the server.
     * @throws JSONException if response from server is not valid JSON (i.e. you passed an invalid
     * server address)
     */
    static List<Artist> getAllArtists(SubsonicUser requestUser) throws
            IOException, JSONException {
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser, SubsonicService.GET_ARTISTS);
        URL requestUrl = buildUrl(request);
        return SubsonicJsonParseUtils.parseGetArtists(sendRequest(requestUrl));
    }

    /**
     * Sends request to the server to get albums for a given artist
     * @param artistId ID of the requested artist
     * @param requestUser User attached to request
     * @return Artist object from the server
     * @throws IOException if I/O error reading from server
     * @throws JSONException if response from server is not valid JSON
     */
    static List<Album> getArtistAlbums(String artistId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, artistId);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ARTIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return SubsonicJsonParseUtils.parseGetArtist(sendRequest(requestUrl));
    }

    private static final String ALBUM_SEARCH_TYPE_QUERY = "type";
    private static final String ALL_ARTIST_PARAM = "alphabeticalByName";

    /**
     * Sends request to get all albums, sorted alphabetically
     * @param requestUser User attached to request
     * @return List of all albums present on the server
     * @throws IOException if I/O error reading from server
     * @throws JSONException if response from server is not valid JSON
     */
    public static List<Album> getAllAlbums(SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ALBUM_SEARCH_TYPE_QUERY, ALL_ARTIST_PARAM);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ALBUM_LIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return SubsonicJsonParseUtils.parseGetAlbumList(sendRequest(requestUrl));
    }

    public static List<Artist> getSimilarArtists(String artistId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, artistId);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ARTIST_INFO, optionalParams);
        URL requestUrl = buildUrl(request);
        return SubsonicJsonParseUtils.parseGetSimilarArtists(sendRequest(requestUrl));
    }

    private static Bitmap getBitmapFromURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
        connection.disconnect();
        return image;
    }

    public static Bitmap getCoverArt(String path, SubsonicUser requestUser) throws
            IOException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, path);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_COVER_ART, optionalParams);
        URL requestURL = buildUrl(request);
        return getBitmapFromURL(requestURL);
    }

    public static Bitmap getCoverArt(Album album, SubsonicUser requestUser) throws
            IOException {
        return getCoverArt(album.getCoverArt(), requestUser);
    }

    public static Bitmap getCoverArt(Song song, SubsonicUser requestUser) throws
            IOException {
        return getCoverArt(song.getId(), requestUser);
    }

    public static List<Song> getAlbum(String albumId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, albumId);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ALBUM, optionalParams);
        URL requestUrl = buildUrl(request);
        return SubsonicJsonParseUtils.parseGetAlbum(sendRequest(requestUrl));
    }
}
