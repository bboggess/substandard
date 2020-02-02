package com.example.substandard.database.network;

import android.net.Uri;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class SubsonicUser implements AbstractNetworkRequest.Authenticator {

    private String serverAddress;
    private String username;
    private String authToken;
    private String salt;


    /*
     * Query parameters for building URL requests to server.
     *
     * All except for FORMAT_QUERY are required in all requests.
     * USERNAME_QUERY: Username sending the request
     * AUTH_TOKEN_QUERY: An authentication token = md5(password + salt)
     * SALT_QUERY: A random string used to compute the password hash. Must be at least 6 characters.
     */
    private final static String USERNAME_QUERY = "u";
    private final static String AUTH_TOKEN_QUERY = "t";
    private final static String SALT_QUERY = "s";

    public SubsonicUser(String serverAddress, String username, String password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.salt = createSalt();
        this.authToken = createAuthToken(password, salt);
    }

    SubsonicUser(String serverAddress, String username, String authToken, String salt) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.authToken = authToken;
        this.salt = salt;
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

    public String getServerAddress() {
        return serverAddress;
    }

    void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    public String getAuthToken() {
        return authToken;
    }

    void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getSalt() {
        return salt;
    }

    void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public Uri.Builder addAuthenticationParams(Uri.Builder builder) {
        return builder.appendQueryParameter(USERNAME_QUERY, getUsername())
                .appendQueryParameter(AUTH_TOKEN_QUERY, getAuthToken())
                .appendQueryParameter(SALT_QUERY, getSalt());
    }
}
