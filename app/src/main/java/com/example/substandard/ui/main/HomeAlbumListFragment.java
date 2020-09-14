package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.ui.ViewHolderItemClickListener;
import com.example.substandard.ui.model.AlbumListViewModel;
import com.example.substandard.ui.model.AlbumListViewModelFactory;
import com.example.substandard.utility.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeAlbumListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeAlbumListFragment extends Fragment implements
        ViewHolderItemClickListener<Album> {
    private static final String TAG = HomeAlbumListFragment.class.getSimpleName();

    private static final String ARG_ALBUM_LIST = "album_list";
    private static final String ARG_LIST_TYPE = "list_type";

    private List<String> albumIdList;
    private String listType;

    private RecyclerView albumView;

    public HomeAlbumListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ids list of ids of albums to show
     * @param type type of list (recent, favorite, newest)
     * @return A new instance of fragment HomeAlbumListFragment.
     */
    public static HomeAlbumListFragment newInstance(ArrayList<String> ids, String type) {
        HomeAlbumListFragment fragment = new HomeAlbumListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_ALBUM_LIST, ids);
        args.putString(ARG_LIST_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumIdList = getArguments().getStringArrayList(ARG_ALBUM_LIST);
            listType = getArguments().getString(ARG_LIST_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: creating view for " + albumIdList);
        View rootView = inflater.inflate(R.layout.fragment_home_album_list, container, false);

        TextView header = rootView.findViewById(R.id.tv_album_list_header);
        header.setText(listType);

        albumView = rootView.findViewById(R.id.rv_albums);

        AlbumAdapter albumAdapter = new AlbumAdapter(getContext(), this, this);
        albumView.setAdapter(albumAdapter);

        AlbumListViewModelFactory factory = InjectorUtils.provideAlbumListViewModelFactory(getContext(), albumIdList);
        AlbumListViewModel albumViewModel = new ViewModelProvider(this, factory).get(AlbumListViewModel.class);
        albumViewModel.getAlbums().observe(getViewLifecycleOwner(), albums -> {
            Log.d(TAG, "onCreateView: returned albums from DB " + albums);
            albumAdapter.setAlbums(albums);
        });

        return rootView;
    }

    @Override
    public void onItemClick(Album obj) {

    }
}