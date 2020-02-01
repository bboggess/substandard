package com.example.substandard.cover;

public class CachedCoverArt extends CoverArt {
    @Override
    public boolean isOffline() {
        return true;
    }

    @Override
    public String getUrl() {
        return null;
    }

}
