package com.example.musicapp.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.adapters.SongAdapter;
import com.example.musicapp.database.AppDatabase;
import com.example.musicapp.dialogs.SongDetailsDialog;
import com.example.musicapp.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class PlaylistFragment extends Fragment implements SongAdapter.OnSongActionListener {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private AppDatabase database;
    private int userId;
    private MediaPlayer mediaPlayer;
    private Song currentlyPlayingSong;

    public static PlaylistFragment newInstance(int userId) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        FloatingActionButton addButton = view.findViewById(R.id.add_button);

        database = AppDatabase.getDatabase(requireContext());
        userId = getArguments() != null ? getArguments().getInt("user_id", -1) : -1;

        setupRecyclerView();
        loadSongs();

        addButton.setOnClickListener(v -> showSongDialog(null));

        return view;
    }

    private void setupRecyclerView() {
        adapter = new SongAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadSongs() {
        new Thread(() -> {
            List<Song> songs = database.songDao().getSongsByUser(userId);
            requireActivity().runOnUiThread(() -> adapter.setSongs(songs));
        }).start();
    }

    @Override
    public void onPlayClick(Song song) {
        if (currentlyPlayingSong != null && currentlyPlayingSong.id == song.id && mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopPlaying();
            return;
        }

        stopPlaying();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(song.filePath);
            mediaPlayer.prepareAsync();
            currentlyPlayingSong = song;

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                Toast.makeText(requireContext(), "Now playing: " + song.title, Toast.LENGTH_SHORT).show();
                adapter.setCurrentlyPlayingSong(song);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlaying();
                adapter.setCurrentlyPlayingSong(null);
            });

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error playing song", Toast.LENGTH_SHORT).show();
            stopPlaying();
        }
    }

    @Override
    public void onEditClick(Song song) {
        showSongDialog(song);
    }

    @Override
    public void onDeleteClick(Song song) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Song")
                .setMessage("Are you sure you want to delete this song?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        database.songDao().delete(song);
                        requireActivity().runOnUiThread(this::loadSongs);
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentlyPlayingSong = null;
    }

    private void showSongDialog(Song song) {
        SongDetailsDialog dialog = new SongDetailsDialog(song, savedSong -> {
            new Thread(() -> {
                if (song == null) {
                    database.songDao().insert(savedSong);
                } else {
                    database.songDao().update(savedSong);
                }
                requireActivity().runOnUiThread(this::loadSongs);
            }).start();
        });
        dialog.show(getParentFragmentManager(), "SongDetailsDialog");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }
}