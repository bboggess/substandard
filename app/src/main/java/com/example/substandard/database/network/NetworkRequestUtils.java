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


    public static JSONObject sendRequest(AbstractNetworkRequest request) throws
            JSONException, IOException {
        URL requestUrl = request.buildUrl();
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

    public static Bitmap getBitmapFromURL(AbstractNetworkRequest request) throws IOException {
        URL requestUrl = request.buildUrl();
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.connect();
        Bitmap image = BitmapFactory.decodeStream(connection.getInputStream());
        connection.disconnect();
        return image;
    }
}
