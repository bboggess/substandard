package com.example.substandard.database.network.subsonic;

import android.net.Uri;

import com.example.substandard.database.network.AbstractNetworkRequest;

import java.util.HashMap;
import java.util.Map;

public class SubsonicNetworkRequest extends AbstractNetworkRequest {

    private SubsonicUser user;
    private SubsonicService service;

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
     * Required query parameters.
     *
     * VERSION_QUERY: The Subsonic protocol used by the client. This is stored in SUBSONIC_PROTOCOL_VERSION
     * CLIENT_QUERY: A string uniquely identifying this app. Will be stored elsewhere.
     * FORMAT_QUERY: (Optional) The requested format of response. Supported values are "xml", "json", and
     *               "jsonp". The default value is "xml".
     */
    private final static String VERSION_QUERY = "v";
    private final static String CLIENT_QUERY = "c";
    private final static String FORMAT_QUERY = "f";

    private final static Map<String, String> DEFAULT_PARAMS = getDefaultParams();

    private static Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap();
        params.put(CLIENT_QUERY, APP_NAME);
        params.put(VERSION_QUERY, SUBSONIC_PROTOCOL_VERSION);
        params.put(FORMAT_QUERY, REQUEST_RETURN_FORMAT);
        return params;
    }

    /*
     * Optional parameters needed for various services.
     */
    // used in many services
    public final static String ID_QUERY = "id";
    // Optional in getAlbumList, getRandomSongs,
    public final static String NUM_TO_RETURN_QUERY = "size";
    public final static String ARTIST_QUERY = "artist";
    public final static String ALBUM_LIST_TYPE_QUERY = "type";

    public enum SubsonicService {
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

    public enum AlbumListType {
        RANDOM ("random"),
        NEWEST ("newest"),
        FREQUENT ("frequent"),
        RECENT("recent");

        private final String text;
        AlbumListType(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public SubsonicNetworkRequest(SubsonicUser user, SubsonicService service) {
        super(user, DEFAULT_PARAMS);
        this.user = user;
        this.service = service;
    }

    public SubsonicNetworkRequest(SubsonicUser user, SubsonicService service, Map<String, String> additionalParam) {
        super(user, additionalParam);
        // Go ahead and all default parameters to the optional ones
        getAdditionalParams().putAll(DEFAULT_PARAMS);
        this.user = user;
        this.service = service;
    }


    @Override
    public Map<String, String> getAllQueryParameters() {
        return getAdditionalParams();
    }

    @Override
    public Uri getBaseUrl() {
        Uri.Builder builder = Uri.parse(user.getServerAddress()).buildUpon()
                .appendPath(SUBSONIC_REST_PATH)
                .appendPath(service.getText());
        return builder.build();
    }
}
