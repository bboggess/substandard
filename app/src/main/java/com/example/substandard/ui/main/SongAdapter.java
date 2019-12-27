package com.example.substandard.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongAdapterViewHolder> {
    private List<Song> songs;
    private Context context;

    SongAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public SongAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.song_list_item, parent, false);
        return new SongAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapterViewHolder holder, int position) {
        Song boundSong = songs.get(position);
        holder.titleView.setText(boundSong.getTitle());
        holder.trackNumView.setText(Integer.toString(position + 1));
    }

    @Override
    public int getItemCount() {
        return null == songs ? 0 : songs.size();
    }

    void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    class SongAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView trackNumView;

        public SongAdapterViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.song_tv);
            trackNumView = view.findViewById(R.id.track_num_tv);
        }
    }
}
