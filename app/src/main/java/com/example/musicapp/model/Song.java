package com.example.musicapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class Song {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String artist;
    public String filePath;
    public int userId;

    public Song(String title, String artist, String filePath, int userId) {
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.userId = userId;
    }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
}