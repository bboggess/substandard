package com.example.substandard.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.service.CoverArtDownloadIntentService;
import com.example.substandard.service.CoverArtResultReceiver;
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
    private ViewHolderItemClickListener<Album> clickListener;

    AlbumAdapter(@NonNull Context context, ViewHolderItemClickListener<Album> clickListener) {
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
        // Get a hold of the loaded album
        Album boundAlbum = albums.get(position);
        holder.albumView.setText(boundAlbum.getName());

        // Setup and start service to download cover art
        Intent coverArtIntent = new Intent(context, CoverArtDownloadIntentService.class);
        coverArtIntent.putExtra(CoverArtDownloadIntentService.IMAGE_PATH_EXTRA_KEY, boundAlbum.getCoverArt());
        coverArtIntent.putExtra(CoverArtDownloadIntentService.RESULT_RECEIVER_EXTRA_KEY, holder.resultReceiver);
        context.startService(coverArtIntent);

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
            View.OnClickListener,
            CoverArtResultReceiver.CoverArtReceiver {
        private TextView albumView;
        private CoverArtImageView coverArtView;
        private TextView artistView;
        private CoverArtResultReceiver resultReceiver;
        private ProgressBar coverArtLoading;

        AlbumAdapterViewHolder(View view) {
            super(view);
            albumView = view.findViewById(R.id.album_name_tv);
            coverArtView = view.findViewById(R.id.cover_art_view);
            artistView = view.findViewById(R.id.album_artist_name_tv);

            coverArtLoading = view.findViewById(R.id.album_cover_pb);
            coverArtView.setOnClickListener(this);

            resultReceiver = new CoverArtResultReceiver(new Handler());
            resultReceiver.setReceiver(this);
        }

        @Override
        public void onClick(View v) {
            Album album = albums.get(getAdapterPosition());
            clickListener.onItemClick(album);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == CoverArtDownloadIntentService.STATUS_SUCCESS) {
                Bitmap coverArt = resultData.getParcelable(CoverArtDownloadIntentService.BITMAP_EXTRA_KEY);
                coverArtView.setImageBitmap(coverArt);
                coverArtLoading.setVisibility(View.INVISIBLE);
            } else {
                // TODO stock image or something
            }
        }
    }
}
