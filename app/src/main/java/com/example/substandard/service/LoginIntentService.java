package com.example.substandard.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.example.substandard.database.network.SubsonicNetworkUtils;
import com.example.substandard.utility.SubstandardPreferences;

/**
 * Background service to verify login information. If the login is successful,
 * the service will save the login information to the SharedPreferences file.
 *
 * This Intent *must* have username, password, server address, and a ResultReceiver
 * passed in as extras.
 */
// TODO rewrite Settings Activity to use this service to get user info
public class LoginIntentService extends IntentService {
    /**
     * Keys for obtaining extras from the calling Intent
     */
    public static final String USERNAME_EXTRA_KEY = "username";
    public static final String SERVER_EXTRA_KEY = "server";
    public static final String PASSWORD_EXTRA_KEY = "password";
    public static final String RECEIVER_EXTRA_KEY = "receiver";

    /**
     * Constants used for communicating success/failure of login
     */
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILED = 1;


    public LoginIntentService() {
        super(LoginIntentService.class.getSimpleName());
    }

    /**
     * Helper method to retrieve login info from the calling Intent
     * @param intent Intent that was used to start this service
     * @return SubsonicUser with all required info for login
     */
    private SubsonicNetworkUtils.SubsonicUser getUserFromIntent(Intent intent) {
        String username = intent.getStringExtra(USERNAME_EXTRA_KEY);
        String server = intent.getStringExtra(SERVER_EXTRA_KEY);
        String password = intent.getStringExtra(PASSWORD_EXTRA_KEY);

        return new SubsonicNetworkUtils.SubsonicUser(server, username, password);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // used to alert UI of the result of login attempt
        ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER_EXTRA_KEY);

        SubsonicNetworkUtils.SubsonicUser user = getUserFromIntent(intent);
        boolean loginSuccessful = SubsonicNetworkUtils.authenticate(user);

        // the ResultReceiver requires that we pass in an argument Bundle
        Bundle args = new Bundle();

        if (loginSuccessful) {
            SubstandardPreferences.writePreferredUser(this, user);
            resultReceiver.send(STATUS_SUCCESS, args);
        } else {
            resultReceiver.send(STATUS_FAILED, args);
        }
    }
}
