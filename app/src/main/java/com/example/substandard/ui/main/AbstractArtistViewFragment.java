package com.example.substandard.ui.main;

import androidx.fragment.app.Fragment;

import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.AlbumAndArtist;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistAndAllAlbums;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Fragment that all Fragments being used for views belonging to a specific artist
 * should extend. This saves a lot of pointless boilerplate I had before.
 */
public abstract class AbstractArtistViewFragment extends Fragment {
    private ArtistAndAllAlbums artist;

    public AbstractArtistViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDetach() {
        super.onDetach();
        artist = null;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArtistViewFragment.
     */
    public static AbstractArtistViewFragment newInstance(Artist artist) {
        return new ArtistViewFragment();
    }


    void setArtist(ArtistAndAllAlbums artist) {
        this.artist = artist;
    }

    /**
     *
     * @return Artist to whom the Fragment is associated.
     */
    public Artist getArtist() {
        return artist.getArtist();
    }

    public ArtistAndAllAlbums getArtistAndAllAlbums() {
        return artist;
    }

    public List<Album> getAlbums() {
        return artist.getAlbums();
    }

    public List<AlbumAndArtist> getAlbumsWithArtist() {
        List<AlbumAndArtist> toRet = new ArrayList<>(getAlbums().size());

        for (Album album : getAlbums()) {
            toRet.add(new AlbumAndArtist(album ,getArtist()));
        }

        return toRet;
    }
}
