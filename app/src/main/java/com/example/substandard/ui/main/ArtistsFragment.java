package com.example.substandard.ui.main;

import android.content.Context;
import android.content.Intent;
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
import com.example.substandard.database.data.Artist;
import com.example.substandard.ui.artistdetail.ArtistViewActivity;
import com.example.substandard.utility.InjectorUtils;

import java.util.List;


/**
 * A simple {@link Fragment} for displaying a list of all artists in the library within the
 * main activity.
 */
public class ArtistsFragment extends Fragment implements ArtistAdapter.ItemClickListener {
    private static final String TAG = ArtistsFragment.class.getSimpleName();

    private ArtistAdapter artistAdapter;
    private RecyclerView recyclerView;

    private OnFragmentInteractionListener mListener;

    private ArtistViewModel artistViewModel;

    public ArtistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArtistsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setUpRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_artists);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        artistAdapter = new ArtistAdapter(getContext(), this);
        recyclerView.setAdapter(artistAdapter);
    }

    private void setUpArtistsViewModel() {
        ArtistViewModelFactory factory = InjectorUtils.provideArtistViewModelFactory(getContext());
        artistViewModel = new ViewModelProvider(getActivity(), factory).get(ArtistViewModel.class);
        artistViewModel.getArtists().observe(this, new Observer<List<Artist>>() {
            @Override
            public void onChanged(List<Artist> artists) {
                Log.d(TAG, "updating UI on database change");
                artistAdapter.setArtists(artists);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);
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

    @Override
    public void onItemClick(Artist artist) {
        Log.d(TAG, "handling RecyclerView click");
        Intent artistViewIntent = new Intent(getContext(), ArtistViewActivity.class);
        artistViewIntent.putExtra(ArtistViewActivity.ARTIST_ID_EXTRA, artist.getId());
        startActivity(artistViewIntent);
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
