package com.example.substandard.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.ui.OnAlbumClickListener;
import com.example.substandard.ui.model.AlbumsByArtistViewModel;
import com.example.substandard.ui.model.AlbumsByArtistViewModelFactory;
import com.example.substandard.utility.InjectorUtils;

import java.util.List;

/**
 * A {@link Fragment} to display a list of all albums by an artist, within the artist view
 * activity.
 */
public class AlbumsByArtistFragment extends AbstractArtistViewFragment implements
    AlbumAdapter.ItemOnClickListener {
    private final static String TAG = AlbumsByArtistFragment.class.getSimpleName();

    private RecyclerView albumsView;
    private ProgressBar progressBar;

    private AlbumsByArtistViewModel albumsViewModel;
    private AlbumAdapter albumAdapter;

    private OnAlbumClickListener albumClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAlbumClickListener) {
            albumClickListener = (OnAlbumClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAlbumClickListener");
        }
    }

    /**
     * Helper method for all setup needed to get a RecyclerView running
     * @param rootView the Fragment view
     */
    private void setUpRecyclerView(View rootView) {
        Log.d(TAG, "creating RecyclerView for: " + getArtist().getName());
        albumsView = rootView.findViewById(R.id.rv_albums_by_artist);
        GridLayoutManager layoutManager =
                new GridLayoutManager(getContext(), 2);
        albumsView.setLayoutManager(layoutManager);
        albumsView.setHasFixedSize(true);

        albumAdapter = new AlbumAdapter(getContext(), this);
        albumsView.setAdapter(albumAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Helper method for initializing the ViewModel
     */
    private void setUpArtistsViewModel() {
        Log.d(TAG, "setting up albums view model for: " + getArtist().getName());
        progressBar.setVisibility(View.VISIBLE);

        AlbumsByArtistViewModelFactory factory = InjectorUtils
                .provideAlbumsByArtistViewModelFactory(getContext(), getArtist());
        albumsViewModel = new ViewModelProvider(this, factory)
                .get(AlbumsByArtistViewModel.class);
        albumsViewModel.getAlbums().observe(this, new Observer<List<Album>>() {
            @Override
            public void onChanged(List<Album> albums) {
                Log.d(TAG, "fetched albums: " + albums.toString());
                albumAdapter.setAlbums(albums);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_albums_by_artist, container, false);
//        Log.d(TAG, "created album list fragment for artist " + getArtist().getName());
        progressBar = rootView.findViewById(R.id.albums_by_artist_pb);
        setUpRecyclerView(rootView);
        setUpArtistsViewModel();
        return rootView;
    }

    @Override
    public void onItemClick(Album album) {
        Log.d(TAG, "clicked on album: " + album.getName());
        albumClickListener.onAlbumClick(album);
    }
}
