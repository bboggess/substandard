package com.example.substandard.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.substandard.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

}
