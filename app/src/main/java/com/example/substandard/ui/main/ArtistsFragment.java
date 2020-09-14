package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Artist;
import com.example.substandard.ui.ViewHolderItemClickListener;
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

    public ArtistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArtistsFragment.
     */
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
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_artists);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        artistAdapter = new ArtistAdapter(requireContext(), this,this);
        recyclerView.setAdapter(artistAdapter);
    }

    private void setUpArtistsViewModel() {
        ArtistViewModelFactory factory = InjectorUtils.provideArtistViewModelFactory(getContext());
        ArtistViewModel artistViewModel = new ViewModelProvider(this, factory)
                                                .get(ArtistViewModel.class);
        artistViewModel.getArtists().observe(getViewLifecycleOwner(), (artists) -> {
                Log.d(TAG, "updating UI on database change");
                artistAdapter.setArtists(artists);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpRecyclerView(view);
        setUpArtistsViewModel();
    }

    @Override
    public void onItemClick(Artist artist) {
        // I am annoyed that I have to pass in the name as well, but otherwise the label
        // isn't correctly updated
        NavDirections directions = ArtistsFragmentDirections
                .actionArtistsFragmentToArtistViewFragment(artist.getId(), artist.getName());
        Navigation.findNavController(requireView()).navigate(directions);
    }

}
