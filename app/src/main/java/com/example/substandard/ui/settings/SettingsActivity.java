package com.example.substandard.ui.settings;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.example.substandard.R;
import com.example.substandard.database.network.SubsonicNetworkUtils;

public class SettingsActivity extends AppCompatActivity implements
    LoginDialogFragment.LoginDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.settings));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


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

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
}
