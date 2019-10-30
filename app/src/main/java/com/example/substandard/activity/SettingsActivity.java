package com.example.substandard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.substandard.R;
import com.example.substandard.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings, new SettingsFragment())
                .commit();
    }
}
