package com.example.musicapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.database.AppDatabase;
import com.example.musicapp.model.User;

public class ProfileFragment extends Fragment {
    public static ProfileFragment newInstance(int userId) {
        return new ProfileFragment();
    }

    private static final int PICK_IMAGE_REQUEST = 100;

    private EditText usernameEditText;
    private ImageView avatarImageView;
    private AppDatabase database;
    private int userId;
    private String avatarUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameEditText = view.findViewById(R.id.username_edit_text);
        avatarImageView = view.findViewById(R.id.avatar_image_view);
        Button changeAvatarButton = view.findViewById(R.id.change_avatar_button);
        Button saveButton = view.findViewById(R.id.save_button);

        database = AppDatabase.getDatabase(requireContext());
        userId = requireActivity().getSharedPreferences("musicapp_prefs", 0)
                .getInt("user_id", -1);

        loadUserData();

        changeAvatarButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        saveButton.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        new Thread(() -> {
            User user = database.userDao().getUserById(userId);
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    usernameEditText.setText(user.username);
                    if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
                        Glide.with(requireContext())
                                .load(Uri.parse(user.avatarUri))
                                .into(avatarImageView);
                        avatarUri = user.avatarUri;
                    }
                }
            });
        }).start();
    }

    private void saveUserData() {
        String newUsername = usernameEditText.getText().toString().trim();
        if (newUsername.isEmpty()) {
            usernameEditText.setError("Enter username");
            return;
        }

        new Thread(() -> {
            User user = database.userDao().getUserById(userId);
            if (user != null) {
                user.username = newUsername;
                if (avatarUri != null) {
                    user.avatarUri = avatarUri;
                }
                database.userDao().update(user);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                avatarImageView.setImageURI(uri);
                avatarUri = uri.toString();
            }
        }
    }
}