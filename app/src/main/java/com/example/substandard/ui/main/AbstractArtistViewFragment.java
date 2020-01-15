package com.example.substandard.ui.main;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.example.substandard.database.data.Artist;
import com.example.substandard.ui.OnMediaClickListener;

/**
 * Abstract Fragment that all Fragments being used for views belonging to a specific artist
 * should extend. This saves a lot of pointless boilerplate I had before.
 */
public abstract class AbstractArtistViewFragment extends Fragment {
    private OnMediaClickListener mListener;

    private Artist artist;

    public AbstractArtistViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        artist = null;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArtistViewFragment.
     */
    public static AbstractArtistViewFragment newInstance(Artist artist) {
        ArtistViewFragment fragment = new ArtistViewFragment();
        fragment.setArtist(artist);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMediaClickListener) {
            mListener = (OnMediaClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMediaClickListener");
        }
    }

    void setArtist(Artist artist) {
        this.artist = artist;
    }

    /**
     *
     * @return Artist to whom the Fragment is associated.
     */
    public Artist getArtist() {
        return artist;
    }


    public OnMediaClickListener getListener() {
        return mListener;
    }

}
