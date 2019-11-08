package com.example.substandard.ui.artistdetail;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.utility.InjectorUtils;

import java.util.List;

/**
 * A {@link Fragment} to display a list of all albums by an artist, within the artist view
 * activity.
 */
public class AlbumsByArtistFragment extends Fragment {
    private final static String TAG = AlbumsByArtistFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    private RecyclerView albumsView;

    private AlbumsByArtistViewModel albumsViewModel;
    private AlbumAdapter albumAdapter;
    private Artist artist;

    public AlbumsByArtistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AlbumsByArtistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumsByArtistFragment newInstance() {
        AlbumsByArtistFragment fragment = new AlbumsByArtistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    void setArtist(Artist artist) {
        this.artist = artist;
    }

    /**
     * Helper method for all setup needed to get a RecyclerView running
     * @param rootView the Fragment view
     */
    private void setUpRecyclerView(View rootView) {
        albumsView = rootView.findViewById(R.id.rv_albums_by_artist);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        albumsView.setLayoutManager(layoutManager);
        albumsView.setHasFixedSize(true);

        albumAdapter = new AlbumAdapter(getContext());
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
        AlbumsByArtistViewModelFactory factory = InjectorUtils.provideAlbumsByArtistViewModelFactory(getContext(), artist);
        albumsViewModel = new ViewModelProvider(getActivity(), factory).get(AlbumsByArtistViewModel.class);
        albumsViewModel.getAlbums().observe(this, new Observer<List<Album>>() {
            @Override
            public void onChanged(List<Album> albums) {
                // TODO create UI
                Log.d(TAG, "fetched albums: " + albums.toString());
                albumAdapter.setAlbums(albums);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_albums_by_artist, container, false);
        Log.d(TAG, "created album list fragment for artist " + artist.getName());
        setUpRecyclerView(rootView);
        setUpArtistsViewModel();
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
