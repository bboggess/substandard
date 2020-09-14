package com.example.substandard.database.network.subsonic;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.Song;
import com.example.substandard.database.network.NetworkRequestUtils;
import com.example.substandard.utility.SubstandardPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides static methods for interacting with the Subsonic server over the network. This class
 * only knows about things related to sending network requests to the server; it can not interpret
 * the info.
 */
public class SubsonicNetworkUtils {
    /*
     * Tag for debugging purposes.
     */
    private static final String TAG = SubsonicNetworkUtils.class.getSimpleName();

    /**
     * Sometimes it's nice to just be able to load the user from here, and this does this.
     *
     * Probably should factor this out and move everything to {@link SubstandardPreferences}.
     * @param context needed for shared preferences access
     * @return the currently saved user, or a user with all fields "" if no user found
     */
    public static SubsonicUser getSubsonicUserFromPreferences(Context context) {
        String username = SubstandardPreferences.getPreferredUsername(context);
        String server =  SubstandardPreferences.getPreferredServerAddress(context);
        String authToken = SubstandardPreferences.getPreferredAuthToken(context);
        String salt = SubstandardPreferences.getPreferredSalt(context);
        return new SubsonicUser(server, username, authToken, salt);
    }

    /**
     * Sends an authentication request to the server. Used for testing.
     *
     * @param requestUser User on whose behalf we are sending request
     * @return true if the authentication request was successful.
     */
    // TODO Would like to share the error request when it fails
    public static boolean authenticate(SubsonicUser requestUser) {
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.PING);
        boolean requestSuccessful = false;
        try {
            JSONObject requestJson = NetworkRequestUtils.sendRequest(request);
            requestSuccessful = SubsonicJsonParseUtils.requestSuccessful(requestJson);
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
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ARTISTS);
        return SubsonicJsonParseUtils.parseGetArtists(NetworkRequestUtils.sendRequest(request));
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
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, artistId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ARTIST, optionalParams);
        return SubsonicJsonParseUtils.parseGetArtist(NetworkRequestUtils.sendRequest(request));
    }

    /**
     * Asks the server for similar artists
     * @param artistId id of artist to use in search
     * @param requestUser user for authentication
     * @return list of all similar artists according to server
     * @throws IOException issue connecting to server
     * @throws JSONException unexpected response to request
     */
    public static List<Artist> getSimilarArtists(String artistId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, artistId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ARTIST_INFO, optionalParams);
        return SubsonicJsonParseUtils.parseGetSimilarArtists(NetworkRequestUtils.sendRequest(request));
    }

    /**
     * Asks the server for an image
     * @param path pathname of image as given by the server
     * @param requestUser for auth
     * @return image located at the path
     * @throws IOException if unable to connect to server
     */
    public static Bitmap getCoverArt(String path, SubsonicUser requestUser) throws
            IOException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, path);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_COVER_ART, optionalParams);
        return NetworkRequestUtils.getBitmapFromURL(request);
    }

    /**
     * Get the stored image of the album's cover
     * @param album album whose cover is requested
     * @param requestUser for auth
     * @return bitmap of album cover (if it's a weird result, blame the server)
     * @throws IOException issues connecting to server
     */
    public static Bitmap getCoverArt(Album album, SubsonicUser requestUser) throws
            IOException {
        return getCoverArt(album.getCoverArt(), requestUser);
    }

    /**
     * Get album cover for album that song appears on
     * @param song song we need art for
     * @param requestUser for auth
     * @return bitmap of the song's album's cover
     * @throws IOException issue connecting to server
     */
    public static Bitmap getCoverArt(Song song, SubsonicUser requestUser) throws
            IOException {
        return getCoverArt(song.getId(), requestUser);
    }

    /**
     * Tries to get the artist's top song.
     * @param artist whose top track to fetch
     * @param requestUser for auth
     * @return Either the top track, or null if that information is not found
     * @throws IOException unable to connect to server
     * @throws JSONException unexpected response from server
     */
    public static Song getTopSong(Artist artist, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ARTIST_QUERY, artist.getName());
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_TOP_SONGS, optionalParams);
        return SubsonicJsonParseUtils.parseTopTrack(NetworkRequestUtils.sendRequest(request));
    }

    public static String getArtistThumbnail(Artist artist, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ARTIST_QUERY, artist.getName());
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_TOP_SONGS, optionalParams);
        return SubsonicJsonParseUtils.parseArtistThumbnail(NetworkRequestUtils.sendRequest(request));
    }

    /**
     * Downloads an audio file and then writes it to the given File
     * @param songId id of the song
     * @param toWrite file to write the downloaded audio file to
     * @param requestUser for auth
     */
    public static void downloadSong(String songId, File toWrite, DownloadManager downloadManager, SubsonicUser requestUser)
            throws MalformedURLException {
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.DOWNLOAD);
        Uri uri = Uri.parse(request.buildUrl().toString());
        DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);
        downloadRequest.setDestinationUri(Uri.fromFile(toWrite));
        downloadManager.enqueue(downloadRequest);
    }

    /**
     * Fetch all songs on a given album
     * @param albumId album whose songs we need
     * @param requestUser for auth
     * @return list of all songs on the album
     * @throws IOException unable to connect to server
     * @throws JSONException unexpected response from server
     */
    public static List<Song> getAlbum(String albumId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, albumId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ALBUM, optionalParams);
        return SubsonicJsonParseUtils.parseGetAlbum(NetworkRequestUtils.sendRequest(request));
    }

    /**
     * fetch url for streaming an audio file
     * @param songId id of the song to stream
     * @param requestUser for auth
     * @return url at which one can directly stream the song
     * @throws MalformedURLException issue with query URL for this song ID
     */
    public static URL getStream(String songId, SubsonicUser requestUser) throws MalformedURLException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, songId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.STREAM, optionalParams);
        return request.buildUrl();
    }

    /**
     * Get a list of five albums from the server
     * @param type type of list to create. Can be random albums, newest albums, most recently played,
     *             and most frequently played
     * @param requestUser for auth
     * @return list of IDs returned by the server
     * @throws IOException cannot connect to server
     * @throws JSONException unexpected response from the server
     */
    public static List<String> getAlbumList(SubsonicNetworkRequest.AlbumListType type, SubsonicUser requestUser)
            throws IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ALBUM_LIST_TYPE_QUERY, type.getText());
        optionalParams.put(SubsonicNetworkRequest.NUM_TO_RETURN_QUERY, "5");
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ALBUM_LIST, optionalParams);
        return SubsonicJsonParseUtils.parseGetAlbumList(NetworkRequestUtils.sendRequest(request));
    }
}
