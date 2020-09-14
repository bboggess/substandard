package com.example.substandard.utility;

import android.content.Context;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.AlbumDao;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.data.SongDao;
import com.example.substandard.database.data.SubsonicLibraryDatabase;
import com.example.substandard.database.network.subsonic.SubsonicNetworkDataSource;
import com.example.substandard.ui.model.AlbumListViewModelFactory;
import com.example.substandard.ui.model.AlbumsByArtistViewModelFactory;
import com.example.substandard.ui.model.ArtistDetailViewModelFactory;
import com.example.substandard.ui.model.ArtistViewModelFactory;
import com.example.substandard.ui.model.HomeScreenViewModelFactory;
import com.example.substandard.ui.model.SimilarArtistsViewModelFactory;
import com.example.substandard.ui.model.SongListViewModelFactory;

import java.util.List;

/**
 * Static methods for linking various components
 */
public class InjectorUtils {

    /**
     * Helper method for getting data repository from the UI packages
     * @param context Context used for getting user preferences, etc
     * @return repository which can be used to make requests from the Artist database
     */
    public static SubsonicLibraryRepository provideLibraryRepository(Context context) {
        SubsonicLibraryDatabase database = SubsonicLibraryDatabase.getInstance(context.getApplicationContext());
        ArtistDao artistDao = database.artistDao();
        AlbumDao albumDao = database.albumDao();
        SongDao songDao = database.songDao();
        AppExecutors executors = AppExecutors.getInstance();
        SubsonicNetworkDataSource dataSource = SubsonicNetworkDataSource.getInstance(context, executors);
        return SubsonicLibraryRepository.getInstance(artistDao, albumDao, songDao, dataSource, executors);
    }

    /**
     * Helper method for creating ViewModelFactories from the UI. Needs to be handled here
     * in order to access a repository.
     * @param context Context used for getting user preferences, etc
     * @return factory for creating ViewModels for accessing data from UI
     */
    public static ArtistViewModelFactory provideArtistViewModelFactory(Context context) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new ArtistViewModelFactory(repository);
    }

    public static ArtistDetailViewModelFactory provideArtistDetailViewModelFactory(Context context, String artistId) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new ArtistDetailViewModelFactory(repository, artistId);
    }

    public static AlbumsByArtistViewModelFactory provideAlbumsByArtistViewModelFactory(Context context, Artist artist) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new AlbumsByArtistViewModelFactory(repository, artist);
    }

    public static SimilarArtistsViewModelFactory provideSimilarArtistsViewModelFactory(Context context, String artistId) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new SimilarArtistsViewModelFactory(repository, artistId);
    }

    public static SongListViewModelFactory provideSongListViewModelFactory(Context context, String albumId) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new SongListViewModelFactory(repository, albumId);
    }

    public static AlbumListViewModelFactory provideAlbumListViewModelFactory(Context context, List<String> ids) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new AlbumListViewModelFactory(repository, ids);
    }

    public static HomeScreenViewModelFactory provideHomeScreenViewModelFactory(Context context) {
        SubsonicLibraryRepository repository = provideLibraryRepository(context);
        return new HomeScreenViewModelFactory(repository);
    }
}
