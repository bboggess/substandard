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

    private ViewHolderItemClickListener<Song> clickListener;

    SongAdapter(Context context, ViewHolderItemClickListener<Song> clickListener) {
        this.context = context;
        this.clickListener = clickListener;
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
        // If you pass in an int, it is treated as a resource ID
        holder.trackNumView.setText(Integer.toString(boundSong.getTrackNum()));
    }

    @Override
    public int getItemCount() {
        return null == songs ? 0 : songs.size();
    }

    void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    class SongAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleView;
        private TextView trackNumView;

        public SongAdapterViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.song_tv);
            trackNumView = view.findViewById(R.id.track_num_tv);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Song clicked = songs.get(getAdapterPosition());
            clickListener.onItemClick(clicked);
        }
    }
}
