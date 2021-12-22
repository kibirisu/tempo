package com.cappielloantonio.play.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.play.App;
import com.cappielloantonio.play.R;
import com.cappielloantonio.play.glide.CustomGlideRequest;
import com.cappielloantonio.play.interfaces.MediaCallback;
import com.cappielloantonio.play.model.Song;
import com.cappielloantonio.play.repository.QueueRepository;
import com.cappielloantonio.play.repository.SongRepository;
import com.cappielloantonio.play.service.MusicPlayerRemote;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

public class DiscoverSongAdapter extends RecyclerView.Adapter<DiscoverSongAdapter.ViewHolder> {
    private static final String TAG = "DiscoverSongAdapter";

    private final LayoutInflater inflater;
    private final Context context;
    private final MainActivity activity;

    private List<Song> songs;

    public DiscoverSongAdapter(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.songs = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_home_discover_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.textTitle.setText(MusicUtil.getReadableString(song.getTitle()));
        holder.textAlbum.setText(MusicUtil.getReadableString(song.getAlbumName()));

        CustomGlideRequest.Builder
                .from(context, song.getPrimary(), CustomGlideRequest.SONG_PIC, null)
                .build()
                .into(holder.cover);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        startAnimation(holder);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setItems(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textTitle;
        TextView textAlbum;
        ImageView cover;

        ViewHolder(View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.title_discover_song_label);
            textAlbum = itemView.findViewById(R.id.album_discover_song_label);
            cover = itemView.findViewById(R.id.discover_song_cover_image_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            List<Song> opener = new ArrayList<>();
            opener.add(songs.get(getBindingAdapterPosition()));
            MusicPlayerRemote.openQueue(opener, 0, true);

            QueueRepository queueRepository = new QueueRepository(App.getInstance());
            queueRepository.insertAllAndStartNew(opener);

            activity.setBottomSheetInPeek(true);
            activity.setBottomSheetMusicInfo(songs.get(getBindingAdapterPosition()));

            SongRepository songRepository = new SongRepository(App.getInstance());
            songRepository.getInstantMix(songs.get(getBindingAdapterPosition()), 20, new MediaCallback() {
                @Override
                public void onError(Exception exception) {
                    Log.e(TAG, "onError() " + exception.getMessage());
                }

                @Override
                public void onLoadMedia(List<?> media) {
                    MusicPlayerRemote.enqueue((List<Song>) media);
                }
            });
        }
    }

    private void startAnimation(ViewHolder holder) {
        holder.cover.animate()
                .setDuration(20000)
                .setStartDelay(10)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .scaleX(1.4f)
                .scaleY(1.4f)
                .start();
    }
}