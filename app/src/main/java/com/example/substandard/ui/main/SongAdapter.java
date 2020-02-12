package com.example.substandard.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Song;
import com.example.substandard.ui.CoverArtImageView;
import com.example.substandard.ui.ViewHolderItemClickListener;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Song> songs;
    private Context context;

    private ViewHolderItemClickListener<Song> clickListener;

    /*
     * It turns out that having a scrollable header on a RecyclerView is kind of
     * annoying. One solution suggested on Stack Overflow was to treat the header as an
     * item, and then have two types of view holders.
     *
     * This works. So the header is the cover art image, the items are songs.
     */
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Bitmap coverArt;

    SongAdapter(Context context, ViewHolderItemClickListener<Song> clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.song_list_item, parent, false);
            return new SongAdapterViewHolder(view);
        } else if (viewType == TYPE_HEADER) {
            ImageView coverArtView = new CoverArtImageView(context);
            coverArtView.setMinimumWidth(parent.getMeasuredWidth());
            return new SongAdapterHeader(coverArtView);
        }

        throw new RuntimeException("there is no type matching " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SongAdapterViewHolder) {
            Song boundSong = getSong(position);
            SongAdapterViewHolder viewHolder = (SongAdapterViewHolder) holder;
            viewHolder.titleView.setText(boundSong.getTitle());
            // If you pass in an int, it is treated as a resource ID
            viewHolder.trackNumView.setText(Integer.toString(boundSong.getTrackNum()));
        } else if (holder instanceof SongAdapterHeader) {
            SongAdapterHeader header = (SongAdapterHeader) holder;
            header.coverArtView.setImageBitmap(coverArt);
        }
    }

    private Song getSong(int position) {
        return songs.get(position - 1);
    }

    @Override
    public int getItemCount() {
        return (null == songs ? 0 : songs.size()) + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_HEADER : TYPE_ITEM;
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

    public void setCoverArt(Bitmap coverArt) {
        this.coverArt = coverArt;
        notifyDataSetChanged();
    }

    class SongAdapterHeader extends RecyclerView.ViewHolder {
        private ImageView coverArtView;

        public SongAdapterHeader(View view) {
            super(view);
            this.coverArtView = (ImageView) view;
        }
    }
}
