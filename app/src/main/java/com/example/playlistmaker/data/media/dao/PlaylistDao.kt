package com.example.playlistmaker.data.media.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.data.media.entity.PlaylistEntity

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)                //добавить плейлист

//    @Query("DELETE FROM playlists_table WHERE id = :playlistId")       //удалить плейлист по id
//    suspend fun deletePlaylist(playlistId: Int)

    @Query("SELECT * FROM playlists_table")                            //получить сущности треков
    suspend fun getPlaylists(): List<PlaylistEntity>

    @Query("UPDATE playlists_table SET tracks_id = :tracksId, tracks_count = :tracksCount WHERE id = :playlistId")
    suspend fun updateTracks(playlistId: Int, tracksId: String, tracksCount: Int)         //Обновить список треков
}