package com.example.substandard.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.substandard.R;
import com.example.substandard.database.network.subsonic.SubsonicUser;

/**
 * Helper class for interacting with SharedPreferences
 */
public class SubstandardPreferences {
    private static final String TAG = SubstandardPreferences.class.getSimpleName();

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

    /**
     * Remembers whether or not the library database has been initialized so we only do it once
     * @param context
     * @param isInit true if has been initialized, false if hasn't been (deleted or something)
     */
    public static void setDatabaseInitialized(Context context, boolean isInit) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_db_initialized_key), isInit);
        editor.apply();
    }

    /**
     *
     * @param context
     * @return true if database has been initialized, false otherwise
     */
    public static boolean isDatabaseInitialized(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_db_initialized_key), false);
    }

    /**
     * Writes the preferred user's info to the SharedPreferences file to read from later.
     * Note that it does *not* save the password directly
     * @param context
     * @param user
     */
    public static void writePreferredUser(Context context, SubsonicUser user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_salt_key), user.getSalt());
        editor.putString(context.getString(R.string.pref_auth_token_key), user.getAuthToken());
        editor.putString(context.getString(R.string.pref_username_key), user.getUsername());
        editor.putString(context.getString(R.string.pref_server_key), user.getServerAddress());
        editor.apply();
        Log.d(TAG, "writing preferred user");
    }
}
