package com.example.substandard.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.example.substandard.R;
import com.example.substandard.database.network.SubsonicNetworkUtils;

/**
 * Background service to verify login information. If the login is successful,
 * the service will save the login information to the SharedPreferences file.
 *
 * This Intent *must* have username, password, server address, and a ResultReceiver
 * passed in as extras.
 */
public class LoginIntentService extends IntentService {
    public static final String USERNAME_EXTRA_KEY = "username";
    public static final String SERVER_EXTRA_KEY = "server";
    public static final String PASSWORD_EXTRA_KEY = "password";
    public static final String RECEIVER_EXTRA_KEY = "receiver";

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILED = 1;


    public LoginIntentService() {
        super(LoginIntentService.class.getSimpleName());
    }

    private void writeToSharedPreferences(SubsonicNetworkUtils.SubsonicUser user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_salt_key), user.getSalt());
        editor.putString(getString(R.string.pref_auth_token_key), user.getAuthToken());
        editor.putString(getString(R.string.pref_username_key), user.getUsername());
        editor.putString(getString(R.string.pref_server_key), user.getServerAddress());
        editor.apply();
    }

    private SubsonicNetworkUtils.SubsonicUser getUserFromIntent(Intent intent) {
        String username = intent.getStringExtra(USERNAME_EXTRA_KEY);
        String server = intent.getStringExtra(SERVER_EXTRA_KEY);
        String password = intent.getStringExtra(PASSWORD_EXTRA_KEY);

        return new SubsonicNetworkUtils.SubsonicUser(server, username, password);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER_EXTRA_KEY);
        SubsonicNetworkUtils.SubsonicUser user = getUserFromIntent(intent);
        boolean loginSuccessful = SubsonicNetworkUtils.authenticate(user);

        Bundle args = new Bundle();

        if (loginSuccessful) {
            writeToSharedPreferences(user);
            resultReceiver.send(STATUS_SUCCESS, args);
        } else {
            resultReceiver.send(STATUS_FAILED, args);
        }
    }
}
