package com.example.substandard.utility;

import android.net.Uri;
import android.util.Log;

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
    /**
     * Tag for debugging purposes.
     */
    private static final String TAG = SubsonicNetworkUtils.class.getSimpleName();

    /**
     * This is the version of the REST API that this app will use. This is value was chosen to be
     * backwards compatible with all version 6.*.* of Subsonic. Not compatible with any earlier versions.
     */
    private final static String SUBSONIC_PROTOCOL_VERSION = "1.14.0";

    /**
     * I prefer to have the results returned in json rather than xml. I don't know why I prefer
     * this. Maybe I had a reason.
     */
    private final static String REQUEST_RETURN_FORMAT = "json";

    private final static String APP_NAME = "substandard";

    /**
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

    /**
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

    /**
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
        GET_ALBUM_LIST ("getAlbumList"),
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
    private static class SubsonicUser {
        private String serverAddress;
        private String username;
        private String password;

        public SubsonicUser(String serverAddress, String username, String password) {
            this.serverAddress = serverAddress;
            this.username = username;
            this.password = password;
        }

        public String getServerAddress() {
            return serverAddress;
        }

        public void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Encapsulates info needed to send a request to the Subsonic server. All requests should
     * go through a SubsonicServerRequest.
     */
    private static class SubsonicServerRequest {
        private SubsonicUser user;
        private SubsonicService service;
        /**
         * Every request needs a user and a service, but some need more information. This should
         * be put in additionalParameters.
         */
        private Map<String, String> additionalParameters;

        public SubsonicServerRequest(SubsonicUser user, SubsonicService service) {
            this.user = user;
            this.service = service;
            this.additionalParameters = Collections.EMPTY_MAP;
        }

        public SubsonicServerRequest(SubsonicUser user, SubsonicService service, Map<String, String> additionalParameters) {
            this.user = user;
            this.service = service;
            this.additionalParameters = additionalParameters;
        }

        public SubsonicUser getUser() {
            return user;
        }

        public void setUser(SubsonicUser user) {
            this.user = user;
        }

        public SubsonicService getService() {
            return service;
        }

        public void setService(SubsonicService service) {
            this.service = service;
        }

        public Map<String, String> getAdditionalParameters() {
            return additionalParameters;
        }

        public void setAdditionalParameters(Map<String, String> additionalParameters) {
            this.additionalParameters = additionalParameters;
        }
    }

    /**
     * Returns the md5 hash of password and salt. Should be used to create the authorization
     * token.
     *
     * @param password The user's Subsonic password
     * @param salt A randomly generated String with which to hash
     * @return The MD5 hash of the concatenated string password + hash
     */
    private static String createAuthToken(String password, String salt) {
        // At one point I did this by hand, but the Apache library is better
        // Weird that calling DigestUtils.md5Hex directly throws exception...
        return new String(Hex.encodeHex(DigestUtils.md5(password + salt)));
    }

    /**
     * Returns a random String of 6 characters to be used as salt in the hash.
     */
    private static String createSalt() {
        // This is a quick way to get a random string
        return UUID.randomUUID().toString().substring(0,6);
    }

    /**
     * Builds a URL formatted for request to Subsonic server.
     * @param request The request we are building a URL to send
     * @return A fully formed URL that can be authenticated by the server
     */
    private static URL buildUrl(SubsonicServerRequest request) {
        String salt = createSalt();
        SubsonicUser requestUser = request.getUser();
        String authToken = createAuthToken(requestUser.getPassword(), salt);
        Uri.Builder uriBuilder = Uri.parse(requestUser.getServerAddress()).buildUpon()
                .appendPath(request.getService().getText())
                .appendQueryParameter(USERNAME_QUERY, requestUser.getUsername())
                .appendQueryParameter(AUTH_TOKEN_QUERY, authToken)
                .appendQueryParameter(SALT_QUERY, salt)
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
     * @param server Base URL of the server
     * @param username The username for the request
     * @param password The password associated to the username
     * @return The JSON returned by the request. Returns null if no/invalid response.
     * @throws IOException If I/O error reading from the server.
     * @throws JSONException If the request returns malformed JSON (i.e. you didn't pass in an
     * address of an actual Subsonic server)
     */
    // TODO I would like this to actually return a boolean -- true if success, false if not
    // think about how to give error message (just log?)
    public static JSONObject authenticate(String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser, SubsonicService.PING);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Sends request to the server to return json file containing artists on server.
     *
     * @param server Base URL of the server
     * @param username Username for the request
     * @param password The user's password
     * @return JSONObject containing artists in library.
     * @throws IOException Thrown if I/O error reading from the server.
     * @throws JSONException if response from server is not valid JSON (i.e. you passed an invalid
     * server address)
     */
    public static JSONObject getArtists(String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username,password);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser, SubsonicService.GET_ARTISTS);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Sends request to the server to return JSON file containing info for a given artist
     * @param artistId ID of the requested artist
     * @param server Base URL of the Server
     * @param username Username for the request
     * @param password The user's password
     * @return JSONObject containing details for the requested artist
     * @throws IOException if I/O error reading from server
     * @throws JSONException if response from server is not valid JSON
     */
    public static JSONObject getArtist(int artistId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(artistId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ARTIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Retrieves list of all genres from the server
     * @param server
     * @param username
     * @param password
     * @return JSONObject containing list of all genres, with song and album counts
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getGenres(String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_GENRES);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets details for the album with given ID, including list of songs.
     * @param albumID
     * @param server
     * @param username
     * @param password
     * @return JSONObject with album info and an array of song objects
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getAlbum(int albumID, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(albumID));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ALBUM, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets details for song with given ID from the server
     * @param songId
     * @param server
     * @param username
     * @param password
     * @return JSONObject with information associated with the song
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getSong(int songId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(songId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_SONG, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject getArtistInfo(int artistId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(artistId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ARTIST_INFO, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets album description, image URLs, and such from last.fm
     * @param albumId
     * @param server
     * @param username
     * @param password
     * @return JSONObject containing album information from last.fm
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getAlbumInfo(int albumId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(albumId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_ALBUM_INFO, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject getSimilarSongs(int songId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(songId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_SIMILAR_SONGS, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject getTopSongs(int artistId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(artistId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_TOP_SONGS, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets a list of all starred songs, albums, and artists.
     * @param server
     * @param username
     * @param password
     * @return JSONObject with an array containing all songs, albums, and artists which have
     * been starred.
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getStarred(String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_STARRED);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets all playlists the user has access to. These playlists can then be accessed by the id
     * attribute.
     * @param server
     * @param username
     * @param password
     * @return JSONObject with an array containing playlist objects
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getPlaylists(String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_PLAYLISTS);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets all files contained in the chosen playlist. The file objects have all song info and paths.
     * @param playlistId
     * @param server
     * @param username
     * @param password
     * @return JSONObject containing a playlist object
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getPlaylist(int playlistId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(playlistId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_PLAYLIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject createPlaylist(String playlistName, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(PLAYLIST_NAME_QUERY, playlistName);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.CREATE_PLAYLIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject updatePlaylist(int playlistId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(PLAYLIST_ID_QUERY, Integer.toString(playlistId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.CREATE_PLAYLIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Delete a saved playlist.
     * @param playlistId
     * @param server
     * @param username
     * @param password
     * @return Empty subsonic response.
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject deletePlaylist(int playlistId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(playlistId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.DELETE_PLAYLIST, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject stream(int fileId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(fileId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.STREAM, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject download(int fileId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(fileId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.DOWNLOAD, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject getCoverArt(int id, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(id));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_COVER_ART, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Searches for lyrics to the given song. The plain text for the lyrics is contained in a
     * lyrics JSON object.
     * @param artist
     * @param song
     * @param server
     * @param username
     * @param password
     * @return JSONObject containing a lyrics object. The lyrics object is empty if no lyrics are
     * found.
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getLyrics(String artist, String song, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ARTIST_QUERY, artist);
        optionalParams.put(SONG_QUERY, song);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_LYRICS, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject starSong(int songId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(songId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.STAR, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject unstarSong(int songId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(songId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.UNSTAR, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    public static JSONObject scrobble(int fileId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(fileId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.SCROBBLE, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets all top-level music folders. The id tags for directories are needed for fetching
     * media files.
     * @param server
     * @param username
     * @param password
     * @return JSONObject containing arrays of musicFolder objects
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getMusicFolders(String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_MUSIC_FOLDERS);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }

    /**
     * Gets a list of all files in the chosen directory. This contains, for instance, the path to
     * each file in the directory.
     * @param musicFolderId
     * @param server
     * @param username
     * @param password
     * @return JSONObject with an array of files in the directory.
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getMusicDirectory(int musicFolderId, String server, String username, String password) throws
            IOException, JSONException {
        SubsonicUser requestUser = new SubsonicUser(server, username, password);
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(ID_QUERY, Integer.toString(musicFolderId));
        SubsonicServerRequest request = new SubsonicServerRequest(requestUser,
                SubsonicService.GET_MUSIC_DIRECTORY, optionalParams);
        URL requestUrl = buildUrl(request);
        return sendRequest(requestUrl);
    }
}
