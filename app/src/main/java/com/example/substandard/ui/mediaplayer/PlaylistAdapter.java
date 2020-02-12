package com.example.substandard.ui.mediaplayer;

import android.content.Context;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.ui.ViewHolderItemClickListener;

import java.util.List;

/**
 * Adapter for displaying list of songs in playlist fragment
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private static final String TAG = PlaylistAdapter.class.getSimpleName();

    private List<MediaSessionCompat.QueueItem> queue;
    private MediaSessionCompat.QueueItem currentTrack;

    private ViewHolderItemClickListener<MediaSessionCompat.QueueItem> clickListener;

    private Context context;

    public PlaylistAdapter(Context context, ViewHolderItemClickListener<MediaSessionCompat.QueueItem> listener) {
        this.context = context.getApplicationContext();
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.media_player_playlist_list_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        MediaDescriptionCompat currentItem = queue.get(position).getDescription();
        Log.d(TAG, "bound view holder: media item " + currentItem.getTitle());
        holder.trackNameView.setText(currentItem.getTitle());
        // Not currently set up to support this. Oops.
//        holder.albumCoverView.setImageBitmap(currentItem.getIconBitmap());
    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    public void setQueue(List<MediaSessionCompat.QueueItem> queue) {
        this.queue = queue;
        notifyDataSetChanged();
    }

    public void setCurrentTrack(MediaSessionCompat.QueueItem currentTrack) {
        this.currentTrack = currentTrack;
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView trackNameView;
        private ImageView albumCoverView;

        public PlaylistViewHolder(View view) {
            super(view);
            trackNameView = view.findViewById(R.id.song_name_tv);
            albumCoverView = view.findViewById(R.id.album_art_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            MediaSessionCompat.QueueItem clicked = queue.get(getAdapterPosition());
            clickListener.onItemClick(clicked);
        }
    }
}
