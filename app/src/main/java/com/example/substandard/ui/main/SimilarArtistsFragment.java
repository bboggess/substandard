package com.example.substandard.ui.main;

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
import com.example.substandard.ui.model.SimilarArtistsViewModel;
import com.example.substandard.ui.model.SimilarArtistsViewModelFactory;
import com.example.substandard.utility.InjectorUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SimilarArtistsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SimilarArtistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimilarArtistsFragment extends AbstractArtistViewFragment implements
    ArtistAdapter.ItemClickListener {
    private static final String TAG = SimilarArtistsFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ArtistAdapter artistAdapter;
    private SimilarArtistsViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "created View");
        View rootView = inflater.inflate(R.layout.fragment_similar_artists, container, false);
        setUpRecyclerView(rootView);
        setUpArtistsViewModel();
        return rootView;
    }

    private void setUpRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_similar_artists);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        artistAdapter = new ArtistAdapter(getContext(), this);
        recyclerView.setAdapter(artistAdapter);
        Log.d(TAG, "set up RecyclerView for: " + getArtist().getName());
    }

    private void setUpArtistsViewModel() {
        SimilarArtistsViewModelFactory factory = InjectorUtils
                .provideSimilarArtistsViewModelFactory(getContext(), getArtist().getId());
        viewModel = new ViewModelProvider(this, factory)
                .get(SimilarArtistsViewModel.class);
        viewModel.getSimilarArtists().observe(this, new Observer<List<Artist>>() {
            @Override
            public void onChanged(List<Artist> artists) {
                Log.d(TAG, "updating UI with similar artists");
                artistAdapter.setArtists(artists);
            }
        });
    }

    @Override
    public void onItemClick(Artist artist) {
        getListener().onArtistClick(artist);
    }

}
