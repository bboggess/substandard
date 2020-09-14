package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.ui.ViewHolderItemClickListener;

/**
 * A {@link Fragment} to display a list of all albums by an artist, within the artist view
 * activity.
 */
public class AlbumsByArtistFragment extends AbstractArtistViewFragment implements
        ViewHolderItemClickListener<Album> {
    private final static String TAG = AlbumsByArtistFragment.class.getSimpleName();

    /**
     * Helper method for all setup needed to get a RecyclerView running
     * @param rootView the Fragment view
     */
    private void setUpRecyclerView(View rootView) {
        Log.d(TAG, "creating RecyclerView for: " + getArtist().getName());
        RecyclerView albumsView = rootView.findViewById(R.id.rv_albums_by_artist);
        GridLayoutManager layoutManager =
                new GridLayoutManager(getContext(), 2);
        albumsView.setLayoutManager(layoutManager);
        albumsView.setHasFixedSize(true);

        AlbumAdapter albumAdapter = new AlbumAdapter(requireContext(), this, this);
        albumsView.setAdapter(albumAdapter);
        albumAdapter.setAlbums(getAlbumsWithArtist());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_albums_by_artist, container, false);
        AbstractArtistViewFragment parentFragment = (ArtistViewFragment) getParentFragment();
        assert parentFragment != null;
        setArtist(parentFragment.getArtistAndAllAlbums());

        setUpRecyclerView(rootView);
        return rootView;
    }

    @Override
    public void onItemClick(Album album) {
        Log.d(TAG, "clicked on album: " + album.getName());
        NavDirections directions = ArtistViewFragmentDirections
                .actionArtistViewFragmentToSongListFragment(album.getId(), album.getName());
        Navigation.findNavController(requireView()).navigate(directions);
    }
}
