package com.example.substandard.utility;

import android.content.Context;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.SubsonicArtistRepository;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.data.ArtistDatabase;
import com.example.substandard.database.network.SubsonicNetworkDataSource;
import com.example.substandard.fragment.ArtistViewModelFactory;

/**
 * Static methods for linking various components
 */
public class InjectorUtils {

    public static SubsonicArtistRepository provideArtistRepository(Context context) {
        ArtistDatabase database = ArtistDatabase.getInstance(context.getApplicationContext());
        ArtistDao artistDao = database.artistDao();
        AppExecutors executors = AppExecutors.getInstance();
        SubsonicNetworkDataSource dataSource = SubsonicNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return SubsonicArtistRepository.getInstance(artistDao, dataSource, executors);
    }

    public static ArtistViewModelFactory provideArtistViewModelFactory(Context context) {
        SubsonicArtistRepository repository = provideArtistRepository(context);
        return new ArtistViewModelFactory(repository);
    }
}
