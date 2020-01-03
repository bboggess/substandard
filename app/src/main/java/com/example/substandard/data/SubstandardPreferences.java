package com.example.substandard.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.substandard.R;

/**
 * Helper class for interacting with SharedPreferences
 */
public class SubstandardPreferences {
    /**
     * Reads the address for the user's Subsonic server from the SharedPreferences file
     * @param context needed for opening SharedPreferences
     * @return saved server address, or empty string if none found
     */
    public static String getPreferredServerAddress(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_server_key), "");
    }

    /**
     * Reads the address for the user's Subsonic username from the SharedPreferences file
     * @param context
     * @return saved username, or empty string if none found
     */
    public static String getPreferredUsername(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_username_key), "");
    }

    /**
     * Reads the address for the user's Subsonic authentication token from the SharedPreferences file.
     * Note that we don't ever save the password, just token + salt.
     * @param context
     * @return saved authentication token, or empty string if none found
     */
    public static String getPreferredAuthToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_auth_token_key), "");
    }

    /**
     * Reads the salt associated to the authentication token from the SharedPreferences file
     * @param context
     * @return saved salt, or empty string if none found
     */
    public static String getPreferredSalt(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_salt_key), "");
    }
}
