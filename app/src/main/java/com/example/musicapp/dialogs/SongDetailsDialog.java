package com.example.musicapp.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.musicapp.R;
import com.example.musicapp.model.Song;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SongDetailsDialog extends DialogFragment {
    private static final int PICK_AUDIO_REQUEST = 101;

    private EditText titleInput;
    private EditText artistInput;
    private TextView fileInfoText;
    private String selectedFilePath;
    private final Song currentSong;
    private final SongActionListener listener;

    public interface SongActionListener {
        void onSongSaved(Song song);
    }

    public SongDetailsDialog(Song song, SongActionListener listener) {
        this.currentSong = song;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_song_details, null);

        titleInput = view.findViewById(R.id.et_title);
        artistInput = view.findViewById(R.id.et_artist);
        fileInfoText = view.findViewById(R.id.tv_file_info);
        Button selectFileBtn = view.findViewById(R.id.btn_select_file);

        if (currentSong != null) {
            titleInput.setText(currentSong.title);
            artistInput.setText(currentSong.artist);
            if (currentSong.filePath != null) {
                fileInfoText.setText(currentSong.filePath.substring(
                        currentSong.filePath.lastIndexOf("/") + 1));
                selectedFilePath = currentSong.filePath;
            }
        }

        selectFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, PICK_AUDIO_REQUEST);
        });

        builder.setView(view)
                .setTitle(currentSong == null ? "Add Song" : "Edit Song")
                .setPositiveButton("Save", (dialog, id) -> saveSong())
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void saveSong() {
        String title = titleInput.getText().toString().trim();
        String artist = artistInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Enter title");
            return;
        }
        if (artist.isEmpty()) {
            artistInput.setError("Enter artist");
            return;
        }
        if (selectedFilePath == null) {
            Toast.makeText(requireContext(), "Select audio file first", Toast.LENGTH_SHORT).show();
            return;
        }

        Song song = currentSong != null ?
                new Song(title, artist, selectedFilePath, currentSong.userId) :
                new Song(title, artist, selectedFilePath, getUserId());

        if (currentSong != null) {
            song.id = currentSong.id;
        }

        if (listener != null) {
            listener.onSongSaved(song);
        }
        dismiss();
    }

    private int getUserId() {
        return requireActivity()
                .getSharedPreferences("musicapp_prefs", 0)
                .getInt("user_id", -1);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    // Для Android 11+ нужно явно запросить флаг PERSISTABLE
                    final int takeFlags = data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // Получаем временные права
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri,
                            takeFlags
                    );

                    // Копируем файл во внутреннее хранилище приложения
                    selectedFilePath = copyFileToAppStorage(uri);
                    String fileName = getFileNameFromUri(uri);
                    fileInfoText.setText(fileName != null ? fileName : "Audio file selected");

                } catch (IOException | SecurityException e) {
                    Toast.makeText(requireContext(), "Error accessing file", Toast.LENGTH_SHORT).show();
                    Log.e("SongDetailsDialog", "File access error", e);
                }
            }
        }
    }

    private String copyFileToAppStorage(Uri uri) throws IOException {
        // Создаем уникальное имя файла
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "AUDIO_" + timeStamp + ".mp3";

        File outputFile = new File(requireContext().getFilesDir(), fileName);

        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(outputFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }

        return outputFile.getAbsolutePath();
    }
    private String getFileNameFromUri(Uri uri) {
        String displayName = null;
        try (Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e("SongDetailsDialog", "Error getting file name", e);
        }
        return displayName;
    }
}