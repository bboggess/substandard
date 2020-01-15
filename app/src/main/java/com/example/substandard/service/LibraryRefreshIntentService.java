package com.example.substandard.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.utility.InjectorUtils;

public class LibraryRefreshIntentService extends IntentService {
    public LibraryRefreshIntentService(){
        super(LibraryRefreshIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SubsonicLibraryRepository repository = InjectorUtils.provideLibraryRepository(this);
        repository.refreshLibrary();
    }
}
