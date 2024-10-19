package com.example.playlistmaker.data.media.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists_table")

data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "playlist_name")
    val playlistName: String,
    @ColumnInfo(name = "playlist_description")
    val playlistDescription: String,
    @ColumnInfo(name = "playlist_image")
    val playlistImage: String?,
    @ColumnInfo(name = "tracks_id")
    val tracksId: String?,
    @ColumnInfo(name = "tracks_count")
    val tracksCount: Int
)
