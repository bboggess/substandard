package com.example.substandard.ui.artistdetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;

import java.util.List;

/**
 * {@link androidx.recyclerview.widget.RecyclerView.Adapter} for the {@link RecyclerView} used in
 * {@link AlbumsByArtistFragment} for displaying all albums by a given artist.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumAdapterViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private final Context context;

    private List<Album> albums;

    AlbumAdapter(@NonNull Context context) {
        this.context = context;
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
    class AlbumAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView albumView;

        AlbumAdapterViewHolder(View view) {
            super(view);
            albumView = view.findViewById(R.id.album_tv);
        }
    }
}
