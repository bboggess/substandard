package com.example.substandard.database.network;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractNetworkRequest {
    private Authenticator authenticator;
    private Map<String, String> additionalParams;

    /**
     * Interface to be used by NetworkRequestUtils. Implementing classes need to be able
     * to add authentication parameters into a URL request. Prototypical example is SubsonicUser,
     * which keeps user, password, etc.
     */
    public interface Authenticator {
        /**
         * Adds authentication parameters to request URL
         * @param builder an ongoing URL builder which needs authentication arguments
         * @return the same URL builder, but with authentication info added
         */
        Uri.Builder addAuthenticationParams(Uri.Builder builder);
    }

    public AbstractNetworkRequest(Authenticator authenticator) {
        this.authenticator = authenticator;
        additionalParams = new HashMap<>();
    }

    public AbstractNetworkRequest(Authenticator authenticator, Map<String, String> additionalParams) {
        this(authenticator);
        this.additionalParams = additionalParams;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }


    public URL buildUrl() throws MalformedURLException {
        Uri.Builder builder = getBaseUrl().buildUpon();
        getAuthenticator().addAuthenticationParams(builder);
        Map<String, String> additionalParams = getAllQueryParameters();
        for (String key : additionalParams.keySet()) {
            builder.appendQueryParameter(key, additionalParams.get(key));
        }

        return new URL(builder.build().toString());
    }

    /**
     * Any additional query information which is needed by the request beyond service address and
     * authentication info.
     * @return
     */
    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public abstract Map<String, String> getAllQueryParameters();

    public abstract Uri getBaseUrl();
}
