package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.ui.model.AlbumsByArtistViewModel;
import com.example.substandard.ui.model.AlbumsByArtistViewModelFactory;
import com.example.substandard.utility.InjectorUtils;

/**
 * A {@link Fragment} to display a list of all albums by an artist, within the artist view
 * activity.
 */
public class AlbumsByArtistFragment extends AbstractArtistViewFragment implements
    ViewHolderItemClickListener<Album> {
    private final static String TAG = AlbumsByArtistFragment.class.getSimpleName();

    private RecyclerView albumsView;
    private ProgressBar progressBar;

    private AlbumsByArtistViewModel albumsViewModel;
    private AlbumAdapter albumAdapter;

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

        albumAdapter = new AlbumAdapter(getContext(), this, this);
        albumsView.setAdapter(albumAdapter);
        albumAdapter.setAlbums(getArtistAndAllAlbums().getAlbums());
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
        albumsViewModel.getAlbums().observe(this, (albums) -> {
                Log.d(TAG, "fetched albums: " + albums.toString());
                albumAdapter.setAlbums(albums);
                progressBar.setVisibility(View.INVISIBLE);
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_albums_by_artist, container, false);
        AbstractArtistViewFragment parentFragment = (ArtistViewFragment) getParentFragment();
        setArtist(parentFragment.getArtistAndAllAlbums());

        progressBar = rootView.findViewById(R.id.albums_by_artist_pb);
        setUpRecyclerView(rootView);
//        setUpArtistsViewModel();
        return rootView;
    }

    @Override
    public void onItemClick(Album album) {
        Log.d(TAG, "clicked on album: " + album.getName());
        NavDirections directions = ArtistViewFragmentDirections
                .actionArtistViewFragmentToSongListFragment(album.getId(), album.getName());
        Navigation.findNavController(getView()).navigate(directions);
    }
}
