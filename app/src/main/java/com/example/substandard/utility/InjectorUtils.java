package com.example.substandard.utility;

import android.content.Context;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.SubsonicArtistRepository;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.data.ArtistDatabase;
import com.example.substandard.database.network.SubsonicNetworkDataSource;
import com.example.substandard.ui.artistdetail.AlbumsByArtistViewModelFactory;
import com.example.substandard.ui.artistdetail.ArtistDetailViewModelFactory;
import com.example.substandard.ui.main.ArtistViewModelFactory;

/**
 * Static methods for linking various components
 */
public class InjectorUtils {

    /**
     * Helper method for getting data repository from the UI packages
     * @param context Context used for getting user preferences, etc
     * @return repository which can be used to make requests from the Artist database
     */
    private static SubsonicArtistRepository provideArtistRepository(Context context) {
        ArtistDatabase database = ArtistDatabase.getInstance(context.getApplicationContext());
        ArtistDao artistDao = database.artistDao();
        AppExecutors executors = AppExecutors.getInstance();
        SubsonicNetworkDataSource dataSource = SubsonicNetworkDataSource.getInstance(context, executors);
        return SubsonicArtistRepository.getInstance(artistDao, dataSource, executors);
    }

    /**
     * Helper method for creating ViewModelFactories from the UI. Needs to be handled here
     * in order to access a repository.
     * @param context Context used for getting user preferences, etc
     * @return factory for creating ViewModels for accessing data from UI
     */
    public static ArtistViewModelFactory provideArtistViewModelFactory(Context context) {
        SubsonicArtistRepository repository = provideArtistRepository(context);
        return new ArtistViewModelFactory(repository);
    }

    public static ArtistDetailViewModelFactory provideArtistDetailViewModelFactory(Context context, int artistId) {
        SubsonicArtistRepository repository = provideArtistRepository(context);
        return new ArtistDetailViewModelFactory(repository, artistId);
    }

    public static AlbumsByArtistViewModelFactory provideAlbumsByArtistViewModelFactory(Context context, Artist artist) {
        SubsonicArtistRepository repository = provideArtistRepository(context);
        return new AlbumsByArtistViewModelFactory(repository, artist);
    }
}
