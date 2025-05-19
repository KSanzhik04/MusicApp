package com.example.musicapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.musicapp.model.Song;
import java.util.List;

@Dao
public interface SongDao {
    @Insert
    void insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("SELECT * FROM songs WHERE userId = :userId")
    List<Song> getSongsByUser(int userId);
}