package com.example.substandard.database.network.musicbrainz;

import android.net.Uri;

import com.example.substandard.database.network.AbstractNetworkRequest;

import java.util.HashMap;
import java.util.Map;

public class MusicBrainzNetworkRequest extends AbstractNetworkRequest {
    private static final String BASE_URL = "https://musicbrainz.org/ws/2/";
    private MusicBrainzService service;
    private String mbid;

    private final static String REQUEST_RETURN_FORMAT = "json";
    private final static String RETURN_FORMAT_QUERY = "fmt";
    private final static Map<String, String> DEFAULT_PARAMS = getDefaultParams();

    private static Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put(RETURN_FORMAT_QUERY, REQUEST_RETURN_FORMAT);
        return params;
    }

    public enum MusicBrainzService {
        ARTIST ("artist");

        private String text;
        MusicBrainzService(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public MusicBrainzNetworkRequest(MusicBrainzService service, String mbid) {
        // no authentication required for lookups from MusicBrainz
        super(builder -> {}, DEFAULT_PARAMS);
        this.service = service;
        this.mbid = mbid;
    }

    @Override
    public Map<String, String> getAllQueryParameters() {
        return getAdditionalParams();
    }

    @Override
    public Uri getBaseUrl() {
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                .appendPath(service.getText())
                .appendPath(mbid);
        return builder.build();
    }

}
