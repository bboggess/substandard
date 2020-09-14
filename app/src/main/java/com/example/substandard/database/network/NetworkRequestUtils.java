package com.example.substandard.database.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Static methods for sending network requests
 */
public class NetworkRequestUtils {
    private static final String USER_AGENT_KEY = "User-Agent";
    private static final String USER_AGENT = "substandard-music-player/0.0.1 ( b.boggess727@gmail.com )";

    /**
     * Sends a request to an online service which returns a JSON Object.
     *
     * @param request object containing info for building request URL
     * @return JSON response from the online service, or null if exception thrown
     * @throws JSONException Request didn't receive a JSON response
     * @throws IOException unable to connect to URL
     */
    public static JSONObject sendRequest(AbstractNetworkRequest request) throws
            JSONException, IOException {
        URL requestUrl = request.buildUrl();
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestProperty(USER_AGENT_KEY, USER_AGENT);

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

        return null != jsonString ? new JSONObject(jsonString) : null;
    }

    /**
     * Used to download images when you already have a URL given to you, say when
     * Subsonic gives you an artist image URL directly. Do not use unless another service
     * directly hands you a URL.
     *
     * @param requestUrl URL where image can be found
     * @return image requested
     * @throws IOException If unable to download image
     */
    public static Bitmap getBitmapFromURL(URL requestUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.connect();
        Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
        connection.disconnect();
        return image;
    }

    /**
     * Get a bitmap from a network request; to be used when you have to build your own
     * URLs.
     *
     * @param request object used to build a request URL
     * @return requested image
     * @throws IOException unable to download image
     */
    public static Bitmap getBitmapFromURL(AbstractNetworkRequest request) throws IOException {
        URL requestUrl = request.buildUrl();
        return getBitmapFromURL(requestUrl);
    }
}
