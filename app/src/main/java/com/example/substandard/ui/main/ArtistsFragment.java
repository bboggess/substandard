package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Artist;
import com.example.substandard.ui.model.ArtistViewModel;
import com.example.substandard.ui.model.ArtistViewModelFactory;
import com.example.substandard.utility.InjectorUtils;


/**
 * A simple {@link Fragment} for displaying a list of all artists in the library within the
 * main activity.
 */
public class ArtistsFragment extends Fragment implements ViewHolderItemClickListener<Artist> {
    private static final String TAG = ArtistsFragment.class.getSimpleName();

    private ArtistAdapter artistAdapter;
    private RecyclerView recyclerView;

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
        artistViewModel = new ViewModelProvider(this, factory).get(ArtistViewModel.class);
        artistViewModel.getArtists().observe(this, (artists) -> {
                Log.d(TAG, "updating UI on database change");
                artistAdapter.setArtists(artists);
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


    @Override
    public void onItemClick(Artist artist) {
        // I am annoyed that I have to pass in the name as well, but otherwise the label
        // isn't correctly updated
        NavDirections directions = ArtistsFragmentDirections
                .actionMainFragmentToArtistViewFragment(artist.getId(), artist.getName());
        Navigation.findNavController(getView()).navigate(directions);
    }

}
