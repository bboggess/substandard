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
/* TODO finish writing request methods. Need to work out some more implementation details first.
 */
public class SubsonicNetworkUtils {
    /*
     * Tag for debugging purposes.
     */
    private static final String TAG = SubsonicNetworkUtils.class.getSimpleName();

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
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ALBUM_LIST, optionalParams);
        return SubsonicJsonParseUtils.parseGetAlbumList(NetworkRequestUtils.sendRequest(request));
    }

    public static List<Artist> getSimilarArtists(String artistId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, artistId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ARTIST_INFO, optionalParams);
        return SubsonicJsonParseUtils.parseGetSimilarArtists(NetworkRequestUtils.sendRequest(request));
    }

    public static Bitmap getCoverArt(String path, SubsonicUser requestUser) throws
            IOException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, path);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_COVER_ART, optionalParams);
        return NetworkRequestUtils.getBitmapFromURL(request);
    }

    public static Bitmap getCoverArt(Album album, SubsonicUser requestUser) throws
            IOException {
        return getCoverArt(album.getCoverArt(), requestUser);
    }

    public static Bitmap getCoverArt(Song song, SubsonicUser requestUser) throws
            IOException {
        return getCoverArt(song.getId(), requestUser);
    }


    /**
     * Downloads an audio file and then writes it to the given File
     * @param songId
     * @param toWrite
     * @param requestUser
     * @throws IOException
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

    public static List<Song> getAlbum(String albumId, SubsonicUser requestUser) throws
            IOException, JSONException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, albumId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.GET_ALBUM, optionalParams);
        return SubsonicJsonParseUtils.parseGetAlbum(NetworkRequestUtils.sendRequest(request));
    }

    public static URL getStream(String songId, SubsonicUser requestUser) throws MalformedURLException {
        Map<String, String> optionalParams = new HashMap<>();
        optionalParams.put(SubsonicNetworkRequest.ID_QUERY, songId);
        SubsonicNetworkRequest request = new SubsonicNetworkRequest(requestUser,
                SubsonicNetworkRequest.SubsonicService.STREAM, optionalParams);
        return request.buildUrl();
    }
}
