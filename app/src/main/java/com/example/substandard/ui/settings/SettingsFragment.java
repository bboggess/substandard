package com.example.substandard.ui.settings;


import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.example.substandard.R;
import com.example.substandard.database.network.SubsonicNetworkUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements
       Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        // Set up click listener for login dialog
        Preference p = findPreference(getString(R.string.pref_login_key));
        if (null != p) {
            p.setOnPreferenceClickListener(this);
        }

        // Here we set up all preference summaries
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        int numPrefs = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < numPrefs; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (!(preference instanceof CheckBoxPreference)) {
                String value = "";
                if (preference.getKey().equals(getString(R.string.pref_login_key))) {
                    value = sharedPreferences.getString(getString(R.string.pref_username_key), "");
                } else if (preference instanceof EditTextPreference) {
                    value = sharedPreferences.getString(preference.getKey(), "");
                }
                setPreferenceSummary(preference, value);

            }
        }
    }


    private class DialogClickListener implements LoginDialogFragment.LoginDialogListener {
        // TODO Use the Service to do this!
        @Override
        public void onDialogPositiveClick(DialogFragment dialog) {
            if (dialog instanceof LoginDialogFragment) {
                Dialog view = dialog.getDialog();
                EditText usernameEditText = view.findViewById(R.id.username_edit_text);
                EditText passwordEditText = view.findViewById(R.id.password_edit_text);
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String salt = SubsonicNetworkUtils.createSalt();
                String authToken = SubsonicNetworkUtils.createAuthToken(password, salt);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.pref_salt_key), salt);
                editor.putString(getString(R.string.pref_auth_token_key), authToken);
                editor.putString(getString(R.string.pref_username_key), username);
                editor.apply();
            }
        }

        @Override
        public void onDialogNegativeClick(DialogFragment dialog) {

        }
//

    }
    /*
     * If the use clicks on the login setting, pop up a login dialog.
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.pref_login_key))) {
            DialogFragment loginDialog = new LoginDialogFragment(new DialogClickListener());
            loginDialog.show(getFragmentManager(), getString(R.string.login));
        }

        return true;
    }

    /*
     * Update summaries when preference is changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, sharedPreferences.getString(key, ""));
        Preference preference = findPreference(key);
        if (preference != null) {
            if (preference instanceof EditTextPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    /**
     * Help method to set the text to show beneath a Preference on the PreferenceScreen.
     * @param preference the preference whose summary to set
     * @param value the new value of the summary
     */
    private void setPreferenceSummary(Preference preference, String value) {
        if (!(preference instanceof CheckBoxPreference)) {
            preference.setSummary(value);
        }
    }
}
