package com.example.playlistmaker.data.media

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.data.media.dao.PlaylistDao
import com.example.playlistmaker.data.media.dao.TrackDao
import com.example.playlistmaker.data.media.entity.PlaylistEntity
import com.example.playlistmaker.data.media.entity.TrackEntity

@Database(version = 3, entities = [TrackEntity::class, PlaylistEntity::class])
abstract class AppDatabase: RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao

}