package com.example.substandard.database.network.musicbrainz;

import com.example.substandard.database.network.NetworkRequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MusicBrainzNetworkUtils {
    public static String getImageUrl(String mbId) throws IOException, JSONException {
        MusicBrainzNetworkRequest request = new MusicBrainzNetworkRequest(
                MusicBrainzNetworkRequest.MusicBrainzService.ARTIST, mbId);
        JSONObject response = NetworkRequestUtils.sendRequest(request);
        return MusicBrainzJSONUtils.getImageUrl(response);
    }
}
