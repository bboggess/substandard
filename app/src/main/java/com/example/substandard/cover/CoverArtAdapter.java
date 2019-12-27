package com.example.substandard.cover;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.substandard.database.SubsonicArtistRepository;
import com.example.substandard.database.data.Album;
import com.example.substandard.utility.InjectorUtils;

// TODO We don't care whether we're getting a cached or downloaded cover art file
// build infrastructure to support both!
// TODO rewrite as a service. Adapter is a strange choice, no?
/**
 * Service class for loading cover art off of the main thread, and delivering the loaded
 * image to a {@link CoverArtListener}. Loading either means reading cached image from
 * memory/off the disk, or downloading from the server.
 */
public class CoverArtAdapter {
    private CoverArtListener listener;
    private final LiveData<Bitmap> coverArtBitmap;
    /*
     * We need to call observeForever on the LiveData, and so it is important to
     * store a reference to the Observer so we can remove it after loading is finished
     * to avoid memory leaks.
     */
    private final Observer<Bitmap> observer;

    /**
     *
     * @param context The context which is requesting the cover art
     * @param album The album whose cover is requested
     * @param listener The UI component to be notified when the load is complete
     */
    public CoverArtAdapter(Context context, Album album, CoverArtListener listener) {
        this.listener = listener;
        SubsonicArtistRepository repository = InjectorUtils.provideArtistRepository(context);
        this.coverArtBitmap = repository.getCoverArt(album);

        // Call onDataBind when cover finishes loading
        observer = new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                onDataBind(bitmap);
            }
        };
        coverArtBitmap.observeForever(observer);
    }

    /**
     * Called when loading is complete, and delivering the image to the listener.
     * @param coverArt Loaded cover art image.
     */
    private void onDataBind(Bitmap coverArt) {
        listener.onCoverLoad(coverArt);
        coverArtBitmap.removeObserver(observer);
    }
}
