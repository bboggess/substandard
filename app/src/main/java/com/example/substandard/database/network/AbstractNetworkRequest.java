package com.example.substandard.database.network;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to build requests from online services for JSON or image resources. A network request
 * object contains all of the necessary information for building a request URL. The Authenticator
 * builds authentication into the request (e.g. username/password or API key), and must be
 * implemented on a service by service basis.
 *
 * You can fruitfully think of this as a box which takes in some request information and spits
 * out the URL which will give you that information.
 */
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

    /**
     * This is the main purpose of the class, and is used in making network requests.
     *
     * @return valid URL to which a request may be sent
     * @throws MalformedURLException
     */
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

    /**
     * @return base URL of the service, e.g. address of Subsonic server
     */
    public abstract Uri getBaseUrl();
}
