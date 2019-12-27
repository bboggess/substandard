package com.example.substandard.cover;

import com.example.substandard.SubstandardApplication;
import com.example.substandard.database.data.Album;

import java.io.File;

public class CachedCoverArt extends CoverArt {
    public static final File CACHE_DIR = SubstandardApplication.getInstance().getCacheDir();
    public static final String FILE_EXTENSION = ".png";

    @Override
    public boolean isOffline() {
        return true;
    }

    @Override
    public String getUrl() {
        return null;
    }

    private String getFullPath(Album album) {
        return CACHE_DIR + album.getCoverArt() + FILE_EXTENSION;
    }
}
