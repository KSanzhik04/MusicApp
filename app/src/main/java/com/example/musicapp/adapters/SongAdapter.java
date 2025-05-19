package com.example.musicapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public void setCurrentlyPlayingSong(Song song) {
    }

    public interface OnSongActionListener {
        void onPlayClick(Song song);
        void onEditClick(Song song);
        void onDeleteClick(Song song);
    }

    private List<Song> songs;
    private final OnSongActionListener listener;

    public SongAdapter(OnSongActionListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.title);
        holder.artist.setText(song.artist);

        holder.btnPlay.setOnClickListener(v -> listener.onPlayClick(song));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(song));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(song));
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, artist;
        final ImageButton btnPlay, btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
            btnPlay = itemView.findViewById(R.id.btn_play);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}