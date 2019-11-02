package com.example.substandard.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Artist;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistAdapterViewHolder> {
    private final static String TAG = ArtistAdapter.class.getSimpleName();

    private final Context context;

    private List<Artist> artists;

    public ArtistAdapter(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ArtistAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.artist_list_item, parent, false);
        return new ArtistAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAdapterViewHolder holder, int position) {
        Artist boundArtist = artists.get(position);
        holder.artistTextView.setText(boundArtist.getName());
        Log.d(TAG, "bound view holder: " + boundArtist.getName());

    }

    @Override
    public int getItemCount() {
        if (artists == null) {
            return 0;
        } else {
            return artists.size();
        }
    }

    public void setArtists(List<Artist> artists) {
        Log.d(TAG, "setting artists in Adapter");
        this.artists = artists;
        notifyDataSetChanged();
    }

    class ArtistAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView artistTextView;

        ArtistAdapterViewHolder(View view) {
            super(view);
            artistTextView = view.findViewById(R.id.artist_tv);
        }
    }
}