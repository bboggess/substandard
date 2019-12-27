package com.example.substandard.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.cover.CoverArtAdapter;
import com.example.substandard.cover.CoverArtListener;
import com.example.substandard.database.data.Album;
import com.example.substandard.ui.CoverArtImageView;

import java.util.List;

/**
 * {@link androidx.recyclerview.widget.RecyclerView.Adapter} for the {@link RecyclerView} used in
 * {@link AlbumsByArtistFragment} for displaying all albums by a given artist.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumAdapterViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private final Context context;

    private List<Album> albums;
    private ItemOnClickListener clickListener;

    AlbumAdapter(@NonNull Context context, ItemOnClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
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
        Album boundAlbum = albums.get(position);
        holder.albumView.setText(boundAlbum.getName());
        // TODO add album artist name to album object, and set here
        holder.coverArtAdapter = new CoverArtAdapter(context, boundAlbum, holder);
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

    interface ItemOnClickListener {
        /**
         * Called when an album in the RecyclerView is clicked on.
         * @param album album that was clicked
         */
        void onItemClick(Album album);
    }

    /**
     * All Adapters need a ViewHolder
     */
    class AlbumAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            CoverArtListener  {
        private TextView albumView;
        private CoverArtImageView coverArtView;
        private TextView artistView;
        private CoverArtAdapter coverArtAdapter;
        private ProgressBar coverArtLoading;

        AlbumAdapterViewHolder(View view) {
            super(view);
            albumView = view.findViewById(R.id.album_name_tv);
            coverArtView = view.findViewById(R.id.cover_art_view);
            artistView = view.findViewById(R.id.album_artist_name_tv);
            coverArtLoading = view.findViewById(R.id.album_cover_pb);
            coverArtView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Album album = albums.get(getAdapterPosition());
            clickListener.onItemClick(album);
        }

        @Override
        public void onCoverLoad(Bitmap coverArtImage) {
            coverArtView.setImageBitmap(coverArtImage);
            coverArtLoading.setVisibility(View.INVISIBLE);
        }
    }
}
