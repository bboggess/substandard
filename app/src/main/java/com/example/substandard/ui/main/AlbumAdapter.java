package com.example.substandard.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Album;
import com.example.substandard.ui.CoverArtImageView;
import com.example.substandard.ui.model.SongListViewModel;
import com.example.substandard.utility.InjectorUtils;

import java.util.List;

/**
 * {@link androidx.recyclerview.widget.RecyclerView.Adapter} for the {@link RecyclerView} used in
 * {@link AlbumsByArtistFragment} for displaying all albums by a given artist.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumAdapterViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private final Context context;
    private final LifecycleOwner owner;

    private List<Album> albums;
    private ViewHolderItemClickListener<Album> clickListener;

    AlbumAdapter(@NonNull Context context, LifecycleOwner owner, ViewHolderItemClickListener<Album> clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.owner = owner;
    }

    @NonNull
    @Override
    public AlbumAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.album_list_item, parent, false);
        return new AlbumAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapterViewHolder holder, int position) {
        // Get a hold of the loaded album
        Album boundAlbum = albums.get(position);
        holder.albumView.setText(boundAlbum.getName());

        // Setup and start service to download cover art
        SongListViewModel viewModel = InjectorUtils
                .provideSongListViewModelFactory(context, boundAlbum.getId())
                .create(SongListViewModel.class);
        SubsonicLibraryRepository repository = InjectorUtils.provideLibraryRepository(context);
        viewModel.setCoverArt(repository.getCoverArt(boundAlbum, context));
        viewModel.getCoverArt().observe(owner, bitmap -> {
            holder.coverArtView.setImageBitmap(bitmap);
            holder.coverArtLoading.setVisibility(View.INVISIBLE);
        });

        // Loading bar for album cover
        holder.coverArtLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        if (albums == null) {
            return 0;
        } else {
            return albums.size();
        }
    }

    void setAlbums(List<Album> albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }

    /**
     * All Adapters need a ViewHolder
     */
    class AlbumAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        private TextView albumView;
        private CoverArtImageView coverArtView;
        private ProgressBar coverArtLoading;

        AlbumAdapterViewHolder(View view) {
            super(view);
            albumView = view.findViewById(R.id.album_name_tv);
            coverArtView = view.findViewById(R.id.cover_art_view);

            coverArtLoading = view.findViewById(R.id.album_cover_pb);
            coverArtView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Album album = albums.get(getAdapterPosition());
            clickListener.onItemClick(album);
        }
    }
}
